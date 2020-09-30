package ru.otus.etl.core.input.converters;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.ExtractableMap;
import ru.otus.etl.core.model.Mapping;

@Slf4j
public class ExcelConverter implements EtlConverter {

    public List<Extractable> convert(Mapping mapping, boolean checkOnly) throws ConvertFailedException {
        List<Extractable> list = new ArrayList<>();
        Workbook workbook = null;
        String url = mapping.getSourceUrl();
        log.info("start converting url={}", url);
        try (InputStream inputStream = new URL(url).openStream()) {
            if (url.toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            // int totalRowsCount = firstSheet.getPhysicalNumberOfRows();

            Map<Integer, String> headers = new HashMap<>();
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                if (nextRow.getRowNum() == 0) {
                    headers = parseHeaders(mapping, nextRow);
                    if (mapping.isFirstRowAsHeader()) {
                        continue;
                    }
                }
                ExtractableMap props = new ExtractableMap();
                list.add(props);
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell nextCell = cellIterator.next();
                    int columnIndex = nextCell.getColumnIndex();
                    props.put(headers.get(columnIndex), getCellValue(nextCell));
                }
                if (checkOnly && !list.isEmpty()) {
                    return list;
                }
            }
        } catch (Exception e) {
            log.error("convert failed for {}", mapping, e);
            throw new ConvertFailedException(e);
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
            }
        }
        return list;
    }

    private Map<Integer, String> parseHeaders(Mapping mapping, Row nextRow) {
        Map<Integer, String> headers = new TreeMap<>();
        Iterator<Cell> cellIterator = nextRow.cellIterator();
        while (cellIterator.hasNext()) {
            Cell nextCell = cellIterator.next();
            int columnIndex = nextCell.getColumnIndex();
            if (mapping.isFirstRowAsHeader()) {
                headers.put(columnIndex, getCellValue(nextCell));
            } else {
                headers.put(columnIndex, COLUMN + columnIndex);
            }
        }
        log.info("parseHeaders: headers={}", headers);
        return headers;
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue();
        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        case NUMERIC:
            return String.valueOf(cell.getNumericCellValue());
        }
        return null;
    }

    @Override
    public Map<Integer, String> getHeaders(Mapping mapping) throws ConvertFailedException {
        Map<Integer, String> headers = new TreeMap<>();
        Workbook workbook = null;
        try (InputStream inputStream = new URL(mapping.getSourceUrl()).openStream()) {
            if (mapping.getSourceUrl().toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                if (nextRow.getRowNum() == 0) {
                    return parseHeaders(mapping, nextRow);
                }
            }
        } catch (Exception e) {
            log.error("conver failed for {}", mapping, e);
            throw new ConvertFailedException(e);
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return headers;
    }
}
