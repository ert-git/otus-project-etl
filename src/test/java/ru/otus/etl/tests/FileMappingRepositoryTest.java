package ru.otus.etl.tests;

import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.repository.FileMappingRepository;

public class FileMappingRepositoryTest {

    public static void main(String[] args) throws Exception {
        Mapping m2 = new Mapping();
        m2.setSourceUrl("file:///temp/list.csv");
        m2.setFirstRowAsHeader(true);
        m2.setDelimiter(";");
        m2.setName("test2");

        Mapping m1 = new Mapping();
        m1.setSourceUrl("file:///temp/list.xls");
        m1.setFirstRowAsHeader(true);
        m1.setName("test1");

        FileMappingRepository mng = new FileMappingRepository("/tmp");
        mng.save(m1);
        mng.save(m2);

        mng.findAll().forEach(System.out::println);
    }

}
