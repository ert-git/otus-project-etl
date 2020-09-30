package ru.otus.etl.web.controllers;

import java.util.Collection;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.converters.ConverterFactory;
import ru.otus.etl.core.input.converters.EtlConverter;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.services.FileService;
import ru.otus.etl.services.MappingService;

@Slf4j
@Controller
@RequestMapping("/uploader")
public class FileUploadController {


    private final MappingService mappingService;

    private final FileService fileService;

    public FileUploadController(MappingService mappingService, FileService fileService) {
        this.mappingService = mappingService;
        this.fileService = fileService;
    }

    @AllArgsConstructor
    @Setter
    @Getter
    private static class UploadResponse {
        Collection<String> headers;
        Mapping mapping;
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<UploadResponse> upload(@RequestParam("customFile") MultipartFile file, @RequestParam("id") String id) {
        Mapping mapping = mappingService.getMapping(id);
        if (mapping == null) {
            log.error("upload: no mapping for id={}", id);
            return ResponseEntity.status(500).build();
        }
        String filename = file.getOriginalFilename();
        try {
            String url = fileService.save(file.getInputStream(), filename);
            mapping.setSourceUrl(url);
            mapping.setSourceFilename(filename);
            mappingService.saveMapping(mapping);
            EtlConverter conv = ConverterFactory.get(mapping);
            Map<Integer, String> headers = conv.getHeaders(mapping);
            UploadResponse r = new UploadResponse(headers.values(), mapping);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            log.error("upload: failed for {}", file, e);
            return ResponseEntity.status(500).build();
        }
    }


}
