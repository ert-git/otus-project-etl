package ru.otus.etl.core.transform.cmd;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FNow extends BaseCmd implements Cmd {

    @Override
    public void setArgs(String args) {
    }

    @Override
    public String exec(Extractable src) throws EtlTransformException {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
