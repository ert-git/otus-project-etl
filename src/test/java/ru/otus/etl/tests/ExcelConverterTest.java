package ru.otus.etl.tests;

import java.util.List;

import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.converters.ExcelConverter;
import ru.otus.etl.core.model.Mapping;

public class ExcelConverterTest {

    public static void main(String[] args) throws Exception {
        ExcelConverter b = new ExcelConverter();
        Mapping m = new Mapping();
        m.setSourceUrl("file:///temp/list.xls");
        m.setFirstRowAsHeader(true);
        m.setDelimiter(";");
        List<Extractable> list = b.convert(m, false);
        list.forEach(System.out::println);
    }
}
