/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.rdbms;


import org.apache.log4j.Logger;
import org.wso2.siddhi.core.exception.CannotLoadConfigurationException;
import org.wso2.siddhi.extension.eventtable.jaxbMappings.Element;
import org.wso2.siddhi.extension.eventtable.jaxbMappings.Mapping;
import org.wso2.siddhi.extension.eventtable.jaxbMappings.Mappings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that reads the rdbms-table-config.xml and assign all the db mappings
 */
public class DBQueryHelper {

    private static Map<String, Map<String, String>> dbTypeMappings;
    private static final Logger log = Logger.getLogger(DBQueryHelper.class);

    private DBQueryHelper() {

    }

    public static void loadConfiguration() throws CannotLoadConfigurationException {
        new DBQueryHelper().populateJaxbMappings();
    }

    /**
     * Populate xml values to Jaxb mapping classes
     */
    private void populateJaxbMappings() throws CannotLoadConfigurationException {

        JAXBContext jaxbContext;
        dbTypeMappings = new HashMap<String, Map<String, String>>();
        try {
            jaxbContext = JAXBContext.newInstance(Mappings.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(RDBMSEventTableConstants.RDBMS_TABLE_CONFIG_FILE);
            if (inputStream == null) {
                throw new CannotLoadConfigurationException(RDBMSEventTableConstants.RDBMS_TABLE_CONFIG_FILE + " is not found in the classpath");
            }
            Mappings mappings = (Mappings) unmarshaller.unmarshal(inputStream);
            Map<String, Mapping> dbMap = new HashMap<String, Mapping>();
            List<Mapping> mappingList = mappings.getMapping();

            for (Mapping mapping : mappingList) {
                dbMap.put(mapping.getDb(), mapping);
            }

            //Constructs a map to contain all db wise elements and there values
            for (Mapping mapping : mappingList) {
                if (mapping.getDb() != null) {
                    Mapping defaultMapping = dbMap.get(null);
                    Mapping specificMapping = dbMap.get(mapping.getDb());
                    List<Element> defaultElementList = defaultMapping.getElements().getElementList();
                    Map<String, String> elementMappings = new HashMap<String, String>();
                    for (Element element : defaultElementList) {
                        //Check if the mapping is present in the specific mapping
                        Element elementDetails = null;
                        if (specificMapping.getElements().getElementList() != null) {
                            elementDetails = specificMapping.getElements().getType(element.getKey());
                        }
                        //If a specific mapping is not found then use the default mapping
                        if (elementDetails == null) {
                            elementDetails = defaultMapping.getElements().getType(element.getKey());
                        }
                        elementMappings.put(elementDetails.getKey(), elementDetails.getValue());
                    }
                    dbTypeMappings.put(mapping.getDb(), elementMappings);
                } else {
                    Mapping defaultMapping = dbMap.get(null);
                    List<Element> defaultElementList = defaultMapping.getElements().getElementList();
                    Map<String, String> elementMappings = new HashMap<String, String>();
                    for (Element element : defaultElementList) {
                        Element elementDetails = defaultMapping.getElements().getType(element.getKey());
                        elementMappings.put(elementDetails.getKey(), elementDetails.getValue());
                    }
                    dbTypeMappings.put("default", elementMappings);
                }
            }
        } catch (JAXBException e) {
            throw new CannotLoadConfigurationException("Syntax Error.Cannot unmarshal provided File "
                    + RDBMSEventTableConstants.RDBMS_TABLE_CONFIG_FILE + e.getMessage(), e);
        }
    }

    public static Map<String, Map<String, String>> getDbTypeMappings() {
        return dbTypeMappings;
    }
}
