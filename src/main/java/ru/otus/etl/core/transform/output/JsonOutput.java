package ru.otus.etl.core.transform.output;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.core.transform.EtlTransformException;
import ru.otus.etl.core.transform.cmd.CmdInterpreter;

@Slf4j
public class JsonOutput implements EtlOutput {
    private static final Pattern ARRAY = Pattern.compile("(\\w+)\\[(\\d?)\\]");
    private static final String ROOT_EL = "root";

    @AllArgsConstructor
    private static class Leaf {
        public final JSONObject node;
        public final String key;
        public final String func;
    }

    private final Set<Leaf> leafs = new HashSet<>();
    private final Set<String> mappedFields;
    private final Map<String, JSONObject> objs = new HashMap<>();
    private final List<Extractable> srcDataRecords;
    private final List<Rule> rules;
    @Setter
    private int indentFactor;
    private final String unmappedFieldName;

    public JsonOutput(List<Extractable> srcDataRecords, List<Rule> rules, Set<String> mappedFields, String unmappedFieldName) {
        this.srcDataRecords = srcDataRecords;
        this.rules = rules;
        this.mappedFields = mappedFields;
        this.unmappedFieldName = unmappedFieldName;
    }

    public InputStream getOutput() throws EtlTransformException {
        JSONObject root = new JSONObject();
        objs.put(ROOT_EL, root);
        for (Rule rule : rules) {
            parseLine(rule);
        }
        objs.keySet().forEach(k -> log.debug("parse: objs map: {} = {}", k, objs.get(k)));
        log.debug("parsing completed: template: {}", root.toString(2));

        log.debug("start to apply funcs for {} objs", srcDataRecords.size());
        StringBuilder buff = new StringBuilder();
        if (srcDataRecords.size() > 1) {
            buff.append("[");
        }
        for (int i = 0, len = srcDataRecords.size(); i < len; i++) {
            Extractable srcDataRec = srcDataRecords.get(i);
            for (Leaf leaf : leafs) {
                long now = System.currentTimeMillis();
                String result = CmdInterpreter.exec(leaf.func, srcDataRec);
                log.trace("{} from {}: {} ms", i, len, (System.currentTimeMillis() - now));
                log.trace("exec func={} for key={}, srcDataRec={} with result={}", leaf.func, leaf.key, srcDataRec, result);
                leaf.node.put(leaf.key, result);
            }
            if (unmappedFieldName != null && !unmappedFieldName.trim().isEmpty()) {
                root.remove(unmappedFieldName);
                JSONObject unmapped = new JSONObject();
                root.put(unmappedFieldName, unmapped);
                for (String srcKey : srcDataRec.keySet()) {
                    if (!mappedFields.contains(srcKey)) {
                        unmapped.put(srcKey, srcDataRec.get(srcKey));
                    }
                }
            }
            buff.append(root.toString(indentFactor));
            if (i < srcDataRecords.size() - 1) {
                buff.append(",");
                if (indentFactor > 0) {
                    buff.append("\r\n");
                }
            }
        }
        if (srcDataRecords.size() > 1) {
            buff.append("]");
        }
        log.info("finish to apply funcs: uff length: {}", buff.length());
        return new ByteArrayInputStream(buff.toString().getBytes());
    }

    private static String getKey(String partName, String[] split, int i) {
        Matcher matcher = ARRAY.matcher(partName);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return partName;
        }
    }

    private void parseLine(Rule rule) throws EtlTransformException {
        // Info[0].Headline = get{FiledName}
        // Info[0].Resource[0].ResourceDesc = const{RTSPArchive}
        log.debug("parseLine: line of {}", rule);
        // add root element
        String[] split = (ROOT_EL + "." + rule.getLeft()).split("\\.");
        // right part
        String func = rule.getRight().trim();
        for (int i = 1; i < split.length; i++) {
            String currPart = split[i];
            String prevPart = split[i - 1];
            String key = getKey(currPart, split, i);
            JSONObject parent = objs.get(prevPart);
            if (currPart.contains("[")) {
                if (!parent.has(key)) {
                    // adds key: []
                    parent.put(key, new JSONArray());
                    log.trace("parseLine: adds key[] for key={}, currPart={}, prevPart={}", key, currPart, prevPart);
                }
                if (!objs.containsKey(currPart)) {
                    // adds key: [{}]
                    JSONObject current = new JSONObject();
                    objs.put(currPart, current);
                    parent.append(key, current);
                    log.trace("parseLine: adds key[{}] for key={}, currPart={}, prevPart={}", key, currPart, prevPart);
                }
            } else {
                if (i < split.length - 1) {
                    // new node
                    JSONObject current = objs.computeIfAbsent(currPart, k -> new JSONObject());
                    parent.put(key, current);
                    log.trace("parseLine: adds node for key={}, currPart={}, prevPart={}", key, currPart, prevPart);
                } else {
                    // last leaf
                    leafs.add(new Leaf(parent, key, func));
                    log.debug("parseLine: adds leaf for key={}, currPart={}, prevPart={}, func={}", key, currPart, prevPart, func);
                }
            }
        }
    }

}
