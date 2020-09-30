package ru.otus.etl.web;

import ru.otus.etl.core.EtlException;

public class MappingNotFoundException extends EtlException {

    public MappingNotFoundException(String string) {
        super(string);
    }
}