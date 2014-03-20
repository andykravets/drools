package org.drools.core.common;


import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * User: andrey.kravets
 * Date: 2/14/14 12:36 PM
 */
public class InfinispanBasedTwoLevelCache<K extends Comparable<? super K>, V> implements Map<K, V>, Serializable {

    private final Map<K, V> L1Cache = new HashMap<K, V>();
    private final Cache<K, V> L2Cache;
    private K maxId = null;

    public InfinispanBasedTwoLevelCache() {
        this(null);
    }

    public InfinispanBasedTwoLevelCache(Cache<K, V> cache) {
        if (cache != null)
            L2Cache = cache;
        else {
            try {
                cache = new DefaultCacheManager("infinispan.xml").getCache("FH");
            } catch (IOException e) {
                e.printStackTrace();
                cache = new DefaultCacheManager().getCache("FH");
            } finally {
                L2Cache = cache;
            }
        }
    }

    @Override
    public int size() {
        if (L1Cache.size() != L2Cache.size()) return L2Cache.size();
        return L1Cache.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return L1Cache.containsKey(key) || L2Cache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return L1Cache.containsValue(value) || L2Cache.containsValue(value);
    }

    @Override
    public V get(Object key) {
        V obj = L1Cache.get(key);
        if (obj == null && containsKey(key)) {
            V l2obj = L2Cache.get(key);
            obj = L1Cache.put((K) key, l2obj);
        }
        return obj;
    }

    @Override
    public V put(K key, V value) {
        V val = null;
        synchronized (L1Cache) {
            synchronized (L2Cache) {
                val = L1Cache.put(key, value);
                L2Cache.put(key, value);
            }
            return val;
        }

    }

    @Override
    public V remove(Object key) {
        synchronized (L1Cache) {
            synchronized (L2Cache) {
                V obj = get(key);
                L1Cache.remove(key);
                L2Cache.remove(key);
                return obj;
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        synchronized (L1Cache) {
            synchronized (L2Cache) {
                L1Cache.putAll(m);
                L2Cache.putAll(m);
            }
        }
    }

    @Override
    public void clear() {
        synchronized (L1Cache) {
            synchronized (L2Cache) {
                L1Cache.clear();
                L2Cache.clear();
            }
        }
    }

    @Override
    public Set<K> keySet() {
        cacheSync();
        return L1Cache.keySet();
    }

    @Override
    public Collection<V> values() {
        cacheSync();
        return L1Cache.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        cacheSync();
        return L1Cache.entrySet();
    }

    private void cacheSync() {
        int l1size = 0;
        int l2size = 0;
        synchronized (L1Cache) {
            synchronized (L2Cache) {
                l1size = L1Cache.size();
                l2size = L2Cache.size();
                if (l1size > l2size) {
                    L2Cache.putAll(L1Cache);
                } else if (l2size > l1size) {
                    L1Cache.putAll(L2Cache);
                }
            }
        }
    }

    public K getMaxId(K value, Comparator<? super K> comparator) {
        if (maxId == null) {
            maxId = value;
            for (K key : L2Cache.keySet()) {
                if (comparator.compare(maxId, key) < 0) maxId = key;
            }
        } else {
            if (comparator.compare(value, maxId) > 0) maxId = value;
        }
        return maxId;
    }

    public K getMaxId(K value) {
        if (maxId == null) {
            maxId = value;
            for (K key : L2Cache.keySet()) {
                if (maxId.compareTo(key) < 0) maxId = key;
            }
        } else {
            if (value.compareTo(maxId) > 0) maxId = value;
        }
        return maxId;
    }


}
