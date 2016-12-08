/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.model.types;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * {@code XMLType} represents an XML Element
 *
 * @since 1.0.0
 */
public class XMLType extends AbstractType {

    private static final Logger LOG = LoggerFactory.getLogger(XMLType.class);

    private OMElement omElement;


    public XMLType(String value) {
        if (value != null) {
            try {
                omElement = AXIOMUtil.stringToOM(value);
            } catch (XMLStreamException e) {
                LOG.error("Cannot create OMElement from given String, maybe malformed String ", e);
            }
        }
    }

    public XMLType(OMElement omElement) {
        this.omElement = omElement;
    }

    public XMLType(InputStream inputStream) {
        if (inputStream != null) {
            try {
                omElement = new StAXOMBuilder(inputStream).getDocumentElement();
            } catch (XMLStreamException e) {
                LOG.error("Cannot create OMElement from given source, maybe malformed XML Stream", e);
            }
        }
    }

    /**
     * @return OMElement provides Axiom Object
     */
    public OMElement getOmElement() {
        return omElement;
    }


}
