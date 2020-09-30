package ru.otus.etl.core.input;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtractableXpath implements Extractable {

    private final XPath xPath;
    private final Node root;

    public ExtractableXpath(Node root) {
        this.root = root;
        xPath = XPathFactory.newInstance().newXPath();
    }

    @Override
    public String get(String expression) {
        try {
            // $Description#/GuardObjectInfo/Address
            // $@Id
            String[] keys = expression.split("#");
            String key = keys[0];
            Node node = (Node) xPath.compile(key).evaluate(root, XPathConstants.NODE);
            if (node == null) {
                log.trace("get: no node for expression={}", expression);
                return null;
            }
            if (keys.length > 1) {
                return parseCDATA(keys[1], node.getTextContent());
            } else {
                return node.getTextContent();
            }
        } catch (Exception e) {
            log.error("get: failed for expression={}", expression, e);
        }
        return null;
    }

    private String parseCDATA(String key, String cdata) throws Exception {
        Node node = getCDATANode(key, cdata);
        if (node != null) {
            return node.getTextContent();
        } else {
            log.trace("get: no node for cdataKey={} in cdata={}", key, cdata);
            return null;
        }
    }

    public static Node getCDATANode(String key, String cdata) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return (Node) xPath.compile(key).evaluate(getCDATAXml(cdata), XPathConstants.NODE);
    }

    public static NodeList getCDATANodeList(String key, String cdata) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return (NodeList) xPath.compile(key).evaluate(getCDATAXml(cdata), XPathConstants.NODESET);
    }

    public static Document getCDATAXml(String cdata) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(new InputSource(new StringReader(cdata)));
        return xmlDocument;
    }

    @Override
    public boolean containsKey(String expression) {
        try {
            String[] keys = expression.split("#");
            String key = keys[0];
            String cdataKey = keys.length > 1 ? keys[1] : null;
            Node node = (Node) xPath.compile(key).evaluate(root, XPathConstants.NODE);
            if (node == null) {
                log.trace("containsKey: no node for expression={}", expression);
                return false;
            }
            if (cdataKey != null) {
                return null != getCDATANode(cdataKey, node.getTextContent());
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("containsKey: failed for expression={}", expression, e);
        }
        return false;
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>();
    }

    private static Set<String> keySet(String parent, NodeList nodeList) {
        Set<String> set = new HashSet<>();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            if (isLeaf(node) && node.getNodeType() != Node.TEXT_NODE) {
                set.add(parent + node.getNodeName());
            } else {
                set.addAll(keySet(parent + node.getNodeName() + "/", node.getChildNodes()));
            }
        }
        return set;
    }

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

    @Override
    public List<String> getList(String expression) {
        List<String> list = new ArrayList<>();
        try {
            // $Description#/GuardObjectInfo/Address
            // $@Id
            String[] keys = expression.split("#");
            String key = keys[0];
            NodeList nodes;
            if (keys.length > 1) {
                Node node = (Node) xPath.compile(key).evaluate(root, XPathConstants.NODE);
                if (node == null) {
                    log.trace("getList: no node for expression={}", expression);
                    return null;
                }
                nodes = getCDATANodeList(keys[1], node.getTextContent());
            } else {
                nodes = (NodeList) xPath.compile(key).evaluate(root, XPathConstants.NODESET);
            }
            if (nodes == null) {
                log.trace("getList: no node for expression={}", expression);
                return list;
            }
            int length = nodes.getLength();
            for (int i = 0; i < length; i++) {
                list.add(nodes.item(i).getTextContent());
            }
        } catch (Exception e) {
            log.error("getList: failed for expression={}", expression, e);
        }
        return list;
    }
}
