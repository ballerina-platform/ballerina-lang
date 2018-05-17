/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.uri.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Native function to decode URLs.
 * ballerina.http:decode
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "decode",
        args = {@Argument(name = "url", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                      @ReturnType(type = TypeKind.STRUCT, structType = "Error")},
        isPublic = true
)
public class Decode extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String url = context.getStringArgument(0);
        String charset = context.getStringArgument(1);
        try {
            context.setReturnValues(new BString(URLDecoder.decode(url, charset)));
        } catch (UnsupportedEncodingException e) {
            context.setReturnValues(HttpUtil.getError(context, "Error occurred while decoding the url. " + e
                    .getMessage()));
        }
    }
}
