package ru.otus.etl.core.input.converters;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.otus.etl.core.EtlException;
import ru.otus.etl.core.input.Extractable;
import ru.otus.etl.core.model.Mapping;

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

    private static Set<String> nodes(String parent, NodeList nodeList) {
        Set<String> set = new HashSet<>();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
//            System.out.println(node.getNodeType() + " " + parent + "/" + node.getNodeName());
            if (isLeaf(node) && node.getNodeType() != Node.TEXT_NODE) {
                set.add(parent + node.getNodeName());
            } else  {
                set.addAll(nodes(parent + node.getNodeName() + "/", node.getChildNodes()));
            }
        }
        return set;
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fileIS = new FileInputStream("/temp/База Область.xml");
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(fileIS);
        XPath xPath = XPathFactory.newInstance().newXPath();

        String expression = "/OuterSystemsConf/Configuration/Unit/Unit/Unit/Unit/Unit/Unit/Unit/Unit/Name";
        // String expression = "/Tutorials/Tutorial/title";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
//        Set<String> nodes = nodes(expression, nodeList);
//        nodes.forEach(System.out::println);
//        System.out.println(nodes);

        // XPath xPath = XPathFactory.newInstance().newXPath();
        // String expression = "/Tutorials/Tutorial[@tutId=" + "'" + id + "'" + "]";
         Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
         System.out.println(node.getTextContent());
    }

    @Override
    public List<Extractable> convert(Mapping mapping, boolean checkOnly) throws EtlException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Integer, String> getHeaders(Mapping mapping) throws EtlException {
        // TODO Auto-generated method stub
        return null;
    }
}
