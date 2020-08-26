package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FConst extends BaseCmd implements Cmd {

    private String args;

    @Override
    public void setArgs(String args) {
        this.args = args;
    }

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return args;
    }

}
