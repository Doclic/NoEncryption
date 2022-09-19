package me.doclic.noencryption.utils;

import me.doclic.noencryption.NoEncryption;

import java.io.*;
import java.util.logging.Level;

public class FileMgmt {
    private static NoEncryption main;

    public static void initialize(NoEncryption main) {
        FileMgmt.main = main;
    }

    public static void checkFolders(String[] folders) {

        for (String folder : folders) {
            File f = new File(folder);
            if (!(f.exists() && f.isDirectory())) {
                f.getParentFile().mkdirs();
                f.mkdir();

            }
        }
    }

    public static String fileSeparator() {

        return System.getProperty("file.separator");
    }

    public static File CheckYMLExists(File file) {

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Pass a file and it will return it's contents as a string.
     *
     * @param file File to read.
     * @return Contents of file. String will be empty in case of any errors.
     */
    public static String convertFileToString(File file) {

        if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try (InputStream is = new FileInputStream(file)) {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                reader.close();
            } catch (IOException e) {
                main.getLogger().log(Level.SEVERE, "Exception: " + e.getMessage());
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    /**
     * Writes the contents of a string to a file.
     *
     * @param source String to write.
     * @param file   File to write to.
     * @return True on success.
     * @throws IOException
     */
    public static void stringToFile(String source, File file) {

        try {

            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            out.write(source);
            out.close();

        } catch (IOException e) {
            main.getLogger().log(Level.SEVERE, "Exception: " + e.getMessage());
        }
    }
}