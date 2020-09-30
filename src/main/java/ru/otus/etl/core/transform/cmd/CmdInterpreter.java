package ru.otus.etl.core.transform.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@Slf4j
public abstract class CmdInterpreter implements Cmd {
    
    public CmdInterpreter(String args) {
        
    }

    // get{const{...}}
    private static final Pattern CMD_SIGNATURE = Pattern.compile("(\\w+\\{[^\\{\\}]+\\})");

    private static final String CMD_PACK = CmdInterpreter.class.getPackage().getName() + ".";

    public static Cmd getCmd(String singleCmdStr) throws EtlTransformException {
        String args = singleCmdStr.substring(singleCmdStr.indexOf('{') + 1, singleCmdStr.lastIndexOf('}')).trim();
        String name = singleCmdStr.substring(0, singleCmdStr.indexOf('{')).trim();
        try {
            return (Cmd) Class.forName(CMD_PACK + "F" + up1stLetter(name)).getConstructor(String.class).newInstance(args);
        } catch (Exception e) {
            log.error("getCmd: failed for name={}, ({})", name, singleCmdStr, e);
            throw new EtlTransformException("Команда " + name + " не поддерживается. Проверьте названия команд по справочнику.");
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
            Cmd cmd = getCmd(singleCmdStr);
            log.debug("exec: cmdStr={}, cmd={}", singleCmdStr, cmd);
            String result = cmd.exec(src);
            if (result == null) {
                log.warn("exec: cmdStr={}, cmd={}, result={}", singleCmdStr, cmd, result);
                result = "";
                //throw new EtlTransformException("Ошибка в команде " + singleCmdStr + ". Проверьте синтаксис.");
            } else {
                log.debug("exec: cmdStr={} result={}", singleCmdStr, result);
            }
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
