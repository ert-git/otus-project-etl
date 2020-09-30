package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FConst extends CmdInterpreter {

    private final String result;

    public FConst(String args) {
        super(args);
        this.result = args;
    }

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return result;
    }

}
