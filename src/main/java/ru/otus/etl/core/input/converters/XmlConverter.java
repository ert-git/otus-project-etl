package ru.otus.etl.core.input.converters;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.ExtractableXpath;
import ru.otus.etl.core.model.Mapping;

@Slf4j
public class XmlConverter implements EtlConverter {

    @Override
    public List<Extractable> convert(Mapping mapping, boolean checkOnly) throws EtlException {
        try (FileInputStream fileIS = new FileInputStream(mapping.getSourceUrl())) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIS);
            List<Extractable> result = new ArrayList<>();
            result.add(new ExtractableXpath(xmlDocument));
            return result;
        } catch (Exception e) {
            log.error("convert: failed for {}", mapping, e);
            throw new EtlException("Не удалось загрузить исходный xml документ");
        }
    }

    @Override
    public Map<Integer, String> getHeaders(Mapping mapping) throws EtlException {
        return new HashMap<>();
    }
}
