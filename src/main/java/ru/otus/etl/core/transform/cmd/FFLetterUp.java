package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FFLetterUp extends BaseCmd implements Cmd {

    private String arg;

    public static String up1stLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    public String exec(Extractable src) throws EtlTransformException {
        return up1stLetter(arg);
    }

    @Override
    public void setArgs(String arg) {
        this.arg = arg;
    }

}
