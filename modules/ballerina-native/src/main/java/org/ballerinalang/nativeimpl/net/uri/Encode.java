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

package org.ballerinalang.nativeimpl.net.uri;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Native function to encode URLs.
 * ballerina.net.uri:encode
 */

@BallerinaFunction(
        packageName = "ballerina.net.uri",
        functionName = "encode",
        args = {@Argument(name = "url", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Encodes the given URL") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "url",
        value = "URL to be encoded") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The encoded url") })
public class Encode extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Encode.class);

    @Override
    public BValue[] execute(Context context) {
        String url = getArgument(context, 0).stringValue();
        String encodeURL = "";
        try {
            encodeURL = encode(url); //supporting percentage encoding
        } catch (Throwable e) {
            throw new BallerinaException("Error while encoding the url. " + e.getMessage(), context);
        }
        return getBValues(new BString(encodeURL));
    }

    private String encode(String url) throws UnsupportedEncodingException {
        String encoded;

        encoded = URLEncoder.encode(url, "UTF-8");

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
