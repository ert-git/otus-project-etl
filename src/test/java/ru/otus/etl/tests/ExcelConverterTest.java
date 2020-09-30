package ru.otus.etl.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.converters.CsvConverter;
import ru.otus.etl.core.input.converters.ExcelConverter;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.core.model.Mapping.ResultType;
import ru.otus.etl.core.transform.output.EtlOutput;
import ru.otus.etl.core.transform.output.OutputFactory;

public class ExcelConverterTest {
    private static final String[][] data = new String[3][3];

    @BeforeAll
    public static void before() {
        data[0] = new String[] { "1", "1:1-1-1", "Крас.Путь-ул.Фрунзе" };
        data[1] = new String[] { "2", "1:1-2-1", "Крас.Путь-ул.Рабиновича" };
        data[2] = new String[] { "3", "1:1-3-1", "Крас.Путь-ул.Кемеровская" };
    }

    @Test
    public void test_extractable() throws EtlException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("list.xls");
        ExcelConverter b = new ExcelConverter();
        Mapping m = new Mapping();
        m.setSourceUrl(url.toString());
        m.setFirstRowAsHeader(true);
        m.setDelimiter(";");
        List<Extractable> list = b.convert(m, false);
        assertEquals(3, list.size());
        for (int i = 1; i < list.size(); i++) {
            Extractable rec = list.get(i);
            assertEquals(data[i][0], rec.get("Код"));
            assertEquals(data[i][1], rec.get("Идентификатор"));
            assertEquals(data[i][2], rec.get("Название"));
        }
    }
    
    @Test
    public void test_json_out() throws Exception {
        String url = Thread.currentThread().getContextClassLoader().getResource("list.xls").toString();

        Mapping m = new Mapping();
        m.setName("test");
        m.setFirstRowAsHeader(true);
        m.setSourceUrl(url);
        m.setResultType(ResultType.JSON);
        m.setDelimiter(";");

        m.getRules().add(new Rule("Identifier = $Идентификатор"));
        m.getRules().add(new Rule("Info[0].Description = concat(Адрес размещения: , toLower($Название))"));
        m.getRules().add(new Rule("Info[0].Resource[0].ResourceDesc = const{RTSPArchive}"));

        ExcelConverter c = new ExcelConverter();
        List<Extractable> srcDataRecs = c.convert(m, true);
        EtlOutput output = OutputFactory.get(m, srcDataRecs);
        InputStream res = output.getOutput();
        byte[] b = new byte[res.available()];
        res.read(b);
        
        String result = "{\"Identifier\":\"1:1-1-1\",\"Info\":[{\"Description\":\"Адрес размещения:  крас.путь-ул.фрунзе\",\"Resource\":[{\"ResourceDesc\":\"RTSPArchive\"}]}]}";
        assertEquals(result, new String(b));
    }

    
    public static void main(String[] args) throws Exception {
        ExcelConverter b = new ExcelConverter();
        Mapping m = new Mapping();
        m.setSourceUrl("file:///temp/list.xlsx");
        m.setFirstRowAsHeader(true);
        m.setDelimiter(";");
        List<Extractable> list = b.convert(m, false);
        list.forEach(System.out::println);
    }
}
