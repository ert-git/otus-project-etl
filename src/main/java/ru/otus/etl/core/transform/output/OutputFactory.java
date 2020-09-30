package ru.otus.etl.core.transform.output;

import java.util.List;
import java.util.Set;

import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.core.model.Mapping.ResultType;
import ru.otus.etl.core.transform.EtlTransformException;

public class OutputFactory {

    private OutputFactory() {
    }

    public static EtlOutput get(Mapping mapping, List<Extractable> srcDataRecs) throws EtlTransformException {
        ResultType resultType = mapping.getResultType();
        if (resultType == null) {
            throw new OutputTypeNotSupportedException("Result type not set");
        }
        List<Rule> rules = mapping.getNormalizedRules();
        Set<String> mappedFields = mapping.getMappedFields(rules);
        switch (resultType) {
        case JSON:
            return new JsonOutput(srcDataRecs, rules, mappedFields, mapping.getUnmappedFieldName());
        case CSV:
            return new CsvOutput(srcDataRecs, rules, mappedFields);
        case SCSV:
            return new ScsvOutput(srcDataRecs, rules, mappedFields);
        case PROPERTIES:
            return new PropertiesOutput(srcDataRecs, rules, mappedFields);
        default:
            throw new OutputTypeNotSupportedException("Type " + resultType + " not supported");
        }
    }
}
