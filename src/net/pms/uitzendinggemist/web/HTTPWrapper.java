package net.pms.uitzendinggemist.web;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import net.pms.PMS;

public abstract class HTTPWrapper {

    public static String strCookies = "";
    private static String strHTML = "";

    public static String Request(String URL) {
        return Request(URL, "", "");
    }

    public static String Request(String URL, String PostData) {
        return Request(URL, PostData, "");
    }
    public static String Request(String URL, String PostData, String Referer) {
        String method;
        if (!PostData.equals("")) {
            method = "POST";
        } else {
            method = "GET";
        }
        try {
            URL url = new URL(URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(false);
            http.setRequestMethod(method);
            http.setRequestProperty("Host", url.getHost());
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.3) Gecko/20060426 Firefox/1.5.0.3");
            http.setRequestProperty("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            http.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            http.setRequestProperty("Accept-Encoding", "gzip,deflate");
            http.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            http.setRequestProperty("Keep-Alive", "300");
            http.setRequestProperty("Connection", "keep-alive");
            if (!Referer.equals("0")) {
                http.setRequestProperty("Referer", Referer);
            }
            if (!"".equals(strCookies)) {
                http.setRequestProperty("Cookie", strCookies);
            }
            if (method.equals("POST")) {
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                http.setRequestProperty("Content-Length", "" + PostData.length());
                OutputStreamWriter writePost = new OutputStreamWriter(http.getOutputStream());
                writePost.write(PostData);
                writePost.flush();
            }
            http.connect();
            String encoding = http.getContentEncoding();
            InputStream receiving = null;
            if ("gzip".equalsIgnoreCase(encoding)) {
                receiving = new GZIPInputStream(http.getInputStream());
            } else if ("deflate".equalsIgnoreCase(encoding)) {
                receiving = new InflaterInputStream(http.getInputStream(), new Inflater(true));
            } else {
                receiving = http.getInputStream();
            }
            int intReceived;
            StringBuffer strBuffer = new StringBuffer();
            while ((intReceived = receiving.read()) != -1) {
                strBuffer.append((char) intReceived);
            }
            receiving.close();
            strHTML = strBuffer.toString();
            strBuffer.delete(0, strBuffer.length());
            strBuffer.append(strCookies);
            if (strCookies.length() > 3) {
                strBuffer.append("; ");
            }
            for (int i = 0;; i++) {
                if (http.getHeaderFieldKey(i) == null && http.getHeaderField(i) == null) {
                    break;
                } else if ("Set-Cookie".equalsIgnoreCase(http.getHeaderFieldKey(i))) {
                    String[] fields = http.getHeaderField(i).split(";\\s*");
                    String[] keyvalue = fields[0].split("=");
                    if (strBuffer.indexOf(keyvalue[0] + "=") == -1) {
                        strBuffer.append(keyvalue[0] + "=" + keyvalue[1] + "; ");
                    } else {
                        strBuffer.replace(strBuffer.indexOf(keyvalue[0]), strBuffer.indexOf(";", strBuffer.indexOf(keyvalue[0])), keyvalue[0] + "=" + keyvalue[1]);
                    }
                }
            }
            if (strBuffer.indexOf("; ", strBuffer.length() - 3) != -1) {
                strBuffer.delete(strBuffer.length() - 2, strBuffer.length());
            }
            strCookies = strBuffer.toString();
        } catch (Exception e) {
            PMS.error("HTTP error:", e);
        }
        return strHTML;
    }
}