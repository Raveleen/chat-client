import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GetThread implements Runnable {
    private final Gson gson;

    public GetThread() {
        gson = new GsonBuilder().create();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {

                getMessages();
                getPrivateMessages();

                Thread.sleep(500);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getMessages() {
        try {
            int n = Utils.getCounter();
            URL url = new URL(Utils.getURL() + "/get?room=" + Utils.getRoomname() + "&from=" + n);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("GET");
            http.setDoInput(true);

            InputStream is = http.getInputStream();
            try {
                byte[] buf = requestBodyToArray(is);
                String strBuf = new String(buf, StandardCharsets.UTF_8);

                JsonMessages list = gson.fromJson(strBuf, JsonMessages.class);
                if (list != null) {
                    for (Message m : list.getList().values()) {
                        System.out.println(m);
                        Utils.setCounter(n + 1);
                    }
                }
            } finally {
                is.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getPrivateMessages() {
        try {
            int n = Utils.getPrivateCounter();
            URL url = new URL(Utils.getURL() + "/private?login=" + Utils.getLogin() + "&from=" + n);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("GET");
            http.setDoInput(true);

            if (http.getResponseCode() == 200) {
                InputStream is = http.getInputStream();
                try {
                    byte[] buf = requestBodyToArray(is);
                    String strBuf = new String(buf, StandardCharsets.UTF_8);

                    JsonMessages list = gson.fromJson(strBuf, JsonMessages.class);
                    if (list != null) {
                        for (Message m : list.getList().values()) {
                            System.out.println(m);
                            Utils.setPrivateCounter(n + 1);
                        }
                    }
                } finally {
                    is.close();
                }
            } else {
                return;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] requestBodyToArray(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[10240];
        int r;

        do {
            r = is.read(buf);
            if (r > 0) bos.write(buf, 0, r);
        } while (r != -1);

        return bos.toByteArray();
    }
}
