package ru.otus.etl.core.input.converters;

import ru.otus.etl.core.EtlException;

public class ConverterNotFoundException extends EtlException {

    public ConverterNotFoundException(String message) {
        super(message);
    }

    public ConverterNotFoundException(Throwable cause) {
        super(cause);
    }

}
