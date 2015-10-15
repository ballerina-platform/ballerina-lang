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

package org.wso2.siddhi.extension.eventtable.jaxbMappings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Contains list of elements for a specific db type
 */
public class Mapping {

    private String db;
    private Elements elements;

    public String getDb() {
        return db;
    }

    @XmlAttribute
    public void setDb(String db) {
        this.db = db;
    }

    public Elements getElements() {
        return elements;
    }

    @XmlElement
    public void setElements(Elements elements) {
        this.elements = elements;
    }
}
