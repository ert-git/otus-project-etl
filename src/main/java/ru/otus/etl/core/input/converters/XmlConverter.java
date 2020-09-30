package ru.otus.etl.core.input.converters;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.input.ExtractableXpath;
import ru.otus.etl.core.input.converters.EtlConverter;
import ru.otus.etl.core.model.Mapping;
import ru.otus.etl.core.model.Rule;
import ru.otus.etl.core.model.Mapping.DestType;
import ru.otus.etl.core.model.Mapping.ResultType;
import ru.otus.etl.core.transform.output.EtlOutput;
import ru.otus.etl.core.transform.output.JsonOutput;
import ru.otus.etl.core.transform.output.OutputFactory;

@Slf4j
public class XmlConverter implements EtlConverter {

    private static boolean isLeaf(Node node) {
        if (!node.hasChildNodes()) {
            return true;
        }
        NodeList childNodes = node.getChildNodes();
        int length = childNodes.getLength();
        for (int i = 0; i < length; i++) {
            if (childNodes.item(i).getNodeType() != Node.TEXT_NODE) {
                return false;
            }
        }
        return true;
    }

    private static Set<String> nodes(String parent, NodeList nodeList) throws DOMException, Exception {
        Set<String> set = new HashSet<>();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            // on 1st iteration parent doesn't end with /
            String nodeName = parent.endsWith("/") ? parent + node.getNodeName() : parent;
            if (isLeaf(node) && node.getNodeType() != Node.TEXT_NODE) {
                if (node.getNodeName().contains("#cdata")) {
                    // /OuterSystemsConf/Objects/GuardObject/Description#/GuardObjectInfo/Address
                    nodeName = nodeName.substring(0, nodeName.indexOf("/#"));
                    // /GuardObjectInfo
                    String cdataRoot = ExtractableXpath.getCDATAXml(node.getTextContent()).getFirstChild().getNodeName();
                    NodeList list = ExtractableXpath.getCDATANodeList(cdataRoot, node.getTextContent());
                    // /OuterSystemsConf/Objects/GuardObject/Description#/GuardObjectInfo/
                    set.addAll(nodes(nodeName + "#/" + cdataRoot + "/", list.item(0).getChildNodes()));
                }
                set.add(nodeName);
            } else {
                set.addAll(nodes(nodeName + "/", node.getChildNodes()));
            }
        }
        return set;
    }

    public static void main(String[] args) throws Exception {
        Mapping m = new Mapping();
        m.setSourceUrl("/scripts/esb/git/adapters/adapterstrelets/docs/sensors_oblast.xml");
        m.setXmlRoot("/OuterSystemsConf/Objects/GuardObject");
        // m.setXmlRoot("/OuterSystemsConf/Objects/GuardObject");
        m.getRules().add(new Rule("Address = $Description#/GuardObjectInfo/Address"));
        m.getRules().add(new Rule("Name = $Name"));
        m.getRules().add(new Rule("Id = $@Id"));
        m.getRules().add(new Rule("Contacts = getAll(', ', Description#/GuardObjectInfo/ContactPerson)"));
        m.setResultType(ResultType.JSON);
        XmlConverter c = new XmlConverter();
        List<Extractable> srcDataRecs = c.convert(m, true);
        EtlOutput output = OutputFactory.get(m, srcDataRecs);
        ((JsonOutput) output).setIndentFactor(2);
        InputStream res = output.getOutput();
        byte[] b = new byte[res.available()];
        res.read(b);
        System.out.println(new String(b));

        // c.getHeaders(m);

    }

    private NodeList nodeList;
    private Document xmlDocument;

    @Override
    public List<Extractable> convert(Mapping mapping, boolean checkOnly) throws EtlException {
        List<Extractable> result = new ArrayList<>();
        if (nodeList == null) {
            init(mapping);
        }
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            result.add(new ExtractableXpath(node));
            if (checkOnly) {
                break;
            }
        }
        return result;
    }

    private void init(Mapping mapping) throws EtlException {
        try (FileInputStream fileIS = new FileInputStream(mapping.getSourceUrl().replace("file://", ""))) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            xmlDocument = builder.parse(fileIS);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xmlRoot = Optional.ofNullable(mapping.getXmlRoot()).orElse("/");
            nodeList = (NodeList) xPath.compile(xmlRoot).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (Exception e) {
            log.error("init: failed for {}", mapping, e);
            throw new EtlException("Не удалось загрузить исходный xml документ");
        }
    }

    @Override
    public Map<Integer, String> getHeaders(Mapping mapping) throws EtlException {
        HashMap<Integer, String> headers = new HashMap<>();
        try {
            String[] cachedHeaders = mapping.getHeadersList();
            if (cachedHeaders.length > 0) {
                for (int i = 0; i < cachedHeaders.length; i++) {
                    headers.put(i, cachedHeaders[i]);
                }
                return headers;
            }
            if (nodeList == null) {
                init(mapping);
            }
            String xmlRoot = Optional.ofNullable(mapping.getXmlRoot()).orElse("/");
            Set<String> nodes = nodes(xmlRoot, nodeList);
            int i = 0;
            for (Iterator<String> iterator = nodes.iterator(); iterator.hasNext();) {
                String header = iterator.next();
                header = header.startsWith(xmlRoot) ? header.substring(xmlRoot.length() + 1) : header;
                headers.put(i++, header);
            }
        } catch (Exception e) {
            log.error("getHeaders: failed for {}", mapping, e);
        }
        return headers;
    }
}
