package corex.core.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Joshua on 2018/3/28.
 */
public class RandomUtil {

    public static int nextInt(int lower, int upper) {
        return ThreadLocalRandom.current().nextInt(lower, upper);
    }

    public static int nextInt(int upper) {
        return ThreadLocalRandom.current().nextInt(upper);
    }

    public static <T> T randomOneInList(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("list不能为空");
        }
        return list.get(nextInt(0, list.size()));
    }
}
