package com.minsang.study.common;

public class HelloMessage {

    public static String greet(String name) {
        return "Hello, " + name + "! (from module-common)";
    }

    public static String bye(String name){
        return "Bye, " + name + "! (from module-common)";
    }
}
