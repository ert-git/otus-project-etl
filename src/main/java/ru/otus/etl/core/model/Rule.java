package ru.otus.etl.core.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@ToString(exclude = "mapping")
@NoArgsConstructor
public class Rule {

    public Rule(String line) {
        String[] split = line.split("=");
        left = split[0];
        right = split[1];
    }

    @Id
    @GeneratedValue
    private long id;
    @Column(name = "left_part")
    private String left;
    @Column(name = "right_part")
    private String right;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Mapping mapping;
}
