package ru.otus.etl.core.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.ToString;

public class ExtractableMap implements Extractable {

    private final Map<String, String> map;
    
    public ExtractableMap() {
        map = new HashMap<>();
    }
    
    public void put(String key, String value) {
        map.put(key, value);
    }
    
    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    public String toString() {
        return map.toString();
    }

    @Override
    public List<String> getList(String prefix) {
       return map.keySet().stream().filter(key -> key.startsWith(prefix)).map(key -> map.get(key)).collect(Collectors.toList());
    }
    
    
}
