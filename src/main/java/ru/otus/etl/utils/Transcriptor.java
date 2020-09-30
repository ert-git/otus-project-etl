package ru.otus.etl.utils;

import com.ibm.icu.text.Transliterator;

public class Transcriptor {

    public static final String CYRILLIC_TO_LATIN = "Cyrillic-Latin";

    public static String translit(String st) {
        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        return toLatinTrans.transliterate(st);
    }
}