package ru.otus.etl.core.transform.output;

public class OutputTypeNotSupportedException extends RuntimeException {

    public OutputTypeNotSupportedException(String message) {
        super(message);
    }


}
