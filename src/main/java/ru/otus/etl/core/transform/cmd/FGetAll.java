package ru.otus.etl.core.transform.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
@Slf4j
public class FGetAll extends CmdInterpreter {

    private static final String message = "Недостаточно аргументов. Синтаксис команды: 'разделитель', префикс ключа. Например, getAll(',', Phone)";
    private final String delim;
    private final String key;

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return String.join(delim, src.getList(key));
    }

    private static final Pattern ARGS = Pattern.compile("'(.+)', ?(.*)");

    public FGetAll(String arg) throws EtlTransformException {
        //"';', key"
        super(arg);
        Matcher matcher = ARGS.matcher(arg);
        if (matcher.find()) {
            delim = matcher.group(1);
            this.key = matcher.group(2);
        } else {
            log.error("FGetAll: failed for {}", arg);
            throw new EtlTransformException(message);
        }            
    }

}
