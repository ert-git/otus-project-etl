package ru.otus.etl.core.transform.cmd;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.ToString;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.transform.EtlTransformException;

@ToString
public class FReplace extends BaseCmd implements Cmd {

    private List<String> args;

    public String exec(Extractable src) throws EtlTransformException {
        return args.get(0).replaceAll(args.get(1), args.get(2));
    }

    @Override
    public void setArgs(String args) {
        this.args = Arrays.stream(args.split(",")).map(el -> el.trim()).collect(Collectors.toList());
    }

}
