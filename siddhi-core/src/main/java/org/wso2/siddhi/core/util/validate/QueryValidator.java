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
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.*;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;

import java.util.HashMap;
import java.util.Map;

/**
 * Static utility class to validate a single siddhi query.
 */
public class QueryValidator {

    /**
     * Publicly exposed validate method to validate a given query.
     *
     * @param query               Query to be validated
     * @param streamDefinitionMap Map of stream definitions user provided alongside with query
     * @throws org.wso2.siddhi.core.exception.ValidatorException
     */
    public static void validate(Query query, Map<String, StreamDefinition> streamDefinitionMap) throws ValidatorException {
        Map<String, StreamDefinition> tempDefinition = validateInStream(query.getInputStream(), streamDefinitionMap);       //Map to store definitions related to the given query
        validateSelector(query.getSelector(), tempDefinition);
        validateOutStream(query.getOutputStream(), tempDefinition, streamDefinitionMap);
    }

    /**
     * Method to validate input stream of the query.
     *
     * @param inputStream   input stream subjected to validation
     * @param definitionMap Map containing all stream definitions of execution plan
     * @return Map containing stream definition relevant to this query.
     * @throws org.wso2.siddhi.core.exception.ValidatorException
     */
    private static Map<String, StreamDefinition> validateInStream(InputStream inputStream, Map<String, StreamDefinition> definitionMap) throws ValidatorException {//TODO:Handle renaming
        Map<String, StreamDefinition> tempDefinitionMap = new HashMap<String, StreamDefinition>();
        if (inputStream instanceof BasicSingleInputStream || inputStream instanceof SingleInputStream) {
            InStreamValidator.validate(inputStream, definitionMap, tempDefinitionMap);
        } else if (inputStream instanceof JoinInputStream) {
            InputStream leftStream = ((JoinInputStream) inputStream).getLeftInputStream();
            InputStream rightStream = ((JoinInputStream) inputStream).getRightInputStream();
            InStreamValidator.validate(leftStream, definitionMap, tempDefinitionMap);
            InStreamValidator.validate(rightStream, definitionMap, tempDefinitionMap);
            if (((JoinInputStream) inputStream).getOnCompare() != null) {
                ExpressionParser.parseExpression(((JoinInputStream) inputStream).getOnCompare(), null, null, tempDefinitionMap, null, null,false);
            }
        } else if (inputStream instanceof StateInputStream) {
            //todo handle
            //handlePatternElement(((PatternInputStream) inputStream).getPatternElement(), definitionMap, tempDefinitionMap);
        }
        return tempDefinitionMap;
    }

//    /**
//     * Method to handle patterns input stream. Will decompose pattern stream recursively
//     * @param patternElement pattern element to be handled
//     * @param definitionMap full definition map
//     * @param tempDefinitionMap relevant definition map
//     * @throws ValidatorException
//     */
//    private static void handlePatternElement(PatternElement patternElement, Map<String, StreamDefinition> definitionMap, Map<String, StreamDefinition> tempDefinitionMap) throws ValidatorException {
//        if (patternElement instanceof FollowedByElement) {
//            handlePatternElement(((FollowedByElement) patternElement).getPatternElement(), definitionMap, tempDefinitionMap);
//            handlePatternElement(((FollowedByElement) patternElement).getFollowedByPatternElement(), definitionMap, tempDefinitionMap);
//        } else if (patternElement instanceof BasicSingleInputStream) {
//            InStreamValidator.validate((BasicSingleInputStream) patternElement, definitionMap, tempDefinitionMap);
//        }
//    }


    /**
     * Method to validate out stream of the query and insert it to definition
     * map once validated.
     *
     * @param outputStream          out stream to be validated
     * @param relevantDefinitionMap Map of relevant definitions
     * @param definitionMap         Full definition map
     * @throws org.wso2.siddhi.core.exception.ValidatorException
     */
    private static void validateOutStream(OutputStream outputStream, Map<String, StreamDefinition> relevantDefinitionMap, Map<String, StreamDefinition> definitionMap) throws ValidatorException {
        StreamDefinition definition = relevantDefinitionMap.get(null);
        definition.setId(outputStream.getId());
        OutStreamValidator.validate(definitionMap, definition);
    }

    /**
     * Method to validate selector of the query
     *
     * @param selector            selector to be validated
     * @param streamDefinitionMap relevant definition map
     * @throws org.wso2.siddhi.core.exception.ValidatorException
     */
    private static void validateSelector(Selector selector, Map<String, StreamDefinition> streamDefinitionMap) throws ValidatorException {
        SelectorValidator.validate(selector, streamDefinitionMap);
    }
}
