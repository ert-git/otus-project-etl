package ru.otus.etl.core.transform.cmd;

import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

public interface Cmd {

    String exec(Extractable src) throws EtlTransformException;

}