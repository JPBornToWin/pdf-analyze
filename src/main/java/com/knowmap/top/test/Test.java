package com.knowmap.top.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) {
//        String commandRoot = "python D:\\software\\pycharmWorkspace\\test\\";
//        String[] commandType = {"error.py", "ok.py", "overTime.py"};

        String command = "java -Djava.library.path=/Users/xiahui/Desktop/pdf/Lib -jar /Users/xiahui/Desktop/pdf/PDF-1.0-SNAPSHOT-jar-with-dependencies.jar /Users/xiahui/Desktop/pdf/cnn_AS.pdf /Users/xiahui/Desktop/pdf/result";
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
//        for (int i = 0; i < 10; i++) {
            Runnable runnable = () -> {
                Process process = null;
                try {
//                    process = Runtime.getRuntime().exec(commandRoot + commandType[new Random().nextInt(3)]);
                    process = Runtime.getRuntime().exec(command);
//                    process = Runtime.getRuntime().exec(commandRoot + "error.py");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("utf-8")));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName("utf-8")));
                String currentLine = null;
                try {
                    Thread.sleep(5 * 1000);
                    while (bufferedReader.ready()) {
                        currentLine = bufferedReader.readLine();
                        System.out.println("thread name : " + Thread.currentThread().getName() + "; 心跳包: " + currentLine);
                        if (currentLine.contains("done")) {
                            break;
                        }
                        Thread.sleep(5 * 1000);
                    }

                    if (currentLine == null || !currentLine.contains("done")) {
                        if (errorReader.ready()) {
                            // 执行抛出异常
                            System.out.println(errorReader.readLine() + " 抛出异常");
                        } else {
                            // 超时
                            System.out.println("thread name : " + Thread.currentThread().getName() + " 心跳超时");
                        }
                    } else {
                        System.out.println("thread name : " + Thread.currentThread().getName() + "执行成功");
                    }

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        errorReader.close();
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (process.isAlive()) {
                        process.destroy();
                    }
                }
            };

            threadPoolExecutor.execute(runnable);
//        }


    }
}

