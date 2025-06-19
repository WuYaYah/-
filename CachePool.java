// CachePool.java
import java.util.concurrent.ConcurrentHashMap;

/**
 * CachePool - 用於快取並管理遊戲中各種狀態，
 * 如玩家座標 (name.pos)、玩家分數 (name.score) 等。
 */
public class CachePool {
    private final ConcurrentHashMap<String, Object> pool = new ConcurrentHashMap<>();

    /** 設定快取值 */
    public void set(String key, Object value) {
        pool.put(key, value);
    }

    /** 讀取快取值 */
    public Object get(String key) {
        return pool.get(key);
    }

    /** 判斷是否已有此 key */
    public boolean contains(String key) {
        return pool.containsKey(key);
    }

    /** 移除某個快取值 */
    public void remove(String key) {
        pool.remove(key);
    }
}
