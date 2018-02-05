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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Native function to parse header value and get value with parameter map.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "parseHeader",
        args = {@Argument(name = "headerValue", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                @ReturnType(type = TypeKind.MAP, elementType = TypeKind.STRING),
                @ReturnType(type = TypeKind.STRUCT, structType = "Error")},
        isPublic = true
)
public class ParseHeader extends AbstractNativeFunction {

    private static final String SEMICOLON = ";";
    private static final String COMMA = ",";
    private static final String PARSER_ERROR = "failed to parse: ";
    private static final String BUILTIN_PACKAGE = "ballerina.builtin";
    private static final String STRUCT_GENERIC_ERROR = "error";

    @Override
    public BValue[] execute(Context context) {
        try {
            String headerValue = getStringArgument(context, 0);
            if (headerValue.contains(COMMA)) {
                headerValue = headerValue.substring(0, headerValue.indexOf(COMMA));
            }
            return getValueAndParamMap(headerValue);
        } catch (BLangNullReferenceException ex) {
            String errMsg = PARSER_ERROR + "header value cannot be null";
            return getParserError(context, errMsg);

        } catch (BallerinaException ex) {
            String errMsg = PARSER_ERROR + ex.getMessage();
            return getParserError(context, errMsg);
        }
    }

    private BValue[] getValueAndParamMap(String headerValue) {
        String value = headerValue.trim();
        BMap<String, BValue> paramMap = null;
        if (headerValue.contains(SEMICOLON)) {
            value = extractValue(headerValue);
            List<String> paramList = Arrays.stream(headerValue.substring(headerValue.indexOf(SEMICOLON) + 1)
                    .split(SEMICOLON)).map(String::trim).collect(Collectors.toList());
            paramMap = validateParams(paramList) ? HttpUtil.createParamBMap(paramList) : null;
        }
        return getBValues(new BString(value), paramMap);
    }

    private String extractValue(String headerValue) {
        String value = headerValue.substring(0, headerValue.indexOf(SEMICOLON)).trim();
        if (value.isEmpty()) {
            throw new BallerinaException("invalid header value: " + headerValue);
        }
        return value;
    }

    private boolean validateParams(List<String> paramList) {
        //validate header values which ends with semicolon without params
        if (paramList.size() == 1 && paramList.get(0).isEmpty()) {
            return false;
        }
        return true;
    }

    private BValue[] getParserError(Context context, String errMsg) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(BUILTIN_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);

        BStruct parserError = new BStruct(errorStructInfo.getType());
        parserError.setStringField(0, errMsg);
        return getBValues(null, null, parserError);
    }
}
