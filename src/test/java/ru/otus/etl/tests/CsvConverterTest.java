package ru.otus.etl.tests;

import java.util.List;

import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.converters.CsvConverter;
import ru.otus.etl.core.model.Mapping;

public class CsvConverterTest {
    public static void main(String[] args) throws Exception {
        CsvConverter b = new CsvConverter();
        Mapping m = new Mapping();
        m.setSourceUrl("file:///temp/list.csv");
        // m.setFirstRowAsHeader(true);
        m.setDelimiter(";");
        List<Extractable> list = b.convert(m, false);
        list.forEach(System.out::println);
    }
}
