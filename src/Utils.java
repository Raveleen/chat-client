import java.util.HashMap;

public class Utils {
    private static final String URL = "http://127.0.0.1";
    private static final int PORT = 8080;
    private static String login;
    private static String roomname;
    private static int counter;
    private static int privateCounter;

    public static String getURL() {
        return URL + ":" + PORT;
    }

    public static String getLogin() {
        return login;
    }

    public static void setLogin(String login) {
        Utils.login = login;
    }

    public static String getRoomname() {
        return roomname;
    }

    public static void setRoomname(String roomname) {
        Utils.roomname = roomname;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        Utils.counter = counter;
    }

    public static int getPrivateCounter() {
        return privateCounter;
    }

    public static void setPrivateCounter(int privateCounter) {
        Utils.privateCounter = privateCounter;
    }
}
