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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Arrays;

import static org.ballerinalang.mime.util.Constants.COMMA;
import static org.ballerinalang.mime.util.Constants.PARSER_ERROR;
import static org.ballerinalang.mime.util.Constants.SEMICOLON;

/**
 * Native function to parse header value and get value with parameter map.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "parseHeader",
        args = {@Argument(name = "headerValue", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                @ReturnType(type = TypeKind.MAP, elementType = TypeKind.STRING),
                @ReturnType(type = TypeKind.STRUCT, structType = "Error")},
        isPublic = true
)
public class ParseHeader extends BlockingNativeCallableUnit {

    private static final BTupleType parseHeaderTupleType = new BTupleType(
            Arrays.asList(BTypes.typeString, BTypes.typeMap));

    @Override
    public void execute(Context context) {
        String errMsg;
        try {
            String headerValue = context.getStringArgument(0);
            if (headerValue.contains(COMMA)) {
                headerValue = headerValue.substring(0, headerValue.indexOf(COMMA));
            }

            // Set value and param map
            String value = headerValue.trim();
            if (headerValue.contains(SEMICOLON)) {
                value = HeaderUtil.getHeaderValue(value);
            }
            BRefValueArray contentTuple = new BRefValueArray(parseHeaderTupleType);
            contentTuple.add(0, new BString(value));
            contentTuple.add(1, HeaderUtil.getParamMap(headerValue));

            context.setReturnValues(contentTuple);
            return;
        } catch (BLangNullReferenceException ex) {
            errMsg = PARSER_ERROR + "header value cannot be null";
        } catch (BallerinaException ex) {
            errMsg = PARSER_ERROR + ex.getMessage();
        }

        // set parse error
        context.setReturnValues(MimeUtil.getParserError(context, errMsg));
    }
}
