package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FConcat extends BaseCmd implements Cmd {

    private String[] args;

    public String exec(Extractable src) throws EtlTransformException {
        return String.join("", args);
    }

    @Override
    public void setArgs(String args) {
        this.args = args.split(",");
    }

}
