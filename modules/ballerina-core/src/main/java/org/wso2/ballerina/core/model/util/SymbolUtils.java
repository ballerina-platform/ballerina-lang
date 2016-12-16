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
package org.wso2.ballerina.core.model.util;

import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.TypeC;

/**
 *
 */
public class SymbolUtils {

    public static SymbolName generateSymbolName(String identifier, Parameter[] parameters) {
        StringBuilder stringBuilder = new StringBuilder(identifier);
        for (Parameter param : parameters) {
            stringBuilder.append("_").append(param.getTypeC());
        }
        return new SymbolName(stringBuilder.toString());
    }

    public static SymbolName generateSymbolName(String identifier, TypeC[] types) {
        StringBuilder stringBuilder = new StringBuilder(identifier);
        for (TypeC type : types) {
            stringBuilder.append("_").append(type);
        }
        return new SymbolName(stringBuilder.toString());
    }

    public static TypeC[] getTypesOfParams(Parameter[] parameters) {
        TypeC[] types = new TypeC[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getTypeC();
        }
        return types;
    }
}
