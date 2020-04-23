/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author supun
 *
 */
public class TestXMLParser {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String s = "<4ÑfoĦoƸ>heÑllo</4ÑfoĦoƸ>";
//        String s = "<!-- comment -->";
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        
        XMLStreamReader reader;
        
        try {
            reader = xmlInputFactory.createXMLStreamReader(new StringReader(s));
            while(reader.hasNext()) {
                int next = reader.next();
                String kind = getKind(next);
                System.out.println(kind);
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return;
        }
    }
    
    private static String getKind(int a) {
        switch(a) {
            case XMLStreamReader.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLStreamReader.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLStreamReader.START_ELEMENT:
                return "START_ELEMENT";
            case XMLStreamReader.END_ELEMENT:
                return "END_ELEMENT";
            case XMLStreamReader.COMMENT:
                return "COMMENT";
            case XMLStreamReader.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLStreamReader.NAMESPACE:
                return "NAMESPACE";
                default:
                    return "N/A";
        }
    }

}
