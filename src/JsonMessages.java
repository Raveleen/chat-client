import java.util.*;

public class JsonMessages {
    private final Map<Integer, Message> list;

    public JsonMessages(Map<Integer, Message> sourceList, int fromIndex, int lastIndex) {
        this.list = new HashMap<>();
        for (int i = fromIndex; i <= lastIndex; i++)
            list.put(i, sourceList.get(i));
    }

    public Map<Integer, Message> getList() {
        return list;
    }
}
