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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Native function to get a query parameter value for a given key.
 * ballerina.net.uri:getQueryParam
 */

@BallerinaFunction(
        packageName = "ballerina.net.uri",
        functionName = "getQueryParam",
        args = {@Argument(name = "m", type = TypeKind.MESSAGE),
                @Argument(name = "key", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetQueryParam extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetQueryParam.class);

    @Override
    public BValue[] execute(Context context) {
        BMessage msg = (BMessage) getRefArgument(context, 0);

        String to = (String) msg.value().getProperty(Constants.TO);

        if (!to.contains("?")) {
            return getBValues(new BString("")); // todo return null;
        } else {
            Map<String, String> queryParams = fetchQueryParams(to);
            String key = getStringArgument(context, 0);
            String value = queryParams.get(key);
            if (value == null) {
                return getBValues(new BString("")); // todo return null;
            }
            return getBValues(new BString(value));
        }
    }

    //todo better moving this logic to carbon transport and set this map as a property
    private Map<String, String> fetchQueryParams(String uri) {
        String queryString = uri.substring(uri.indexOf("?") + 1);
        String[] keyValues = queryString.split("&");
        Map<String, String> queryParamMap = new HashMap<>();
        for (String keyValue : keyValues) {
            String[] keyValArray = keyValue.split("=");
            try {
                queryParamMap.put(URLDecoder.decode(keyValArray[0].trim(), "UTF-8"),
                        URLDecoder.decode(keyValArray[1].trim(), "UTF-8"));
            } catch (Throwable e) {
                throw new BallerinaException("Cannot decode the query parameter. " + e.getMessage());
            }
        }
        return queryParamMap;
    }
}
