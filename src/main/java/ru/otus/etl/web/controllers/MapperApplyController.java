package ru.otus.etl.web.controllers;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.services.MappingService;
import ru.otus.etl.web.MappingNotFoundException;

@RestController
@RequestMapping("/rest/mapping/apply")
@Slf4j
public class MapperApplyController {

    private final MappingService mappingService;

    public MapperApplyController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @GetMapping(value = "check/{id}")
    public String check(@PathVariable("id") String id) throws EtlException {
        Mapping mapping = getMapping(id);
        try {
            InputStream res = mappingService.applyMapping(mapping, true);
            byte[] b = new byte[res.available()];
            res.read(b);
            return new String(b);
        } catch (IOException e) {
            log.error("check: failed for id={}", id, e);
            throw new EtlException();
        }
    }

    @GetMapping(value = "send/{id}")
    public String send(@PathVariable("id") String id) throws EtlException {
        Mapping mapping = getMapping(id);
        mappingService.sendMapping(mapping);
        return "Данные отправлены в " + mapping.getDestUrl();
    }

    @GetMapping(value = "download/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) throws EtlException {
        Mapping mapping = getMapping(id);
        try {
            InputStream res = mappingService.applyMapping(mapping, false);
            byte[] b = new byte[res.available()];
            res.read(b);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + mapping.getResultFilename() + "\"").body(b);
        } catch (IOException e) {
            log.error("download: failed for id={}", id, e);
            throw new EtlException();
        }
    }

    private Mapping getMapping(String id) throws MappingNotFoundException {
        Mapping mapping = mappingService.getMapping(id);
        if (mapping == null) {
            log.error("download: mapping not found for id={}", id);
            throw new MappingNotFoundException("Не найдено отображение " + id);
        }
        return mapping;
    }

}
