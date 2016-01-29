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

import android.content.Context;
import android.os.Environment;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by ahebert on 12/17/15.
 * Class managing the file system reading and writing
 */
public class FileService {
    /**
     * Main application context
     */
    private static Context context;

    /**
     * INTERN and EXTERN are used to determine if it's possible to write data on SD-Card
     */
    private final static int INTERN = 0;
    private final static int EXTERN = 1;

    /**
     * Max size of a tmp file
     */
    private final static int MEGA_OCTET = 1024 * 1024;
    private static int MAX_FILE_SIZE = MEGA_OCTET;

    /**
     * Value max for current directory
     */
    private static int LIMIT_SIZE = 100 * MEGA_OCTET;

    public static void setLimitSize(int limitSize) {
        if (limitSize < 100) {
            limitSize = 100;
        }
        LIMIT_SIZE = limitSize * MEGA_OCTET;
    }

    /**
     * Choose limitation mode : keep old, or young value
     */
    private static int MODE = 0;

    public static void setMODE(boolean bool){
        if(true == bool) {
            MODE = 1;
        } else {
            MODE = 0;
        }
    }

    /**
     * Flush-time
     */
    protected static int FLUSH_TIME = 60 * 1000;

    protected static long MIN_VALID_DATE = 0;

    protected static List<File> writeInFile;

    /**
     * Method used to write data on files
     * @param data
     * @param ctxt
     */
    public static void writeToFile(String data, Context ctxt) {
        if (null == writeInFile)
        {
            writeInFile = new ArrayList<>();
        }
        context = ctxt;
        String filename = "fill";
        FileOutputStream outputStream;
        File file;
        //Boolean isIntern = true;
        if (isExternalStorageWritable()) {
            MAX_FILE_SIZE = 10 * MEGA_OCTET;
            file = canCreateFile(filename, data.length(), EXTERN);
            //isIntern = false;
        } else {
            MAX_FILE_SIZE = MEGA_OCTET;
            file = canCreateFile(filename, data.length(), INTERN);
        }
        if (null != file) {
            try {/*
                if(isIntern)
                {
                    outputStream = openFileOutput(file.getName(),MODE_PRIVATE);
                    outputStream.write(data.getBytes(Charset.forName("UTF-8")));
                    outputStream.flush();
                    outputStream.close();
                }
                else {*/
                String dataToSend = new String();
                if (file.length() > 0) {
                    dataToSend = "\n";
                }
                if(!"".equals(data) && !"alarm".equals(data)) {
                    dataToSend += data;
                    dataToSend.replaceAll("(?m)^[ \t]*\r?\n", "");
                    outputStream = new FileOutputStream(file, true);
                    outputStream.write(dataToSend.getBytes(Charset.forName("UTF-8")));
                    outputStream.flush();
                    outputStream.close();
                } else {
                    if (0 == file.length()) {
                        file.delete();
                        //file.se
                    }
                }
                // }
            /*
            File dir = new File(context.getExternalFilesDir(
                    null), filename);

            Log.d("LOGFILE", file.getAbsolutePath() + "\n DIR: " + dir.getPath() + "\n" + dir.listFiles().length);
            */
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                writeInFile.remove(file);
            }
        }
    }

