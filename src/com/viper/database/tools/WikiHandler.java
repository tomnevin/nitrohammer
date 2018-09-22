/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 *
 * Copyright (c) #{classname}.html #{util.YYYY()} Viper Software Services
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

package com.viper.database.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WikiHandler extends DefaultHandler {

    private boolean inTitle = false;
    private String title = "";

    private String outfile;
    private Writer writer;

    public WikiHandler(String outfile) throws Exception {
        this.outfile = outfile;
    }

    @Override
    public void startDocument() {
        try {
            writer = new FileWriter(outfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("title")) {
            title = "";
            inTitle = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        try {
            if (qName.equalsIgnoreCase("title")) {
                writer.write(title);
                writer.write("\n");
                title = "";
                inTitle = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

        if (inTitle) {
            title += new String(ch, start, length);
        }
    }
}