package com.knowmap.top.test;


public class Shell {
    public static void main(String[] args) throws Exception {
        int i = 0;
        while (i < 3) {
            Thread.sleep(3000);
            System.out.println("1");
            System.out.flush();
            i++;
        }
    }
}
