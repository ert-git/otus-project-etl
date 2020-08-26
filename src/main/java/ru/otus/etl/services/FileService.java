package ru.otus.etl.services;

import java.io.InputStream;

import ru.otus.etl.core.EtlException;

public interface FileService {

    String save(InputStream in, String filename) throws EtlException;

    void save(byte[] bytes, String filename) throws EtlException;

}