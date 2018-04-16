/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.net.URLEncoder;

/**
 * Native function to encode URLs.
 * ballerina.http:encode
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "encode",
        args = {@Argument(name = "url", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                      @ReturnType(type = TypeKind.STRUCT, structType = "Error")},
        isPublic = true
)
public class Encode extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String url = context.getStringArgument(0);
        String charset = context.getStringArgument(1);
        try {
            context.setReturnValues(new BString(encode(url, charset)));
        } catch (Throwable e) {
            context.setReturnValues(HttpUtil.getGenericError(context, "Error occurred while encoding the url. " + e
                    .getMessage()));
        }
    }

    private String encode(String url, String charset) throws UnsupportedEncodingException {
        String encoded;

        encoded = URLEncoder.encode(url, charset);

        StringBuilder buf = new StringBuilder(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length() && encoded.charAt(i + 1) == '7'
                    && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }

        }
        return buf.toString();
    }
}
