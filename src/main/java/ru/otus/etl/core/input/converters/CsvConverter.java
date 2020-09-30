package ru.otus.etl.core.input.converters;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.ExtractableMap;
import ru.otus.etl.core.model.Mapping;

@Slf4j
public class CsvConverter implements EtlConverter {

    @Override
    public List<Extractable> convert(Mapping mapping, boolean checkOnly) throws EtlException {
        List<Extractable> list = new ArrayList<>();
        try (InputStream inputStream = new URL(mapping.getSourceUrl()).openStream()) {
            CSVFormat format = CSVFormat.DEFAULT;
            if (mapping.getDelim() != null) {
                format = format.withDelimiter(mapping.getDelim());
            }
            if (mapping.isFirstRowAsHeader()) {
                format = format.withHeader();
            }
            CSVParser parser = CSVParser.parse(inputStream, mapping.getCharset(), format);
            List<String> headerNames = parser.getHeaderNames();
            for (CSVRecord csvRecord : parser) {
                ExtractableMap props = new ExtractableMap();
                list.add(props);
                if (mapping.isFirstRowAsHeader()) {
                    for (String h : headerNames) {
                        props.put(h, csvRecord.get(h));
                    }
                } else {
                    int size = csvRecord.size();
                    for (int i = 0; i < size; i++) {
                        props.put(COLUMN + (i + 1), csvRecord.get(i));
                    }
                }
                if (checkOnly && !list.isEmpty()) {
                    return list;
                }
            }
        } catch (Exception e) {
            log.error("convert failed for {}", mapping, e);
            throw new ConvertFailedException(e);
        }
        return list;
    }

    @Override
    public Map<Integer, String> getHeaders(Mapping mapping) throws ConvertFailedException {
        try (InputStream inputStream = new URL(mapping.getSourceUrl()).openStream()) {
            CSVFormat format = CSVFormat.DEFAULT;
            if (mapping.getDelim() != null) {
                format = format.withDelimiter(mapping.getDelim());
            }
            if (mapping.isFirstRowAsHeader()) {
                format = format.withHeader();
            }
            CSVParser parser = CSVParser.parse(inputStream, mapping.getCharset(), format);
            List<String> headerNames = parser.getHeaderNames();
            Map<Integer, String> map = new TreeMap<>();
            for (int i = 0; i < headerNames.size(); i++) {
                map.put(i, headerNames.get(i));
            }
            return map;
        } catch (Exception e) {
            log.error("convert failed for {}", mapping, e);
            throw new ConvertFailedException(e);
        }
     }
}
