package com.smilewatermelon.kafka;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class Application {


    public static void main(String[] args) throws UnknownHostException {
        System.out.println(11);
        InetAddress localHost = InetAddress.getLocalHost();
        String hostName = localHost.getCanonicalHostName();
        System.out.println(hostName);
    }
}