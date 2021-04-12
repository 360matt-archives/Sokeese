package fr.i360matt.sokeese.utils;


import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 *
 * This class makes it possible to establish a cache system which can expire with the defined time.
 * It is from Vivekananthan but it was remade by me (360matt)
 *
 * @author Vivekananthan M
 * https://github.com/vivekjustthink/WeakConcurrentHashMap
 * https://stackoverflow.com/questions/3802370/java-time-based-map-cache-with-expiring-keys
 *
 * @author 360matt ( reformat - github.com/360matt )
 *
 * @param <K> Key Type
 * @param <V> Value Type
 */
public class ExpirableCallback<K, V> extends ConcurrentHashMap<K, V> {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final Map<K, Long> timeMap = new ConcurrentHashMap<>();
    private long expiryInMillis = 1000;

    public ExpirableCallback () {
        startTask();
    }

    public ExpirableCallback (final long expiryInMillis) {
        this.expiryInMillis = expiryInMillis;
        startTask();
    }

    public final V put (final K key, final V value, int time) {
        timeMap.put(key, System.currentTimeMillis() + time);
        return super.put(key, value);
    }

    private void startTask () {
        executor.scheduleAtFixedRate(() -> {
            final long currentTime = System.currentTimeMillis();

            final Iterator<Entry<K, Long>> iter = timeMap.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<K, Long> entry = iter.next();
                if (currentTime > (entry.getValue() + expiryInMillis)) {
                    ((BiConsumer<?, Boolean>) this.get(entry.getKey())).accept(null, false);
                    remove(entry.getKey());
                    iter.remove();
                }
            }
        }, expiryInMillis / 2, expiryInMillis / 2, TimeUnit.MILLISECONDS);
    }

    public final void quitMap () {
        executor.shutdownNow();
        clear();
        timeMap.clear();
    }

    public final boolean isAlive () {
        return !executor.isShutdown();
    }
}