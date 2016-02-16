//
//   Copyright 2016  Cityzen Data
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package com.warp10.app;

import android.os.Build;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;


/**
 * Created by ahebert on 1/18/16.
 */

/**
 * Class creating a WebSocket
 */
public class WebSocketDataListener {

    //private WebSocketClient webSocketClient;

    /**
     * Current WebSocketClient
     */
    private SecureWebSocket webSocketClient;

    /**
     * URI of the Warp10 platform
     */
    private URI uri;

    /**
     * Private Write token
     */
    private String token;
    /**
     * Boolean to indicate if 1 connection is opened
     */
    private boolean closed = true;

    public String getError() {
        return error;
    }

    private String error = null;

    /**
     * Indicate if the current webSocket is Connected
     * @return
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Private class creating a WebSocketClient objet, implementing needed methods
     */
    private class SecureWebSocket extends WebSocketClient {

        /**
         * Set the URI to the WebSocket
         * @param serverUri
         * @param draft
         */
        public SecureWebSocket( URI serverUri, Draft draft ) {
            super( serverUri, draft);
        }

        /**
         * Add the Auntenticate Token when initiated the WebSocket
         * @param serverHandshake
         */
        public void onOpen(ServerHandshake serverHandshake) {
            Log.i("CZD Websocket", "Opened");
            FileService.writeLogFile("TOKEN" + "\n");
            webSocketClient.send("TOKEN " + token);
        }


        /**
         * Close the websocket
         */
        public void close() {
            super.close();
        }

        /**
         * Implement the return message method
         * @param s
         */
        public void onMessage(String s) {
            Log.i("CZD Websocket", "onMessage " + s);
        }

        /**
         * Implement the action to execute whem the socket is closed
         * @param i
         * @param s
         * @param b
         */
        public void onClose(int i, String s, boolean b) {
            Log.i("CZD Websocket", "Closed " + s);
            FileService.writeLogFile("Socket closed" + "\n");
            webSocketClient = null;
        }

        /**
         * Action to execute when a mistake is detected
         * @param e
         */
        public void onError(Exception e) {
            //Log.i("CZD Websocket", "Error " + e.getMessage());
            //FileService.writeLogFile(e.getMessage());
            error = e.getMessage();
        }
    }


    /**
     * Constructor
     * @param url Warp10 address
     * @param token Authenticate token alloing write on a Warp10 platform
     */
    public WebSocketDataListener(String url, String token) {
        error = null;
        try {
            this.token = token;
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Log.e("CZD Websocket", "Bad URI " + url);
            return;
        }
        if(null != this.webSocketClient) {
            this.webSocketClient.close();
        }
        if(null == this.webSocketClient) {
            this.webSocketClient = new SecureWebSocket(uri, new Draft_17());
            this.connectWebSocket();
            closed = false;
        }
    }

    /**
     * Connect to the current WebSocketClient ot the Warp10 platform
     */
    public void connectWebSocket() {
        Log.i("Websocket", "Connect");
        FileService.writeLogFile("Connect");

        if (null == uri) {
            return;
        }

        if ("google_sdk".equals( Build.PRODUCT )) {
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }

        Map<String, String> headers = new HashMap<String, String>();
        //headers.put("Sec-WebSocket-Protocol", "http-only");
        headers.put("Sec-WebSocket-Protocol", "http-only, chat");
        //headers.put("token",token);
        //FileService.writeLogFile("ToString = " + uri.toString() + "\n");

        try {
            if(uri.toString().startsWith("wss")) {
                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, null, null); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
                webSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));

                webSocketClient.connectBlocking();
                //Log.d("CZD Websocket", "return " + retur);
                //FileService.writeLogFile("CONNECT + END SECURE \n");
            }
            else if (uri.toString().startsWith("ws")){
                webSocketClient.connectBlocking();
                FileService.writeLogFile("CONNECT + END \n");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the current WebSocketClient
     */
    public void closeWebSocket() {
        closed = true;
        FileService.writeLogFile("Close");
        if (null != webSocketClient ) {
            webSocketClient.close();
        }
    }

    /**
     * Push data to a Warp10 platform using current WebSocketClient connected
     * @param data
     */
    public boolean writeData(String data) {
        if(null != webSocketClient) {
            if (data != "") {
                //Log.d("Websocket", "sendData");
                if(WebSocket.READYSTATE.OPEN == webSocketClient.getReadyState()) {
                    webSocketClient.send(data.replaceAll("(?m)^[ \t]*\r?\n", ""));
                    return true;
                }
            }
        }
        return false;
    }

    public String checkConnection() {
        if (null == webSocketClient) {
            if(!closed) {
                webSocketClient = new SecureWebSocket(uri, new Draft_17());
                this.connectWebSocket();
                return "Connection was lost but collect not ended try to establish a new one. Error message was : " + error;
            } else
            {
                return "No connection with a WebSocket and no collect is active.";
            }
        }
        if(WebSocket.READYSTATE.OPEN == webSocketClient.getReadyState()) {
            return "Web socket is still Active";
        }
        if(WebSocket.READYSTATE.CLOSED == webSocketClient.getReadyState()) {
            if(!closed) {
                this.connectWebSocket();
                return "WebSocket was closed but collect not ended, try to start a new connection. Error message was : " + error;
            } else
            {
                return "WebSocket is closed and no collect is active.";
            }
        }
        if(WebSocket.READYSTATE.CONNECTING == webSocketClient.getReadyState()) {
            return "Web socket is connecting";
        }
        if(WebSocket.READYSTATE.CLOSING == webSocketClient.getReadyState()) {
            return "Web socket is closing";
        }
        return "Unkown";
    }
}
