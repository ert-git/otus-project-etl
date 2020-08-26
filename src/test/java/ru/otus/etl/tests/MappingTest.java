package ru.otus.etl.tests;

import java.io.InputStream;
import java.util.List;

import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.converters.ExcelConverter;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.core.model.Mapping.ResultType;
import ru.otus.etl.core.transform.output.EtlOutput;
import ru.otus.etl.core.transform.output.JsonOutput;
import ru.otus.etl.core.transform.output.OutputFactory;

public class MappingTest {

    public static void main(String[] args) throws Exception {
        scsv();
    }
    private static void scsv() throws Exception {
        Mapping m = new Mapping();
        m.setName("test");
        m.setFirstRowAsHeader(true);
        m.setSourceUrl("file:///temp/list.xls");
        m.setResultType(ResultType.SCSV);

        m.getRules().add(new Rule("Identifier = $Идентификатор"));
        m.getRules().add(new Rule("Info[0].Description = concat(Адрес размещения: , toLower($Название))"));
        m.getRules().add(new Rule("Info[0].Resource[0].ResourceDesc = const{RTSPArchive}"));
        // m.getRules().add(new Rule("Alert.Info[0].Alert.Resource = toLower($Название)"));

        ExcelConverter c = new ExcelConverter();
        List<Extractable> srcDataRecs = c.convert(m, true);
        EtlOutput output = OutputFactory.get(m, srcDataRecs);
        InputStream res = output.getOutput();
        byte[] b = new byte[res.available()];
        res.read(b);
        System.out.println(new String(b));
    }
    
    
    private static void props() throws Exception {
        Mapping m = new Mapping();
        m.setName("test");
        m.setFirstRowAsHeader(true);
        m.setSourceUrl("file:///temp/list.xls");
        m.setResultType(ResultType.PROPERTIES);

        m.getRules().add(new Rule("Identifier = $Идентификатор"));
        m.getRules().add(new Rule("Info[0].Description = concat(Адрес размещения: , toLower($Название))"));
        m.getRules().add(new Rule("Info[0].Resource[0].ResourceDesc = const{RTSPArchive}"));
        // m.getRules().add(new Rule("Alert.Info[0].Alert.Resource = toLower($Название)"));

        ExcelConverter c = new ExcelConverter();
        List<Extractable> srcDataRecs = c.convert(m, true);
        EtlOutput output = OutputFactory.get(m, srcDataRecs);
        InputStream res = output.getOutput();
        byte[] b = new byte[res.available()];
        res.read(b);
        System.out.println(new String(b));
    }
    
    private static void json() throws Exception {
        Mapping m = new Mapping();
        m.setName("test");
        m.setFirstRowAsHeader(true);
        m.setSourceUrl("file:///temp/list.xls");
        m.setResultType(ResultType.JSON);

        m.getRules().add(new Rule("Identifier = $Идентификатор"));
        m.getRules().add(new Rule("Info[0].Description = concat(Адрес размещения: , toLower($Название))"));
        m.getRules().add(new Rule("Info[0].Resource[0].ResourceDesc = const{RTSPArchive}"));
        // m.getRules().add(new Rule("Alert.Info[0].Alert.Resource = toLower($Название)"));

        ExcelConverter c = new ExcelConverter();
        List<Extractable> srcDataRecs = c.convert(m, true);
        EtlOutput output = OutputFactory.get(m, srcDataRecs);
        ((JsonOutput) output).setIndentFactor(2);
        InputStream res = output.getOutput();
        byte[] b = new byte[res.available()];
        res.read(b);
        System.out.println(new String(b));
    }
}
