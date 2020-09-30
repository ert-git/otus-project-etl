package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FToLower extends BaseCmd implements Cmd {

    private String arg;

    public String exec(Extractable src) throws EtlTransformException {
        return arg.toLowerCase();
    }

    @Override
    public void setArgs(String arg) {
        this.arg = arg;
    }

}
