/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.net.http.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ballerina function to set a message property.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "parseHeaderValue",
        args = {@Argument(name = "headerValue", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ParseHeaderValue extends AbstractNativeFunction {

    private final String SEMICOLON = ";";
    private final String COMMA = ",";

    @Override
    public BValue[] execute(Context context) {
        String headerValue = getStringArgument(context, 0);
        if (headerValue.contains(COMMA)) {
            headerValue = headerValue.substring(0, headerValue.indexOf(COMMA));
        }
        return getValueAndParamMap(headerValue);
    }

    private BValue[] getValueAndParamMap(String headerValue) {
        String value = headerValue;
        BMap<String, BValue> paramMap = null;
        if (headerValue.contains(SEMICOLON)) {
            value = headerValue.substring(0, headerValue.indexOf(SEMICOLON)).trim();
            List<String> paramList = Arrays.stream(headerValue.substring(headerValue.indexOf(SEMICOLON) + 1)
                    .split(SEMICOLON)).map(String::trim).collect(Collectors.toList());
            paramMap = validateParams(paramList) ? HttpUtil.createParamBMap(paramList) : null;
        }
        return getBValues(new BString(value), paramMap);
    }

    private boolean validateParams(List<String> paramList) {
        if (paramList.size() == 1 && paramList.get(0).isEmpty()) {
            return false;
        }


        return true;
    }
}
