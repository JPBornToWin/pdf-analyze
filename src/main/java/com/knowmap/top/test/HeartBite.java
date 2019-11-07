package com.knowmap.top.test;


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HeartBite {
    public static void main(String[] args) throws Exception {
        Process process = Runtime.getRuntime().exec("python3 /Users/xiahui/PycharmProjects/testknowmap/shell.py");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (true) {
            Thread.sleep(2000);
            String s;
            if (!reader.ready()) {
                System.out.println("shell error");
                break;
            }
            System.out.println(s = reader.readLine());
            if (s.equals("done")) {
                System.out.println("shell done");
                break;
            }
        }
    }
}
