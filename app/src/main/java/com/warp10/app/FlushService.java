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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ahebert on 12/17/15.
 * Class used to declare the flush methods of the application
 */
public class FlushService {

    /**
     * Main flush, flushing all files .metrics recording during execution time
     * @param filename directory name or main file part name containing all files to flush
     * @param ctxt Main application context
     * @param url WarpUrl where to push data
     * @param token Write toke of the application
     */
    protected  static String issue = "NULL";

    /**
     * Method used to flush all files in directory
     * @param filename dir name
     * @param ctxt Application context
     * @param url url to flush
     * @param token write token
     * @param stop last flush ?
     */
   public static void flushAllFiles(String filename, Context ctxt, String url, String token, boolean stop)
   {
       List<File> files = FileService.getAllFiles(filename,ctxt, stop);
       for (File file : files) {
           final String warpUrl = url;
           final String warpToken = token;
           //Log.d("FLUSH", url);
           final File tmpFile = file;
           final Context ctx = ctxt;
           boolean empty = !file.exists() || 0 == file.length();
           if(!empty) {
               new Thread(new Runnable() {
                   public void run() {
                       //Log.d("POSTDATA", warpUrl + " " + warpToken);
                       //String txt = FileService.readFile(tmpFile);
                       //Log.d("FLUSH THREAD", warpUrl);
                       /*
                       if (postData(tmpFile, warpUrl, warpToken)) {
                           tmpFile.delete();
                       } else {
                           if(!"NULL".equals(issue)) {
                               sendNotification(ctx,"Failed connection to warp", issue);
                               issue = "NULL";
                           }
                       }
                       /*if (postData(new StringBuffer(txt), warpUrl, warpToken)) {
                           tmpFile.delete();
                       }*/
                   }
               }).start();
           }
       }
   }

    /**
     * Static boolean test used only inside sendAtestPost to test if it success.
     * Has to be declared here because it's modified in a different thread.
     */
    private static volatile boolean test;

    /**
     * Method used to test if the url and the token are valid inside the Warp plat-form.
     * This method execute a simple empty POST HTTP. The application need to be connected to internet to execute it.
     * @param buffer
     * @param url
     * @param token
     * @return
     */
    public static boolean sendAtestPost (final StringBuffer buffer, final String url, final String token)
    {
        Thread testPost = new Thread(new Runnable() {
            public void run() {
                //Log.d("POSTDATA", warpUrl + " " + warpToken);
                if (FlushService.postData(buffer, url, token)) {
                    test = true;
                }
            }
        });
        testPost.start();
        try {
            testPost.join();
         } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return test;
    }

    /**
     * Post data on server
     * @param buffer
     * @param url
     * @param token
     * @return
     */
    @Deprecated
    public static boolean postData(StringBuffer buffer, String url, String token) {
        // Create a new HttpClient and Post Header
        URL obj = null;
        HttpsURLConnection con = null;
        OutputStream os = null;
        GZIPOutputStream out = null;
        //Log.d("URLINSIDE", url + " " + token );
        boolean returnValue = false;
        try {
            obj = new URL(url);
            con  = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("X-CityzenData-Token", token);
            con.setRequestProperty("Content-Type", "text/plain");
            con.setChunkedStreamingMode(16384);
            //OutputStream os = con.getOutputStream();
            //os.write(s.getBytes());
            //os.flush();
            //os.close();
            con.connect();

            //Log.d("URLINSIDE", "CONNECT");

            os = con.getOutputStream();
            //out = new GZIPOutputStream(os);
            PrintWriter pw = new PrintWriter(os);
            StringBuilder sb = new StringBuilder(buffer);
            pw.println(sb.toString());
            pw.close();
            int responseCode = con.getResponseCode();

            Log.d("URLINSIDE", "RESPONSECODE");
            if (200 != responseCode) {
                Log.d("Code ", "" + con.getResponseCode());
                Log.d("Code ",con.getResponseMessage());
                FileService.writeLogFile("Code: " + con.getResponseCode() + "message: " + con.getResponseMessage());
                if (500 == responseCode) {
                    issue = con.getResponseMessage();
                    Log.d("Value Issue", issue);
                }
            }

            if(200 == responseCode)
            {
                returnValue = true;
            }

            /*
            StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }*/
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally {
            if(con != null) {
                con.disconnect();
            }
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null)
            {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return returnValue;
        }
    }

    /**
     * Send a notification to the user
     * @param context Application context
     * @param text text
     */
    public static void sendNotification(Context context, String title, String text) {
        int notifyID = 1;
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(com.warp10.app.R.drawable.notification)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new Notification.BigTextStyle().bigText(text));

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(notifyID, mBuilder.build());
    }

    /**
     * Function used to post the data contained in a compressed file
     * @param file compressed file
     * @param url url
     * @param token authenticate token
     * @return
     */
    public static boolean postData(File file, String url, String token) {
        // Create a new HttpClient and Post Header
        URL obj = null;
        HttpsURLConnection con = null;
        OutputStream os = null;
        GZIPOutputStream out = null;
        //Log.d("URLINSIDE", url + " " + token );
        boolean returnValue = false;
        try {
            // connect to url and set header
            obj = new URL(url);
            con  = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("X-CityzenData-Token", token);
            con.setRequestProperty("Content-Type", "application/gzip");
            con.setChunkedStreamingMode(16384);

            con.connect();

            // Read data inside file
            FileInputStream fileIn = new FileInputStream(file);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);

            os = con.getOutputStream();
            out = new GZIPOutputStream(os);

            byte[] buffer = new byte[1024];
            int bytes_read;
            while((bytes_read = gZIPInputStream.read(buffer)) > 0)
            {
                out.write(buffer, 0, bytes_read);
            }
            out.finish();

            //OutputStream os = con.getOutputStream();
            //os.write(s.getBytes());
            //os.flush();
            //os.close();

            //Log.d("URLINSIDE", "CONNECT");
            //pw.println(sb.toString());
            int responseCode = con.getResponseCode();

            Log.d("URLINSIDE", "RESPONSECODE");
            if (200 != responseCode) {
                String responseMessage = con.getResponseMessage();
                Log.d("Code ", "" + responseCode);
                Log.d("Code ", responseMessage);
                FileService.writeLogFile("Code: " + responseCode + "message: " + responseMessage);
                if(500 == responseCode){
                    issue = responseMessage;
                }
            }

            if(200 == responseCode)
            {
                returnValue = true;
            }
            gZIPInputStream.close();
            fileIn.close();
            /*
            StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }*/
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally {
            if(con != null) {
                con.disconnect();
            }
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null)
            {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return returnValue;
        }
    }
}
