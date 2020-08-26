package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FJoin extends BaseCmd implements Cmd {

    private String[] args;

    public String exec(Extractable src) throws EtlTransformException {
        String delimiter = args[0].trim().replaceAll("'", "");
        String[] els = new String[args.length-1];
        for (int i = 1; i < args.length; i++) {
            els[i-1] = args[i].trim();
        }
        return String.join(delimiter, els);
    }

    @Override
    public void setArgs(String args) {
        this.args = args.split(",");
    }

}
