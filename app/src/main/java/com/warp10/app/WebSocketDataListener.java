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

public class WebSocketDataListener {

    //private WebSocketClient webSocketClient;

    private SecureWebSocket webSocketClient;

    private URI uri;
    private String token;

    private class SecureWebSocket extends WebSocketClient {

        public SecureWebSocket( URI serverUri, Draft draft ) {
            super( serverUri, draft);
        }

        public void onOpen(ServerHandshake serverHandshake) {
            Log.i("CZD Websocket", "Opened");
            webSocketClient.send("TOKEN " + token);
        }

        @Override
        public void close() {
            super.close();
        }

        @Override
        public void onMessage(String s) {
            Log.i("CZD Websocket", "onMessage " + s);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            Log.i("CZD Websocket", "Closed " + s);
            webSocketClient = null;
        }

        @Override
        public void onError(Exception e) {
            Log.i("CZD Websocket", "Error " + e.getMessage());
        }
    }


    public WebSocketDataListener(String url, String token) {
        try {
            this.token = token;
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Log.e("CZD Websocket", "Bad URI " + url);
            return;
        }
        this.webSocketClient = new SecureWebSocket(uri, new Draft_17());
    }

    public void connectWebSocket() {
        Log.i("Websocket", "Connect");

        if (uri == null) {
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

        try {
            if(uri.getPath().startsWith("wss")) {
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

                boolean retur = webSocketClient.connectBlocking();
                //Log.d("CZD Websocket", "return " + retur);
            }
            else if (uri.getPath().startsWith("ws")){
                webSocketClient.connectBlocking();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public void writeData(String data) {
        if(null != webSocketClient) {
            if (data != "") {
                Log.d("Websocket","sendData");
                webSocketClient.send(data.replaceAll("(?m)^[ \t]*\r?\n", ""));
            }
        }
    }
}
