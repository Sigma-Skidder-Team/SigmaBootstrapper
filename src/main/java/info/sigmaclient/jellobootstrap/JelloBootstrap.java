package info.sigmaclient.jellobootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JelloBootstrap {
    private String[] launchArgs;
    private char[] hexArray;

    public static void main(String[] args) {
        new JelloBootstrap(args);
    }

    public JelloBootstrap(String[] args) {
        String serverHash;
        File file = new File("SigmaJelloPrelauncher.jar");
        this.launchArgs = args;
        this.hexArray = "0123456789abcdef".toCharArray();
        String localSum = "";
        if (file.exists()) {
            localSum = this.getFileSha1Sum(file);
        }
        if ((serverHash = this.queryUrl("https://jelloprg.sigmaclient.cloud/download/prelauncher/version")) != null && !localSum.equals(serverHash)) {
            this.downloadFileFromUrl("https://jelloprg.sigmaclient.cloud/download/prelauncher/" + serverHash, file);
        }
        this.launchPrelauncher(file);
    }

    private void downloadFileFromUrl(String url, File file) {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            try (InputStream is = con.getInputStream();
                 FileOutputStream fos = new FileOutputStream(file);){
                byte[] buff = new byte[8192];
                int readedLen = 0;
                while ((readedLen = is.read(buff)) > -1) {
                    fos.write(buff, 0, readedLen);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String queryUrl(String url) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(url).openConnection();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));){
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                String v7 = sb.toString();
                return v7;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileSha1Sum(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (FileInputStream fis = new FileInputStream(file);){
                int n = 0;
                byte[] buffer = new byte[4096];
                while (n != -1) {
                    n = ((InputStream)fis).read(buffer);
                    if (n <= 0) continue;
                    digest.update(buffer, 0, n);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return this.bytesToHex(digest.digest());
        }
        catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    public void launchPrelauncher(File file) {
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            Class<?> cl = urlClassLoader.loadClass("info.sigmaclient.jelloprelauncher.JelloPrelauncher");
            Class[] mainArgType = new Class[]{new String[0].getClass()};
            Method main = cl.getMethod("main", mainArgType);
            Object[] argsArray = new Object[]{this.launchArgs};
            main.invoke(null, argsArray);
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = this.hexArray[v >>> 4];
            hexChars[j * 2 + 1] = this.hexArray[v & 0xF];
        }
        return new String(hexChars);
    }
}