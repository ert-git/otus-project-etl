package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FGet extends CmdInterpreter {
    private static final String message = "Недостаточно аргументов. Синтаксис команды: название_поля. Например, $название_поля";

    private final String fieldName;

    @Override
    public String exec(Extractable src) throws EtlTransformException {
//        if (!src.containsKey(fieldName)) {
//            throw new EtlTransformException("Поле '" + fieldName + "' не найдено в исходном документе. Проверьте разделители и формат.");
//        }
        return src.get(fieldName);
    }

    public FGet(String args) throws EtlTransformException {
        super(args);
        if (args.isEmpty()) {
            throw new EtlTransformException(message);
        }
        this.fieldName = args;
    }

}
