package ru.otus.etl.core.input.converters;

import ru.otus.etl.core.model.Mapping;

public class ConverterFactory {
    
    private ConverterFactory() {}

    public static EtlConverter get(Mapping mapping) throws ConverterNotFoundException {
        String url = mapping.getSourceUrl().toLowerCase();
        if (url.endsWith(".csv")) {
            return new CsvConverter();
        } else if (url.endsWith(".xls") || url.endsWith(".xlsx")) {
            return new ExcelConverter();
        } else if (url.endsWith(".xml")) {
            return new XmlConverter();
        }
        throw new ConverterNotFoundException("Не найден конвертер для " + url);
    }
}
