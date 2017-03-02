/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.siddhi.tcp.transport.utils;


import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

/**
 * the converter class that converts Events and its definitions in to various forms
 */
public final class EventDefinitionConverterUtil {

    private EventDefinitionConverterUtil() {

    }

    public static Attribute.Type[] generateAttributeTypeArray(List<Attribute> attributes) {
        if (attributes != null) {
            Attribute.Type[] attributeTypes = new Attribute.Type[attributes.size()];
            for (int i = 0, metaDataSize = attributes.size(); i < metaDataSize; i++) {
                Attribute attribute = attributes.get(i);
                attributeTypes[i] = attribute.getType();
            }
            return attributeTypes;
        } else {
            return null;  //to improve performance
        }
    }

}
