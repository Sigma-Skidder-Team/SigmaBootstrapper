package info.sigmaclient.jellobootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JelloBootstrap {

    private String[] launchArgs;

    public static void main(String[] args) {
        new JelloBootstrap(args);
    }

    public JelloBootstrap(String[] args) {
        File file = new File("SigmaJelloPrelauncher.jar");
        this.launchArgs = args;
        this.downloadFileWithSHA1Check("https://github.com/Sigma-Skidder-Team/SigmaPrelauncher/releases/download/1.0/SigmaJelloPrelauncher.jar", file);
        this.launchPrelauncher(file);
    }

    private void downloadFileWithSHA1Check(String url, File file) {
        try {
            if (file.exists()) {
                File tempFile = new File(file.getParent(), "SigmaJelloPrelauncher_tmp.jar");
                downloadFileFromUrl(url, tempFile);

                // Compare SHA-1 checksums
                String existingFileHash = getSHA1Checksum(file);
                String newFileHash = getSHA1Checksum(tempFile);

                if (!existingFileHash.equals(newFileHash)) {
                    Files.delete(file.toPath());
                    Files.move(tempFile.toPath(), file.toPath());
                    System.out.println("SigmaJelloPrelauncher.jar updated.");
                } else {
                    Files.delete(tempFile.toPath());
                    System.out.println("SigmaJelloPrelauncher.jar is up to date.");
                }
            } else {
                downloadFileFromUrl(url, file);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }
    }

    private void downloadFileFromUrl(String url, File file) {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            try (InputStream is = con.getInputStream();
                 FileOutputStream fos = new FileOutputStream(file);) {
                byte[] buff = new byte[8192];
                int readedLen;
                while ((readedLen = is.read(buff)) > -1) {
                    fos.write(buff, 0, readedLen);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private String getSHA1Checksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                sha1Digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] sha1Bytes = sha1Digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : sha1Bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void launchPrelauncher(File file) {
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            Class<?> cl = urlClassLoader.loadClass("info.sigmaclient.jelloprelauncher.JelloPrelauncher");
            Class[] mainArgType = new Class[]{String[].class};
            Method main = cl.getMethod("main", mainArgType);
            Object[] argsArray = new Object[]{this.launchArgs};
            main.invoke(null, argsArray);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}