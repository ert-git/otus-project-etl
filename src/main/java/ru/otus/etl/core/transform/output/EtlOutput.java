package ru.otus.etl.core.transform.output;

import java.io.InputStream;

import ru.otus.etl.core.transform.EtlTransformException;

public interface EtlOutput {
    
    InputStream getOutput() throws EtlTransformException;
}
