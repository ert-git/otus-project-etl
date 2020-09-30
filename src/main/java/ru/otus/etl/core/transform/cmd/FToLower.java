package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FToLower extends CmdInterpreter {

    private final String target;

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return target.toLowerCase();
    }

    public FToLower(String arg) {
        super(arg);
        this.target = arg;
    }

}
