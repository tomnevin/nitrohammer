/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.java #{util.YYYY()} Viper Software Services
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 *
 * -----------------------------------------------------------------------------
 */

package com.viper.database.utils;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XMLUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    private static DocumentBuilder builder = null;

    private static DocumentBuilder getBuilder() throws Exception {

        if (builder != null) {
            return builder;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setIgnoringComments(false);
        factory.setExpandEntityReferences(false);
        factory.setIgnoringElementContentWhitespace(false);

        builder = factory.newDocumentBuilder();
        // builder.setEntityResolver(dtdResolver);

        return builder;
    }

    private static Transformer getTransformer() throws Exception {

        TransformerFactory tFactory = TransformerFactory.newInstance();
        return tFactory.newTransformer();
    }

    /**
     * This method writes a DOM document to a file
     * 
     * @param filename
     * @param document
     */
    public static void writeDocumentToFile(Document document, String filename) throws Exception {

        getTransformer().transform(new DOMSource(document), new StreamResult(new File(filename)));
    }
    
    public static void writeDocumentToFile(Document document, File file) throws Exception {

        getTransformer().transform(new DOMSource(document), new StreamResult(file));
    }

    public static String writeDocumentToText(Document document) throws Exception {

        StringWriter writer = new StringWriter();
        getTransformer().transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    public static Document readXMLFile(String filename) throws Exception {

        return readXMLFile(FileUtil.getInputStream(FileUtil.class, filename));
    }

    public static Document readXMLFile(URL url) throws Exception {

        return readXMLFile(new InputSource(url.openStream()));
    }

    public static Document readXMLText(String xml) throws Exception {

        Document doc = getBuilder().parse(new InputSource(new StringReader(xml)));
        doc.getDocumentElement().normalize();
        return doc;
    }

    public static Document readXMLFile(InputSource source) throws Exception {

        Document doc = getBuilder().parse(source);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public static Document readXMLFile(InputStream inputStream) throws Exception {

        Document doc = getBuilder().parse(inputStream);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public static Document readXMLFile(File file) throws Exception {

        Document doc = getBuilder().parse(file);
        doc.getDocumentElement().normalize();
        return doc;
    }
}
