package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FConcat extends CmdInterpreter {

    private final String[] args;

    public FConcat(String args) {
        super(args);
        this.args = args.split(",");
    }

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return String.join("", args);
    }

}
