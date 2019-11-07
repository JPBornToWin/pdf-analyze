package com.knowmap.top.test;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int count = 0;
        for (int i = a; i <= b; i++) {
            if (!(i + "").equals(new StringBuilder().append(i).reverse().toString())) {
                continue;
            }

            if (deal(i)) {
                System.out.println(i);
                count++;
            }
        }

        System.out.println(count);

    }

    public static boolean deal(int a) {
        for (int i = 2; i * i <= a; i++) {
            if (a % i == 0) {
                return false;
            }
        }

        return true;
    }
}
