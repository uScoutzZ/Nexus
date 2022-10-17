package de.uscoutz.nexus.utilities;

import java.util.concurrent.ConcurrentHashMap;

public class BiMap<K, I, V> {

    private ConcurrentHashMap<K, V> values2;
    private ConcurrentHashMap<K, I> values1;

    public BiMap() {
        this.values2 = new ConcurrentHashMap<>();
        this.values1 = new ConcurrentHashMap<>();
    }

    public void clear() {
        this.values2.clear();
        this.values1.clear();
    }

    public void put(K key, I id, V value)
    {
        this.values1.put(key, id);
        this.values2.put(key, value);
    }

    public void remove(K key, I id)
    {
        this.values2.remove(key);
        this.values1.remove(id);
    }

    public I getValue1(K key) {
        return values1.get(key);
    }

    public V getValue2(K key) {
        return values2.get(key);
    }

    public boolean keySet(K key) {
        return this.values2.containsKey(key);
    }

    public boolean idSet(I id) {
        return this.values1.containsKey(id);
    }

    public boolean cointains(V value) {
        return this.values2.contains(value) && this.values1.contains(value);
    }

    public ConcurrentHashMap<K, I> getValues1() {
        return values1;
    }

    public ConcurrentHashMap<K, V> getValues2() {
        return values2;
    }

    public Integer size() {
        if(this.values2.size() == this.values1.size()) {
            return this.values2.size();
        }
        return null;
    }
}
