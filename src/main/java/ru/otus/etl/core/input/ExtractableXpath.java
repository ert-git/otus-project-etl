package ru.otus.etl.core.input;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtractableXpath implements Extractable {

    private final XPath xPath;
    private final Document xmlDocument;

    public ExtractableXpath(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
        xPath = XPathFactory.newInstance().newXPath();
    }

    @Override
    public String get(String expression) {
        try {
            Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
            return node != null ? node.getTextContent() : null;
        } catch (Exception e) {
            log.error("get: failed for expression={}", expression, e);
        }
        return null;
    }

    @Override
    public boolean containsKey(String expression) {
        try {
            return (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) != null;
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
            } else  {
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
}
