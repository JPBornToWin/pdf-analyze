package com.knowmap.top.common;

import lombok.Getter;

public class FileConstant {
    static {
        if ("windows".equals(System.getProperties().getProperty("os.name"))) {
            ROOT = "/Users/xiahui/knowmap-file";
        } else {
//            ROOT = "/root/knowmap-file";
            ROOT = "/Users/xiahui/knowmap-file";
        }
    }

    @Getter
    private static String ROOT;

    private static String jsonDir = ROOT + "/" + "jsonFile/";


    private static String pdfDir = ROOT + "/" + "pdfFile/";

    public static String getJsonDir(String checksum) {
        return jsonDir + checksum;
    }

    public static String getPdfPath(String checksum) {
        return pdfDir + checksum + ".pdf";
    }

}
