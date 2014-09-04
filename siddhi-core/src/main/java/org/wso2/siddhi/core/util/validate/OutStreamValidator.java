/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.util.validate;

import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;
import java.util.Map;

/**
 * Class to validate stream definitions
 */
public class OutStreamValidator {
    /**
     * Method to validate stream definition
     *
     * @param streamDefinitionMap Map of existing definitions
     * @param definition          New definitions which needs to be validated
     * @throws org.wso2.siddhi.core.exception.ValidatorException Thrown if validation is failed
     */
    public static void validate(Map<String, StreamDefinition> streamDefinitionMap, StreamDefinition definition) throws ValidatorException {
        if (streamDefinitionMap.containsKey(definition.getId()) && !(streamDefinitionMap.get(definition.getId()).equals(definition))) {
            throw new ValidatorException("Different stream definition already exist under stream name " + definition.getId());
        }

    }

    /**
     * Method to validate stream definition
     *
     * @param definitionList List of existing definitions
     * @param newDefinition  New definitions which needs to be validated
     * @throws org.wso2.siddhi.core.exception.ValidatorException Thrown if validation is failed
     */
    public static void validate(List<StreamDefinition> definitionList, StreamDefinition newDefinition) throws ValidatorException {
        for (StreamDefinition definition : definitionList) {
            if ((definition.getId().equals(newDefinition.getId())) && !definition.equals(newDefinition)) {
                throw new ValidatorException("Different stream definition already exist under stream name " + newDefinition.getId());
            }
        }
    }
}
