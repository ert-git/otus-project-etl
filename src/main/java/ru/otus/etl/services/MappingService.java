package ru.otus.etl.services;

import java.io.InputStream;

import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.model.Mapping;

public interface MappingService {

    void saveMapping(Mapping mapping) throws EtlException;

    Mapping getMapping(String id);

    Iterable<Mapping> getAllMappings();

    void delete(String id) throws EtlException;

    InputStream applyMapping(Mapping mapping, boolean checkOnly) throws EtlException;

    void sendMapping(Mapping mapping) throws EtlException;

}