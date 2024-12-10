package me.ildarorama.formatter;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

public class BindingStore {
    private final Map<String, String> store = new HashMap<>();

    public void put(String name, String value) {
        store.put(name, value);
    }

    public String get(String name) {
        if (store.containsKey(name)) {
            return store.get(name);
        }
        throw new MissingFormatArgumentException("No such parameter: " + name);
    }
}
