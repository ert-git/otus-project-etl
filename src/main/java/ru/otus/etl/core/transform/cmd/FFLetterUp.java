package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FFLetterUp extends CmdInterpreter {

    private final String target;

    public static String up1stLetter(String s) {
        return s.isEmpty() ? "" : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return up1stLetter(target);
    }

    public FFLetterUp(String args) {
        super(args);
        this.target = args;
    }

}
