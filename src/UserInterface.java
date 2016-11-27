import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UserInterface {
    private static String roomname = "main";

    public static void getUserInterface(Scanner scanner) {
        if (authentication(scanner)) {
            Utils.setRoomname("main");
            Thread th = new Thread(new GetThread());
            th.setDaemon(true);
            th.start();

            boolean flag = true;

            while (flag) {
                String text = scanner.nextLine();

                flag = commandController(text, flag);

                if (textChecker(text) == 0) {
                    changeStatus("offline");
                    break;
                }
            }
        }
    }

    private static boolean authentication(Scanner scanner) {
        while (true) {
            try {
                System.out.println("========================");
                System.out.println("Enter your login: ");
                String login = scanner.nextLine();
                Utils.setLogin(login);
                System.out.println("Enter your password: ");
                String pass = scanner.nextLine();

                String url = new StringBuilder().append(Utils.getURL()).append("/login").toString();
                URL obj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();

                String aut = new StringBuilder().append(login + ";" + pass).toString();
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(aut, String.class);
                os.write(json.getBytes(StandardCharsets.UTF_8));

                if (conn.getResponseCode() == 200) {
                    System.out.println("------------------------");
                    System.out.println("Authentication completed.);
                    System.out.println("------------------------");
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int textChecker(String text) {
        String[] result = text.split(";");

        if (text.isEmpty()) {
            return 0;
        }
        if (result[0].equals("$list")) {
            return 1;
        }
        if (result[0].split(" ")[0].equals("$status") &&
                (result[0].split(" ")[1].equals("online") ||
                        result[0].split(" ")[1].equals("busy"))) {
            return 2;
        }
        if (result[0].split(" ")[0].equals("$room")) {
            if (result[0].split(" ")[1].equals("create")) {
                return 3;
            }
            if (result[0].split(" ")[1].equals("enter")) {
                return 4;
            }
        }
        if (result[0].split(" ")[0].equals("$pm") && !result[0].split(" ")[1].equals("show")) {
            return 5;
        }
        if (result[0].split(" ")[0].equals("$pm") && result[0].split(" ")[1].equals("show")) {
            return 6;
        }
        if (result[0].equals("$exit")) {
            return 7;
        }
        return 10;
    }

    private static boolean commandController(String text, boolean flag) {
        switch (textChecker(text)) {
            case 1: {
                accessList();
                break;
            }
            case 2: {
                changeStatus(text.split(";")[0].split(" ")[1]);
                break;
            }
            case 3: {
                createRoom(text);
                break;
            }
            case 4: {
                changeRoom(text);
                break;
            }
            case 5: {
                sendPrivateMessage(text);
                break;
            }
            case 6: {
                break;
            }
            case 7: {
                flag = false;
                changeStatus("offline");
                break;
            }
            case 10: {
                sendMessage(text, Utils.getRoomname());
                break;
            }
        }

        return flag;
    }

    private static void sendMessage(String text, String roomname) {
        try {
            Message m = new Message(Utils.getLogin(), text, null, roomname);
            int res = m.send(new StringBuilder().append(Utils.getURL()).append("/add").toString());

            if (res != 200) {
                System.out.println("HTTP error occured: " + res + ".");
            }
        } catch (IOException e) {
            System.out.println("Sending failed.");
            e.printStackTrace();
        }
    }

    private static void sendPrivateMessage(String text) {
        try {
            String[] temp = text.split(";");
            String message = temp[1];
            String to = temp[0].split(" ")[1];

            Message m = new Message(Utils.getLogin(), message, to, null);
            int res = m.send(new StringBuilder().append(Utils.getURL()).append("/add").toString());

            if (res != 200) {
                System.out.println("HTTP error occured: " + res + ".");
            }
        } catch (IOException e) {
            System.out.println("Sending failed.");
            e.printStackTrace();
        }
    }

    private static void accessList() {
        try {
            String url = new StringBuilder().append(Utils.getURL()).append("/list").toString();
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            InputStream is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[10240];
            int r;

            do {
                r = is.read(buf);
                if (r > 0) bos.write(buf, 0, r);
            } while (r != -1);

            byte[] buffer = bos.toByteArray();

            String strBuf = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().create();
            String list = gson.fromJson(strBuf, String.class);

            fancyListOutput(list);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fancyListOutput(String list) {
        String[] array = list.split(";");
        System.out.printf("%10s%5s%10s", "User", "|", "Status");
        System.out.println();
        System.out.println("=====================================");
        for (int i = 0; i < array.length; i++) {
            System.out.printf("%10s%5s%10s%n", array[i].split(" ")[0], "|", array[i].split(" ")[1]);
        }
        System.out.println("=====================================");
    }

    private static void changeStatus(String status) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(Utils.getURL() + "/status").openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            String a = new StringBuilder(Utils.getLogin()).append(';').append(status).append(';').toString();

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(a, String.class);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            if (httpURLConnection.getResponseCode() == 200) {
                System.out.println("------------------------");
                System.out.println("Status changed.");
                System.out.println("------------------------");
            } else {
                System.out.println("------------------------");
                System.out.println("Error: " + httpURLConnection.getResponseCode() + ".");
                System.out.println("------------------------");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void changeRoom(String text) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(Utils.getURL() + "/rooms").openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            String command = text.split(";")[0].split(" ")[1];
            String roomname = text.split(";")[0].split(" ")[2];

            OutputStream outputStream = httpURLConnection.getOutputStream();
            String a = new StringBuilder(command).append(';').append(roomname).append(';').append(Utils.getLogin()).toString();

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(a, String.class);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            if (httpURLConnection.getResponseCode() == 200) {
                Utils.setRoomname(roomname);
                Utils.setCounter(0);
                System.out.println("------------------------");
                System.out.println("Room entered.");
                System.out.println("------------------------");
            } else {
                System.out.println("------------------------");
                System.out.println("Error: " + httpURLConnection.getResponseCode() + ".");
                System.out.println("------------------------");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createRoom(String text) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(Utils.getURL() + "/rooms").openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            String command = text.split(";")[0].split(" ")[1];
            String roomname = text.split(";")[0].split(" ")[2];

            OutputStream outputStream = httpURLConnection.getOutputStream();
            String a = new StringBuilder(command).append(';').append(roomname).append(';').append(Utils.getLogin()).toString();

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(a, String.class);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            if (httpURLConnection.getResponseCode() == 200) {
                System.out.println("------------------------");
                System.out.println("Room created.");
                System.out.println("------------------------");
            } else {
                System.out.println("------------------------");
                System.out.println("Error: " + httpURLConnection.getResponseCode() + ".");
                System.out.println("------------------------");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
