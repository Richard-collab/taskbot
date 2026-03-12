package org.example.chat.utils;

import java.net.InetAddress;

public class BaseUtils {

    private static final boolean IS_LOCAL = isLocalComputer(); // 是否本机
    private static final boolean IS_NEW_VM = isNewVmComputer(); // 是否新虚机

    public static String getHost() {
        String host = null;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            host = inetAddress.getHostName();
//            System.out.println("本机host： " + host);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return host;
    }

    private static boolean isLocalComputer() {
        String host = getHost();
        return "DT-Wangqi".equals(host);
    }

    public static boolean isLocal() {
        return IS_LOCAL;
    }

    private static boolean isNewVmComputer() {
        String host = getHost();
        return "DT-RDP-BD".equals(host);
    }

    public static boolean isNewVM() {
        return IS_NEW_VM;
    }

    public static void main(String[] args) {
        String host = getHost();
        System.out.println(host);
    }
}
