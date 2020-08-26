package ru.otus.etl.services;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.converters.ConverterFactory;
import ru.otus.etl.core.input.converters.EtlConverter;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.core.model.Mapping.DestType;
import ru.otus.etl.core.transform.output.EtlOutput;
import ru.otus.etl.core.transform.output.OutputFactory;
import ru.otus.etl.jms.JmsPublisher;
import ru.otus.etl.repository.MappingRepository;

@Slf4j
@Service
public class MappingServiceImpl implements MappingService {
    private final MappingRepository repository;
    private final JmsPublisher publisher;
    private final FileService fileService;

    public MappingServiceImpl(FileService fileService, MappingRepository repository, JmsPublisher publisher) {
        this.fileService = fileService;
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public Mapping getMapping(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void saveMapping(Mapping mapping) throws EtlException {
        try {
            repository.save(mapping);
        } catch (Exception e) {
            log.error("saveMapping: failed for {}", mapping, e);
            throw new EtlException("Не удалось сохранить отображение");
        }
    }

    @Override
    public Iterable<Mapping> getAllMappings() {
        return repository.findAll();
    }

    @Override
    public void delete(String id) throws MappingEditException {
        repository.deleteById(id);
    }

    @Override
    public InputStream applyMapping(Mapping mapping, boolean checkOnly)
            throws EtlException {
        EtlConverter c = ConverterFactory.get(mapping);
        List<Extractable> srcDataRecs = c.convert(mapping, checkOnly);
        EtlOutput output = OutputFactory.get(mapping, srcDataRecs);
        return output.getOutput();
    }

    @Override
    public void sendMapping(Mapping mapping) throws EtlException {
        if (mapping.getDestType() == DestType.NONE) {
            return;
        }
        InputStream res = applyMapping(mapping, false);
        byte[] b = stream2bytes(res);
        switch (mapping.getDestType()) {
        case JMS:
            publisher.sendMessage(mapping.getDestUrl(), new String(b));
            break;
        case FS:
            fileService.save(b, mapping.getDestUrl());
            break;
        default:
            break;
        }
    }

    private byte[] stream2bytes(InputStream res) throws EtlException {
        byte[] b;
        try {
            b = new byte[res.available()];
            res.read(b);
        } catch (Exception e) {
            log.error("stream2bytes: failed to get output", e);
            throw new EtlException("Внутренняя ошибка");
        }
        return b;
    }
}
