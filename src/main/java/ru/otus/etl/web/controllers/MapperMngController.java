package ru.otus.etl.web.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.converters.ConverterFactory;
import ru.otus.etl.core.input.converters.EtlConverter;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.services.MappingService;
import ru.otus.etl.web.MappingNotFoundException;

@RestController
@RequestMapping("/rest/mapping/mng")
@Slf4j
public class MapperMngController {

    private final MappingService mappingService;

    public MapperMngController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @Setter
    @Getter
    private static class MappingResponse {
        Mapping mapping;
        Collection<String> rightValues = new ArrayList<>();
        Collection<String> leftValues = new ArrayList<>();
    }

    @GetMapping
    public Iterable<Mapping> list() {
        return mappingService.getAllMappings();
    }

    @GetMapping(value = "/{id}")
    public MappingResponse get(@PathVariable("id") String id) throws EtlException {
        Mapping mapping = getMapping(id);
        MappingResponse r = new MappingResponse();
        r.mapping = mapping;
        try {
            EtlConverter conv = ConverterFactory.get(mapping);
            r.rightValues = conv.getHeaders(mapping).values();
        } catch (Exception e) {
            log.warn("get: {} for id={}", e.toString(), id);
        }
        return r;
    }

    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable("id") String id) throws EtlException {
        getMapping(id);
        mappingService.delete(id);
    }

    @GetMapping(value = "/headers/{id}")
    public Map<Integer, String> listHeaders(@PathVariable("id") String id) throws EtlException {
        Mapping mapping = getMapping(id);
        EtlConverter conv = ConverterFactory.get(mapping);
        return conv.getHeaders(mapping);

    }

    @PostMapping(consumes = "application/json")
    public Mapping saveMapping(@RequestBody Mapping mapping) throws EtlException {
        log.info("saveMapping: id={}, new={}, {}", mapping.getId(), mapping.isNew(), mapping);
        if (!mapping.isNew()) {
            Mapping old = getMapping(mapping.getId());
            log.info("saveMapping: not new mapping {} -> get {} rules from previous", mapping, old.getRules().size());
            mapping.setRules(old.getRules());
            mapping.getRules().forEach(r -> r.setMapping(mapping));
        } else {
            mapping.genId();
            log.info("saveMapping: new mapping {}", mapping);
        }
        mappingService.saveMapping(mapping);
        return mappingService.getMapping(mapping.getId());
    }

    @PostMapping(value = "/rules/{id}")
    public Mapping saveRules(@PathVariable("id") String id, @RequestBody Rule[] rules) throws EtlException {
        Mapping mapping = getMapping(id);
        mapping.setRules(new ArrayList<>(Arrays.asList(rules)));
        mappingService.saveMapping(mapping);
        return mappingService.getMapping(mapping.getId());
    }

    private Mapping getMapping(String id) throws MappingNotFoundException {
        Mapping mapping = mappingService.getMapping(id);
        if (mapping == null) {
            log.error("getMapping: mapping not found for id={}", id);
            throw new MappingNotFoundException("Не найдено отображение " + id);
        }
        return mapping;
    }

}
