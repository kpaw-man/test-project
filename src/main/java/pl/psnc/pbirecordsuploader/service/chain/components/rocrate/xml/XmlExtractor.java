package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;
import org.w3c.dom.Node;

@Slf4j
@Component
public  class XmlExtractor {
    private final DocumentBuilderFactory documentBuilderFactory;
    private final XPathFactory xpathFactory;

    public XmlExtractor() {
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setExpandEntityReferences(false);
        this.documentBuilderFactory.setNamespaceAware(true);
        this.documentBuilderFactory.setXIncludeAware(false);
        this.xpathFactory = XPathFactory.newInstance();
    }

    public Document parseDocument(String xmlContent) throws ConvertException {
        try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConvertException("Unable to parse XML content", e);
        }
    }

    public List<String> extractValues(Document document, String expression) throws ConvertException {
        try {
            XPath xpath = xpathFactory.newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            return IntStream.range(0, nodes.getLength())
                    .mapToObj(nodes::item)
                    .map(Node::getTextContent)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        } catch (XPathExpressionException e) {
            throw new ConvertException("Failed to extract values from XML using XPath expression: " + expression, e);
        }
    }
}