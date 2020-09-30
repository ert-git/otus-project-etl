package ru.otus.etl.repository;

import org.springframework.data.repository.CrudRepository;

import ru.otus.etl.core.model.Mapping;

public interface MappingRepository extends CrudRepository<Mapping, String> {

}
