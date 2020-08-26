package ru.otus.etl.core.input;

import java.util.Set;

public interface Extractable {

    String get(String key);

    boolean containsKey(String arg);

    Set<String> keySet();
}
