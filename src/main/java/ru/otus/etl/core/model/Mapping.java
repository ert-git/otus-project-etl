package ru.otus.etl.core.model;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.utils.Transcriptor;

@Entity
@Slf4j
@Getter
@Setter
@ToString
public class Mapping {
    private static final String[] EMPTY_ARR = new String[0];

    public static final String FILES_FOLDER = "/tmp";

    private static final Pattern FIELD = Pattern.compile("\\$([\\wА-я #/@0-9]+)");
    private static final Pattern GETTER = Pattern.compile("get\\{([\\wА-я #/@0-9]+)\\}");

    public static enum ResultType {
        PROPERTIES, JSON, CSV, SCSV
    }

    public static enum DestType {
        JMS, FS, NONE
    }

    public static final String DEF_ENCODING = "utf-8";

    public Mapping() {
        encoding = DEF_ENCODING;
    }

    @Id
    private String id;
    private String name;
    @OneToMany(mappedBy = "mapping", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Rule> rules = new ArrayList<>();
    private String delimiter;
    private String xmlRoot;
    private String unmappedFieldName;
    private String destUrl;
    private String sourceUrl;
    private String sourceFilename;
    private DestType destType;
    private ResultType resultType;
    private String encoding;
    private boolean firstRowAsHeader;
    private String headers;

    @JsonIgnore
    public String[] getHeadersList() {
        return headers != null && !headers.isEmpty() ? headers.split(",") : EMPTY_ARR;
    }

    @JsonIgnore
    public String getResultFilename() {
        return id + "." + resultType.toString().toLowerCase();
    }

    public List<Rule> getRules() {
        if (rules == null) {
            rules = new ArrayList<>();
        }
        return rules;
    }

    @JsonIgnore
    public Character getDelim() {
        if (delimiter != null && !delimiter.trim().isEmpty())
            return Character.valueOf(delimiter.charAt(0));
        else
            return null;
    }

    @JsonIgnore
    public Charset getCharset() {
        if (encoding != null && !encoding.trim().isEmpty()) {
            return Charset.forName(encoding);
        } else {
            return Charset.forName(DEF_ENCODING);
        }
    }

    @JsonIgnore
    public boolean isNew() {
        return id == null || id.trim().isEmpty();
    }

    @JsonIgnore
    public void genId() {
        id = Transcriptor.translit(name).replaceAll("\\W", "_");
    }

    @JsonIgnore
    public List<Rule> getNormalizedRules() {
        return rules.stream().map(rule -> {
            rule.setLeft(rule.getLeft().trim());
            String right = rule.getRight().replaceAll("\\(", "{").replaceAll("\\)", "}");
            StringBuffer sb = new StringBuffer(right);
            Matcher fieldMatcher = FIELD.matcher(right);
            while (fieldMatcher.find()) {
                sb = new StringBuffer();
                String name = fieldMatcher.group(1);
                fieldMatcher = fieldMatcher.appendReplacement(sb, "get{" + name + "}");
                fieldMatcher.appendTail(sb);
                fieldMatcher = FIELD.matcher(sb.toString());
            }
            rule.setRight(sb.toString());
            log.trace("getNormalizedRules: {} -> {}", rule, right);
            return rule;
        }).collect(Collectors.toList());
    }

    @JsonIgnore
    public Set<String> getMappedFields(List<Rule> normalRules) {
        Set<String> mappedFields = new HashSet<>();
        normalRules.forEach(rule -> {
            Matcher getMatcher = GETTER.matcher(rule.getRight());
            while (getMatcher.find()) {
                mappedFields.add(getMatcher.group(1));
            }
        });
        return mappedFields;
    }

}
