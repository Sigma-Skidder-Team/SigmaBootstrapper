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

public class JelloBootstrap {

    private String[] launchArgs;

    public JelloBootstrap(String[] args) {
        File file = new File("SigmaJelloPrelauncher.jar");
        this.launchArgs = args;
        this.downloadFileFromUrl("https://github.com/MarkGG8181/cloud/raw/refs/heads/main/sigma/SigmaJelloPrelauncher.jar", file);
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
            System.err.println(e.getMessage());
        }
    }

    public void launchPrelauncher(File file) {
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            Class<?> cl = urlClassLoader.loadClass("info.sigmaclient.jelloprelauncher.JelloPrelauncher");
            Class[] mainArgType = new Class[]{String[].class};
            Method main = cl.getMethod("main", mainArgType);
            Object[] argsArray = new Object[]{this.launchArgs};
            main.invoke(null, argsArray);
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
