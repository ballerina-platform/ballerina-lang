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

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Contain list of elements available for each db type
 */
public class Elements {

    private List<Element> elementList;

    public List<Element> getElementList() {
        return elementList;
    }

    @XmlElement(name = "element")
    public void setElementList(List<Element> elementList) {
        this.elementList = elementList;
    }

    public Element getType(String javaType) {
        Element matchedElement = null;
        for (Element element : elementList) {
            if (element.getKey().equals(javaType)) {
                matchedElement = element;
                break;
            }
        }
        return matchedElement;
    }
}
