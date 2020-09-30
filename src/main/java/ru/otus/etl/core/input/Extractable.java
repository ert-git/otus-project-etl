package ru.otus.etl.core.input;

import java.util.List;
import java.util.Set;

public interface Extractable {

    String get(String key);
    List<String> getList(String key);

    boolean containsKey(String arg);

    Set<String> keySet();
}
