package ru.otus.etl.core.input.converters;

import java.util.List;
import java.util.Map;

import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.model.Mapping;

public interface EtlConverter {
    static final String COLUMN = "col";

    List<Extractable> convert(Mapping mapping, boolean checkOnly) throws EtlException;
    Map<Integer, String> getHeaders(Mapping mapping) throws EtlException;
}
