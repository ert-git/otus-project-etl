package ru.otus.etl.core.transform.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
@Slf4j
public class FJoin extends CmdInterpreter {
    private static final Pattern ARGS = Pattern.compile("'(.+)'(, ?(.*))+");
    private static final String message = "Недостаточно аргументов. Синтаксис команды: 'разделитель', префикс ключа. Например, join(',', аргумент1, фргумент2, ...)";

    private final String delimiter;
    private final String[] els;

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return String.join(delimiter, els);
    }

    public FJoin(String arg) throws EtlTransformException {
        super(arg);
        try {
            Matcher matcher = ARGS.matcher(arg);
            if (matcher.find()) {
                delimiter = matcher.group(1);
                String[] args = matcher.group(3).trim().split(",");
                els = new String[args.length - 1];
                for (int i = 1; i < args.length; i++) {
                    els[i - 1] = args[i].trim();
                }
            } else {
                throw new EtlTransformException(message);
            }
        } catch (Exception e) {
            log.error("ctor: failed for {}", arg, e);
            throw new EtlTransformException(message);
        }
    }

}
