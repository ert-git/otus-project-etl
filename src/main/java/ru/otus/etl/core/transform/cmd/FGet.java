package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FGet extends BaseCmd implements Cmd {

    private String arg;

    public String exec(Extractable src) throws EtlTransformException {
        if (!src.containsKey(arg)) {
            throw new EtlTransformException("Поле '" + arg + "' не найдено в исходном документе. Проверьте разделители и формат.");
        }
        return src.get(arg);
    }

    @Override
    public void setArgs(String arg) {
        this.arg = arg;
    }

}
