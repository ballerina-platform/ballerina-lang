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
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.HashMap;
import java.util.Map;

public class SelectorValidator {

    /**
     * Method to validate selector for given set of stream definitions     *
     *
     * @param selector            :       Selector to validate
     * @param streamDefinitionMap :      Map to handle renaming
     * @throws org.wso2.siddhi.core.exception.ValidatorException Thrown if not validated
     */
    public static void validate(Selector selector, Map<String, StreamDefinition> streamDefinitionMap) throws ValidatorException {
        StreamDefinition temp = new StreamDefinition();        //inferred stream
        if (!selector.getSelectionList().isEmpty()) {
            for (OutputAttribute attribute : selector.getSelectionList()) {
                ExpressionExecutor executor = ExpressionParser.parseExpression(attribute.getExpression(), null, null, streamDefinitionMap, null, null);//current stream reference and siddhi context is null
                temp.attribute(attribute.getRename(), executor.getReturnType());
            }
            if (selector.getGroupByList() != null) {                        //Handle group by
                for (Variable var : selector.getGroupByList()) {
                    ExpressionParser.parseExpression(var, null, null, streamDefinitionMap, null, null);
                }
            }
            if (selector.getHavingExpression() != null) {                    //Handle having onDeleteExpression. send only the inferred stream
                Map<String, StreamDefinition> tempMap = new HashMap<String, StreamDefinition>(1);
                tempMap.put(null, temp);                                     //putting with null id to avoid conflicts
                ExpressionParser.parseExpression(selector.getHavingExpression(), null, null, tempMap, null, null);
            }
        } else {
            for (StreamDefinition definition : streamDefinitionMap.values()) {
                for (Attribute attribute : definition.getAttributeList()) {
                    temp.attribute(attribute.getName(), attribute.getType());
                    selector.select(attribute.getName(), new Variable(attribute.getName()).ofStream(definition.getId())); //TODO:discuss Should this be in validation??
                }
            }
        }
        streamDefinitionMap.put(null, temp);
    }

}