    /**
     * Check existing file, if it finds a file more recent than Flush-time, and not too large, then right on it,
     * otherwise create a new file, adding current system time as suffix.
     * @param filename prefix of the file, and if on card SD, name of the folder
     * @param data size of the data to write on file
     * @param type If case is write a file on the intern memory or on the SD-Card
     * @return name of the file were data have to be writed
     */
    private static File canCreateFile(String filename, int data, int type) {
        String[] names;
        File dir;
        if (INTERN == type) {
            dir = context.getFilesDir();
            //Log.d("TYPE", "Intern");
        } else if (EXTERN == type) {
            dir = new File(context.getExternalFilesDir(
                    null), filename);
            dir.mkdirs();
            dir.setWritable(true);
            //Log.d("TYPE", "Extern");
        } else {
            return null;
        }
        names = dir.list();
        //Log.d("FILEINFO", "externDir: " + dir.isFile() + dir.getFreeSpace() + " " + names.length);
        Boolean validFile = false;
        File resultFile = null;
        if(!computeLimitationPlace(dir, data)){
           return resultFile;
        }
        if (names != null && names.length > 0) {
            for (String name : names) {
                //Log.d("FILESINDIR", name);
                if (name.endsWith(".tmp")) {
                    File file = new File(dir, name);
                    String[] split = name.split("-");
                    long timeStamp = 0;
                    try {
                        timeStamp = Long.parseLong(split[1].replace(".tmp", ""));
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                    if (file.length() >= MAX_FILE_SIZE) {
                        //renameFile(file);
                        compressFile(file);
                    } else if (System.currentTimeMillis() - timeStamp >= FLUSH_TIME || timeStamp < MIN_VALID_DATE) {
                        compressFile(file);
                    } else if (!validFile) {
                        if (!writeInFile.contains(file)) {
                            validFile = true;
                            resultFile = file;
                            writeInFile.add(file);
                            break;
                        }
                    }
                }
            }
        }
        if (!validFile && dir.getFreeSpace() > data * 5) {
            resultFile = new File(dir, filename + "-" + System.currentTimeMillis() + ".tmp");
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Log.d("LOGFILE",resultFile.getParent() + "\n" + dir.getPath() + "\n" + dir.listFiles().length + " " + resultFile.isFile());
        }
        return resultFile;
    }

    /**
     * Method used to computeLimitationPlace
     * Check mode register by user
     * Case keep old files :
     *  Stop if full size
     * Case keep recent files :
     *  Keep 30MO of memory for temporary files
     *  Delete old metrics files when full
     * @param dir directory name
     * @param bufferSize data size to write in files
     * @return false iif can't write anymore
     */
    private static boolean computeLimitationPlace(File dir, int bufferSize) {
        Pair<Integer,Integer> filesSize = getFilesSize(dir);
        if(0 == MODE)
        {
            if(filesSize.first + filesSize.second + bufferSize + 10 >= LIMIT_SIZE) {
                String errorMessage = "Full allowed sized reached. " +
                        "Writing might not be possible anymore. " +
                        "Flush, delete files or allow more places on disk";
                FlushService.sendNotification(context, "Full size reached", errorMessage);
                writeLogFile(errorMessage);
                File[] tmpFiles = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".tmp");
                    }
                });
                for (File tmp:tmpFiles) {
                    compressFile(tmp);
                }
                filesSize = getFilesSize(dir);
                if(filesSize.first + filesSize.second + bufferSize + 10 >= LIMIT_SIZE) {
                    return false;
                }
            }
        } else if (1 == MODE)
        {
            if(filesSize.second + bufferSize + 10 >= 3*MAX_FILE_SIZE)
            {
                File[] tmpFiles = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".tmp");
                    }
                });
                for (File tmp:tmpFiles) {
                    compressFile(tmp);
                }
                String errorMessage = "Full temporary sized reach. " +
                        "Compressed all current temporary files done.";
                FlushService.sendNotification(context, "Full size reached", errorMessage);
                writeLogFile(errorMessage);
            }
            if(filesSize.first + 3*MAX_FILE_SIZE + 1024 >= LIMIT_SIZE) {
                deleteOldestMetricFile(dir);
                String errorMessage = "Full allowed sized reached. " +
                        "Oldest compressed files have been deleted.";
                FlushService.sendNotification(context, "Full size reached", errorMessage);
                writeLogFile(errorMessage);
                // send a notification
            }

        }
        return true;
    }

    /**
     * Method used to delete the oldest metric file in a directory
     * @param dir directory file
     */
    private static void deleteOldestMetricFile(File dir) {
        File[] metricsFile = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".metrics");
            }
        });
        File oldestFile = null;
        long oldTime = 0;
        for (File currentFile:metricsFile) {
            String[] split = currentFile.getName().split("-");
            long timeStamp = 0;
            try {
                timeStamp = Long.parseLong(split[1].replace(".metrics", ""));
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }

            if (null == oldestFile) {
                oldestFile = currentFile;
                oldTime = timeStamp;
            } else {
                if(timeStamp<oldTime) {
                    oldestFile = currentFile;
                    oldTime = timeStamp;
                }
            }
        }
        if(oldestFile!=null) {
            oldestFile.delete();
        }
    }

    /**
     * Return a pair composed by the size of all the metrics files, and all the tmp files
     * @param dir
     * @return
     */
    private static Pair<Integer, Integer> getFilesSize(File dir) {
        String[] names = dir.list();
        int metricsSize = 0;
        int tmpSize = 0;
        if (names != null && names.length > 0) {
            for (String name : names) {
                if (name.endsWith(".metrics")) {
                    metricsSize += new File(dir, name).length();
                } else if (name.endsWith(".tmp")) {
                    tmpSize += new File(dir, name).length();
                }
            }
        }
        return new Pair<>(metricsSize,tmpSize);
    }


    protected static void setContext(Context ctx)
    {
        context = ctx;
    }

    /**
     * Write data on logFiles, need context set before
     * @param data
     */
    public static void writeLogFile(String data)
    {
        FileOutputStream outputStream;
        File file = new File(context.getExternalFilesDir(
                null), "warp.log");
        if (file != null) {
            try {/*
                if(isIntern)
                {
                    outputStream = openFileOutput(file.getName(),MODE_PRIVATE);
                    outputStream.write(data.getBytes(Charset.forName("UTF-8")));
                    outputStream.flush();
                    outputStream.close();
                }
                else {*/
                outputStream = new FileOutputStream(file, true);
                outputStream.write(data.getBytes(Charset.forName("UTF-8")));
                outputStream.flush();
                outputStream.close();
                // }
            /*
            File dir = new File(context.getExternalFilesDir(
                    null), filename);

            Log.d("LOGFILE", file.getAbsolutePath() + "\n DIR: " + dir.getPath() + "\n" + dir.listFiles().length);
            */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Rename a file from .tmp to .metrics
     * @param file name of the file to rename
     * @return if this action succeed
     */
    @Deprecated
    private static boolean renameFile(File file) {
        file.setWritable(true);
        String newPath = file.getAbsolutePath().replace(".tmp", ".metrics");
        File newFile = new File(newPath);
        newFile.mkdirs();
        newFile.delete();
        return file.renameTo(newFile);
    }

    /**
     * Rename and compress a file from .tmp to .metrics
     * @param file name of the file to rename
     * @return if this action succeed
     */
    private static boolean compressFile(File file) {
        //file.setWritable(true);
        String source_filepath = file.getAbsolutePath();
        String destinaton_zip_filepath = file.getAbsolutePath().replace(".tmp", ".metrics");
        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fileOutputStream =new FileOutputStream(destinaton_zip_filepath);
            GZIPOutputStream gzipOuputStream = new GZIPOutputStream(fileOutputStream);
            FileInputStream fileInput = new FileInputStream(source_filepath);

            int bytes_read;
            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOuputStream.write(buffer, 0, bytes_read);
            }
            fileInput.close();
            gzipOuputStream.finish();
            gzipOuputStream.close();
            //Log.d("FILE", "The file was compressed successfully! " + destinaton_zip_filepath);
            file.delete();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Read data contained in a compressed metrics file
     * @param file
     * @return
     */
    public static String readMetricFile(File file) {
        String body = new String();

        try {
            FileInputStream fileIn = new FileInputStream(file);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
            //FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile);

            int bytes_read;

            Reader reader = null;
            StringWriter writer = null;
            String charset = "UTF-8";

            reader = new InputStreamReader(gZIPInputStream, charset);
            writer = new StringWriter();

            char[] buffer = new char[10240];
            for (int length = 0; (length = reader.read(buffer)) > 0;) {
                writer.write(buffer, 0, length);
            }

            body = writer.toString();

            gZIPInputStream.close();
            writer.close();
            reader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //String text = readFile(fileOutputStream);
        return body;
    }


    /**
     * Check if it's possible to write on SD-Card
     * @return true iif it's possible
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Method used in TEST PURPOSE, NOT IN USE
     * Method use to read all data contained in files created by the application
     * @param filename name of the file
     * @param ctxt context of the Main application
     * @return return string containing all data created
     */
    protected static String readFiles(String filename, Context ctxt) {
        context = ctxt;
        List<File> files = getAllFiles(filename, ctxt, false);
        String result = new String();
        for (File file : files) {
            String textFile = readFile(file);
            result += textFile;
        }
        return result;
    }

    /**
     * Method used to get all file created by the application having extension .metrics
     * Those files having this extension are ready to be flushed
     * Convert also old tmp files to .metrics
     * @param filename file name prefix and in case of SD-Card, name of the directory
     * @param ctxt Main application context
     * @return a list of metrics files
     */
    public static List<File> getAllFiles(String filename, Context ctxt, boolean stop) {
        context = ctxt;
        List<File> files;
        if (isExternalStorageWritable()) {
            files = getAllMetricsFile(filename, INTERN, stop);
            files.addAll(getAllMetricsFile(filename, EXTERN, stop));
        } else {
            files = getAllMetricsFile(filename, INTERN, stop);
        }
        return files;
    }

    /**
     * Read a single file
     * @param file file to read
     * @return Entire content of the file as a String
     */
    public static String readFile(File file) {
        if(file.getName().endsWith(".metrics"))
        {
            return readMetricFile(file);
        }
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            if((line = br.readLine()) != null)
            {
                text.append(line);
            }
            while ((line = br.readLine()) != null) {
                text.append('\n');
                text.append(line);
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

    /**
     * Get all the metrics files contained in the directory Intern or Extern
     * if stop to true
     *  Compress all tmp file in metrics file and add them
     * @param filename prefix/directory
     * @param type INTERN or Extern
     * @return
     */
    private static List<File> getAllMetricsFile(String filename, int type, boolean stop) {
        List<File> files = new ArrayList<File>();
        String[] names;
        File dir;
        if (INTERN == type) {
            dir = context.getFilesDir();
            //Log.d("TYPE", "Intern");
        } else if (EXTERN == type) {
            dir = new File(context.getExternalFilesDir(
                    null), filename);
            dir.mkdirs();
            dir.setWritable(true);
            //Log.d("TYPE", "Extern + " + dir.listFiles().length);
        } else {
            return null;
        }
        names = dir.list();
        if (names != null && names.length > 0) {
            for (String name : names) {
                //Log.d("FILESINDIR", name);
                if (name.endsWith(".metrics")) {
                    File file = new File(dir, name);
                    files.add(file);
                }
                if (name.endsWith(".tmp")) {
                    File file = new File(dir, name);
                    if (stop)
                    {
                        String path = file.getAbsolutePath().replace(".tmp", ".metrics");
                        compressFile(file);
                        files.add(new File(path));
                    } else {
                        String[] split = name.split("-");
                        long timeStamp = 0;
                        try {
                            timeStamp = Long.parseLong(split[1].replace(".tmp", ""));
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                        int flush;
                        if(Integer.MAX_VALUE == FLUSH_TIME) {
                            flush = FLUSH_TIME;
                        } else {
                            flush = (FLUSH_TIME + FLUSH_TIME / 3);
                        }
                        if (System.currentTimeMillis() - timeStamp > flush) {
                            //renameFile(file);
                            compressFile(file);
                        }
                    }
                }
            }
        }
        return files;
    }
}
