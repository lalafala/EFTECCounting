package utils;

import android.content.Context;
import android.os.Environment;

import com.example.efteccounting.R;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.logging.XMLFormatter;


public class StreamUtils {

        public static String read() {
            String filename = Environment.getExternalStorageDirectory()+ "/url/url.txt";
            File file=new File(filename);

            StringBuffer buffer = new StringBuffer();
            InputStream is= null;
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (is != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String line = "";
                try {
                    while ((line = in.readLine()) != null) {
                        buffer .append( line);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return buffer.toString();
        }

    public static boolean write(String url) throws IOException {

        String filename = Environment.getExternalStorageDirectory()+ "/url/url.txt";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            // 文件路径
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            // 将文件读入输入流
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            buf = new StringBuffer(url);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            return true;
        }
        catch (Exception e1) {
        e1.printStackTrace();
        return  false;
         } finally {
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
    }




}
