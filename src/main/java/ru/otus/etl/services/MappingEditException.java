package ru.otus.etl.services;

import ru.otus.etl.core.EtlException;

public class MappingEditException extends EtlException {

    public MappingEditException(String message) {
        super(message);
    }

    public MappingEditException(Throwable cause) {
        super(cause);
    }

}
