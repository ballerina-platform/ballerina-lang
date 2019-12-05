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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities related to URI processing.
 */
public class URIUtil {

    private static final String URI_PATH_DELIMITER = "/";
    public static final char DOT_SEGMENT = '.';

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

    public static BMap<String, BValue> getMatrixParamsMap(String path, HttpCarbonMessage carbonMessage) {
        BMap<String, BValue> matrixParamsBMap = new BMap<>();
        Map<String, Map<String, String>> pathToMatrixParamMap =
                (Map<String, Map<String, String>>) carbonMessage.getProperty(HttpConstants.MATRIX_PARAMS);
        Map<String, String> matrixParamsMap = pathToMatrixParamMap.get(path);
        if (matrixParamsMap != null) {
            for (Map.Entry<String, String> matrixParamEntry : matrixParamsMap.entrySet()) {
                matrixParamsBMap.put(matrixParamEntry.getKey(), new BString(matrixParamEntry.getValue()));
            }
        }
        return matrixParamsBMap;
    }


    public static String extractMatrixParams(String path, Map<String, Map<String, String>> matrixParams) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String[] pathSplits = path.split("\\?");
        String[] pathSegments = pathSplits[0].split("/");
        String pathToMatrixParam = "";
        for (String pathSegment : pathSegments) {
            String[] splitPathSegment = pathSegment.split(";");
            pathToMatrixParam = pathToMatrixParam.concat("/" + splitPathSegment[0]);
            Map<String, String> segmentMatrixParams = new HashMap<>();
            for (int i = 1; i < splitPathSegment.length; i++) {
                String[] splitMatrixParam = splitPathSegment[i].split("=");
                if (splitMatrixParam.length != 2) {
                    throw new BallerinaConnectorException(
                            String.format("Found non-matrix parameter '%s' in path '%s'",
                                          splitPathSegment[i], path));
                }
                segmentMatrixParams.put(splitMatrixParam[0], splitMatrixParam[1]);
            }
            matrixParams.put(pathToMatrixParam, segmentMatrixParams);
        }

        for (int i = 1; i < pathSplits.length; i++) {
            pathToMatrixParam = pathToMatrixParam.concat("?").concat(pathSplits[i]);
        }
        return pathToMatrixParam;
    }
}
