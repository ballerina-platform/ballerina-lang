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

package org.ballerinalang.services.dispatchers.uri.parser;



import org.ballerinalang.services.dispatchers.uri.URITemplateException;

import java.util.Map;

public class ReservedStringExpression extends SimpleStringExpression {

    public ReservedStringExpression(String token) throws URITemplateException {
        super(token);
    }

    @Override
    protected boolean isReserved(char ch) {
        return false;
    }

    @Override
    protected String encodeValue(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (super.isReserved(ch)) {
                builder.append(ch);
            } else {
                builder.append(super.encodeValue(String.valueOf(ch)));
            }
        }
        return builder.toString();
    }

    @Override
    protected boolean setVariables(String expressionValue, Map<String, String> variables) {
        if (variableList.size() == 1) {
            Variable var = variableList.get(0);
            String name = var.getName();
            String finalValue = decodeValue(expressionValue);
            if (variables.containsKey(name) && !finalValue.equals(variables.get(name))) {
                return false;
            }

            if (var.checkModifier(finalValue)) {
                variables.put(name, finalValue);
                return true;
            } else {
                return false;
            }
        }
        return super.setVariables(expressionValue, variables);
    }
}
