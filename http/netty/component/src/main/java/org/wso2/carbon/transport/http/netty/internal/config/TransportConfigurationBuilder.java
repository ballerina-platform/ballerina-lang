/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.transport.http.netty.internal.config;

import org.xml.sax.SAXException;

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Parses &amp; creates the object model for the Netty transport configuration file.
 */
public class TransportConfigurationBuilder {

    public static final String NETTY_TRANSPORT_CONF = "transports.netty.conf";

    public static TransportsConfiguration build() {
        TransportsConfiguration transportsConfiguration;
        String nettyTransportsConfigFile =
                System.getProperty(NETTY_TRANSPORT_CONF,
                        "repository" + File.separator + "conf" + File.separator + "transports" +
                                File.separator + "netty-transports.xml");
        File file = new File(nettyTransportsConfigFile);
        if (file.exists()) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(TransportsConfiguration.class);

                // validate using the schema
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                StreamSource streamSource = new StreamSource();
                streamSource.
                        setInputStream(Thread.currentThread().getContextClassLoader().
                                getResourceAsStream("netty-transports.xsd"));
                Schema schema = sf.newSchema(streamSource);

                // un-marshall and populate the Netty transport configuration instance
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setSchema(schema);
                transportsConfiguration = (TransportsConfiguration) unmarshaller.unmarshal(file);
            } catch (SAXException | JAXBException e) {
                String msg = "Error while loading " + nettyTransportsConfigFile + " configuration file";
                throw new RuntimeException(msg, e);
            }
        } else { // return a default config
            transportsConfiguration = TransportsConfiguration.getDefault();
        }
        return transportsConfiguration;
    }
}
