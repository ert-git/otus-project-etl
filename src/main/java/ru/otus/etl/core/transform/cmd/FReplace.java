package ru.otus.etl.core.transform.cmd;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
@Slf4j
public class FReplace extends CmdInterpreter {
    private static final String message = "Недостаточно аргументов. Синтаксис команды: где_заменить, что_заменить, чем_заменить. Например, replace(кирпич, пич, ка)";

    private final String what;
    private final String where;
    private final String replacement;

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return where.replaceAll(what, replacement);
    }

    public FReplace(String arg) throws EtlTransformException {
        super(arg);
        try {
            String[] args = arg.split(",");
            where = args[0].trim();
            what = args[1].trim();
            replacement = args[2].trim();
        } catch (Exception e) {
            log.error("ctor: failed for {}", arg, e);
            throw new EtlTransformException(message);
        }
    }

}
