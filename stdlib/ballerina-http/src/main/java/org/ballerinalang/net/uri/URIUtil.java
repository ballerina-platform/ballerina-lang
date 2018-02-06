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

package org.ballerinalang.net.uri;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Utilities related to URI processing.
 */
public class URIUtil {

    public static final String URI_PATH_DELIMITER = "/";

    public static String[] getPathSegments(String path) {
        if (path.startsWith(URI_PATH_DELIMITER)) {
            path = path.substring(1);
        }
        return path.split(URI_PATH_DELIMITER);
    }

    public static String getSubPath(String path, String basePath) {
        if (path.length() == basePath.length()) {
            return URI_PATH_DELIMITER;
        }

        return path.substring(basePath.length());
    }

    public static void populateQueryParamMap(String queryParamString, BMap<String, BString> queryParamsMap)
            throws UnsupportedEncodingException {
        String[] queryParamVals = queryParamString.split("&");
        for (String queryParam : queryParamVals) {
            int index = queryParam.indexOf('=');
            if (index != -1) {
                String queryParamName = queryParam.substring(0, index).trim();
                String queryParamValue = URLDecoder.decode(queryParam.substring(index + 1).trim(), "UTF-8");
                if (queryParamValue.matches("")) {
                    queryParamsMap.put(queryParamName, new BString(""));
                    continue;
                }
                queryParamsMap.put(queryParamName, new BString(queryParamValue));
            }
        }
    }
}
