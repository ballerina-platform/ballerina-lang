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

package org.ballerinalang.services.dispatchers.uri;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Query parameter related operations.
 */

public class QueryParamProcessor {
    public static final String ENCODING = "UTF-8";

    public static Map<String, String> processQueryParams(String queryStr) throws UnsupportedEncodingException {
        Map<String, String> queryParams = new HashMap<>();
        String[] entries = queryStr.split("&");
        for (String entry : entries) {
            int index = entry.indexOf('=');
            if (index != -1) {
                String name = entry.substring(0, index);
                String value = URLDecoder.decode(entry.substring(index + 1),
                        ENCODING);
                queryParams.put(name, value);
            }
        }
        return queryParams;
    }
}
