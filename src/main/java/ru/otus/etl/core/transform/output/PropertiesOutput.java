package ru.otus.etl.core.transform.output;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.core.transform.EtlTransformException;
import ru.otus.etl.core.transform.cmd.CmdInterpreter;

public class PropertiesOutput implements EtlOutput {
    private static final String DELIM = "=";
    private static final String LINE_END = "\r\n";

    private final List<Extractable> srcDataRecs;
    private final List<Rule> rules;

    public PropertiesOutput(List<Extractable> srcDataRecs, List<Rule> rules, Set<String> mappedFields) throws PropertiesOutputException {
        if (srcDataRecs.size() > 1) {
            throw new PropertiesOutputException("Multirows sources not supported");
        }
        this.srcDataRecs = srcDataRecs;
        this.rules = rules;
    }

    public InputStream getOutput() throws EtlTransformException {
        StringBuilder sb = new StringBuilder();
        if (srcDataRecs.isEmpty()) {
            return new ByteArrayInputStream(new byte[0]);
        }
        Extractable rec = srcDataRecs.get(0);
        for (Rule rule : rules) {
            sb.append(rule.getLeft()).append(DELIM).append(CmdInterpreter.exec(rule.getRight(), rec)).append(LINE_END);
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }
}
