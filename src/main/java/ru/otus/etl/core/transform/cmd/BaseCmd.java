package ru.otus.etl.core.transform.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@Slf4j
public abstract class BaseCmd implements Cmd {

    public static String fetchArgs(String cmd) {
        return cmd.substring(cmd.indexOf('{') + 1, cmd.lastIndexOf('}')).trim();
    }

    // get{const{...}}
    private static Pattern CMD_SIGNATURE = Pattern.compile("(\\w+\\{[^\\{\\}]+\\})");

    private static final String CMD_PACK = BaseCmd.class.getPackage().getName() + ".";

    public static Cmd getCmd(String cmdStr) throws EtlTransformException {
        try {
            return (Cmd) Class.forName(CMD_PACK + "F" + up1stLetter(cmdStr)).newInstance();
        } catch (Exception e) {
            log.error("getCmd: failed for {}", cmdStr, e);
            throw new EtlTransformException("Команда " + cmdStr + " не поддерживается. Проверьте названия команд по справочнику.");
        }
    }

    public static String fetchCmdName(String s) {
        return s.substring(0, s.indexOf('{')).trim();
    }

    public static String lower1stLetter(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    public static String up1stLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String exec(String func, Extractable src) throws EtlTransformException {
        Cmd lastCmd = null;
        Matcher m = CMD_SIGNATURE.matcher(func);
        while (m.find()) {
            StringBuffer sb = new StringBuffer();
            // get{smth} / const{smth}
            String singleCmdStr = m.group(1);
            // get / const
            Cmd cmd = getCmd(fetchCmdName(singleCmdStr));
            // smth
            cmd.setArgs(fetchArgs(singleCmdStr));
            log.debug("exec: cmdStr={}, cmd={}", singleCmdStr, cmd);
            String result = cmd.exec(src);
            if (result == null) {
                throw new EtlTransformException("Ошибка в команде " + singleCmdStr + ". Проверьте синтаксис.");
            }

            log.debug("exec: result={}", result);
            lastCmd = cmd;
            m = m.appendReplacement(sb, result);
            m.appendTail(sb);
            // fetch next (wrapping) cmd str
            m = CMD_SIGNATURE.matcher(sb.toString());
        }

        log.debug("exec: lastCmd={}", lastCmd);
        return lastCmd.exec(src);
    }

}
