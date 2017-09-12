package com.dcits.sc;

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by lp860 on 2017/7/9.
 */
public class MessageSender {

    static final Logger logger = Logger.getLogger(MessageSender.class.getName());
    public static void start(PrintWriter writer){
        MsgThread thread = new MsgThread(writer);
        thread.start();
    }




}

class MsgThread extends Thread {
        private File logfile = new File("c:/log/sendMail.log");
        private PrintWriter writer;

        File file = null;
        RandomAccessFile randomFile = null;

        public MsgThread(PrintWriter writer) {
            this.writer = writer;

            file = new File("c:/log/sendMail.log");
            try {
                randomFile = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                long filePointer = this.logfile.length();
                RandomAccessFile file = new RandomAccessFile(logfile, "r");
                while (true) {
                    long fileLength = this.logfile.length();
                    if (fileLength < filePointer) {
                        file = new RandomAccessFile(logfile, "r");
                        filePointer = 0;
                    }
                    if (fileLength > filePointer) {
                        file.seek(filePointer);
                        String line = new String(file.readLine().getBytes("iso8859-1"), "utf-8");
                        while (line != null) {
                            System.out.println(line);
                            writer.print(line);
                            line = null;
                            filePointer = file.getFilePointer();
                        }
                    }
                    sleep(500);
                }

            } catch (IOException e) {
                e.printStackTrace();

            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
    }

