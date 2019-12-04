package com.knowmap.top.common;

import lombok.Getter;

public class FileConstant {
    static {
        if (System.getProperties().getProperty("os.name").contains("Mac")) {
            ROOT = "/Users/xiahui/knowmap-file";

            dealJson =  "java -Djava.library.path=/Users/xiahui/Desktop/pdf/Lib -jar /Users/xiahui/Desktop/pdf/PDF-1.0-SNAPSHOT-jar-with-dependencies.jar";
            dealContent =  "python3 /Users/xiahui/PycharmProjects/resetPDF/dispatcher.py";
        } else {
            ROOT = "/root/knowmap-file";

            dealJson =  "java -Djava.library.path=/root/knowmap-script/pdf/Lib -jar /root/knowmap-script/pdf/PDF-1.0-SNAPSHOT-jar-with-dependencies.jar";
            dealContent =  "python3 /root/knowmap-script/resetPDF/dispatcher.py";
        }
    }

    @Getter
    private static String ROOT;

    private static String jsonDir = ROOT + "/" + "jsonFile/";


    private static String pdfDir = ROOT + "/" + "pdfFile/";

    public static String dealJson;

    public static String dealContent;






    public static String getJsonDir(String checksum) {
        return jsonDir + checksum;
    }

    public static String getPdfPath(String checksum) {
        return pdfDir + checksum + ".pdf";
    }

    public static void main(String[] args) {
        System.out.println(System.getProperties().getProperty("os.name"));
    }

}
