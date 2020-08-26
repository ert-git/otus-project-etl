package ru.otus.etl.core.input.converters;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.otus.etl.core.EtlException;

public class ConvertFailedException extends EtlException {

    public ConvertFailedException(IOException e) {
        super("Не удалось выполнить преобразование: " + e.getMessage());
    }

    public ConvertFailedException(Exception e) {
        super("Не удалось выполнить преобразование: непредвиденная ошибка");
    }
}
