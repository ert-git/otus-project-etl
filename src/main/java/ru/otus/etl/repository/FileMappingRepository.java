package ru.otus.etl.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.utils.Transcriptor;

@Slf4j
//@Repository
public class FileMappingRepository implements MappingRepository {
    public static final String MAP_EXTENTION = ".map";
    private final String mapFolder;
    private final String filesFolder;

    public FileMappingRepository() {
        this("/tmp");
    }

    public FileMappingRepository(String karafHome) {
        mapFolder = karafHome + "/etc/mappings/";
        try {
            Files.createDirectories(new File(mapFolder).toPath());
            log.info("mapping folder: {}", mapFolder);
        } catch (IOException e) {
            log.error("failed to create dirs {}", mapFolder, e);
        }
        filesFolder = karafHome + "/data/mappings/";
        try {
            Files.createDirectories(new File(filesFolder).toPath());
            log.info("files folder: {}", filesFolder);
        } catch (IOException e) {
            log.error("failed to create dirs {}", filesFolder, e);
        }
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private ObjectMapper jsonMapper = new ObjectMapper();

    private Path getMappingPath(String id) {
        return new File(mapFolder + (id.endsWith(MAP_EXTENTION) ? id : id + MAP_EXTENTION)).toPath();
    }

    @Override
    public Optional<Mapping> findById(String id) {
        try (InputStream in = Files.newInputStream(getMappingPath(id))) {
            return Optional.of(jsonMapper.readValue(in, Mapping.class));
        } catch (Exception e) {
            log.error("failed to get mapping id={}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Mapping save(Mapping mapping) {
        if (mapping.getId() == null) {
            mapping.setId(Transcriptor.translit(mapping.getName()));
        }
        Path mappingPath = getMappingPath(mapping.getId());
        log.info("save: path={}, {}", mappingPath, mapping);
        try (OutputStream out = Files.newOutputStream(mappingPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            jsonMapper.writeValue(out, mapping);
        } catch (Exception e) {
            log.error("failed to save mapping {} in {}", mappingPath, mapping, e);
            return null;
        }
        return mapping;
    }

    @Override
    public List<Mapping> findAll() {
        try {
            return Files.list(new File(mapFolder).toPath())
                    .filter(f -> f.getFileName().toFile().getName().endsWith(MAP_EXTENTION))
                    .map(f -> findById(f.getFileName().toFile().getName()).get())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getAllMappings: failed", e);
            return null;
        }
    }

    @Override
    public void delete(Mapping m) {
        Path mappingPath = getMappingPath(m.getId());
        log.info("delete: path={}, m={}", mappingPath, m.getId());
        mappingPath.toFile().delete();
    }

    @Override
    public <S extends Mapping> Iterable<S> saveAll(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean existsById(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<Mapping> findAllById(Iterable<String> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long count() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void deleteById(String id) {
        Path mappingPath = getMappingPath(id);
        log.info("delete: path={}, m={}", mappingPath, id);
        mappingPath.toFile().delete();
    }

    @Override
    public void deleteAll(Iterable<? extends Mapping> entities) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteAll() {
        // TODO Auto-generated method stub
        
    }

}
