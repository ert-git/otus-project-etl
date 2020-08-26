package ru.otus.etl.core.transform.output;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.core.transform.EtlTransformException;
import ru.otus.etl.core.transform.cmd.BaseCmd;

public class ScsvOutput implements EtlOutput {
    private static final String LINE_END = "\r\n";
    private static final String DELIM = ",";

    private List<Extractable> srcDataRecs;
    private List<Rule> rules;

    public ScsvOutput(List<Extractable> srcDataRecs, List<Rule> rules, Set<String> mappedFields) {
        this.srcDataRecs = srcDataRecs;
        this.rules = rules;
    }

    @Override
    public InputStream getOutput() throws EtlTransformException {
        if (srcDataRecs.isEmpty()) {
            return new ByteArrayInputStream(new byte[0]);
        }
        StringBuilder sb = new StringBuilder();
        for (Extractable rec : srcDataRecs) {
            for (Rule rule : rules) {
                sb.append(BaseCmd.exec(rule.getRight(), rec)).append(DELIM);
            }
            sb.deleteCharAt(sb.length() - 1).append(LINE_END);
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

}
