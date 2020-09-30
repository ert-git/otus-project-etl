package ru.otus.etl.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.stereotype.Service;

import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    /** The maximum size allowed for uploaded files (-1L means unlimited). */
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 1024;

    private static final String FILES_FOLDER = "/tmp";

    public String save(InputStream in, String filename) throws EtlException {
        String pathname = FILES_FOLDER + "/" + filename;
        log.info("save: try to save file {}", pathname);
        File file = new File(pathname);
        try (OutputStream out = new FileOutputStream(file)) {
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (Exception e) {
            log.error("save: failed for {}", filename, e);
        }
        log.info("save: saved to {}", pathname);
        return "file://" + pathname;
    }

    public void save(byte[] b, String filepath) throws EtlException {
        if (filepath == null || filepath.isEmpty()) {
            throw new EtlException("Не указан путь записи файла");
        }
        try {
            Files.write(b, new File(filepath));
        } catch (Exception e) {
            log.error("save: failed for {}", filepath, e);
            throw new EtlException("Не удалось записать данные в " + filepath);
        }
    }

}
