package com.knowmap.top.test;


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HeartBite {
    public static void main(String[] args) throws Exception {
        Process process = Runtime.getRuntime().exec("python3 /Users/xiahui/PycharmProjects/resetPDF/test/printTest.py");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        while (true) {
            Thread.sleep(4000);
            String s;
            System.out.println(s = errorReader.readLine());
            if (s.equals("done")) {
                System.out.println("shell done");
                break;
            }
        }
    }
}
