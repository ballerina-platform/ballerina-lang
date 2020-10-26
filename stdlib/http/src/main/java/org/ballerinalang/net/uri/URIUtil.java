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

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities related to URI processing.
 */
public class URIUtil {

    public static final String URI_PATH_DELIMITER = "/";
    public static final char DOT_SEGMENT = '.';
    private static final BString[] EMPTY_STRING_ARRAY = new BString[0];

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

    @SuppressWarnings("unchecked")
    public static void populateQueryParamMap(String queryParamString, BMap<BString, Object> queryParamsMap)
            throws UnsupportedEncodingException {
        Map<String, List<String>> tempParamMap = new HashMap<>();
        String[] queryParamVals = queryParamString.split("&");
        for (String queryParam : queryParamVals) {
            int index = queryParam.indexOf('=');
            if (index == -1) {
                continue;
            }
            String queryParamName = queryParam.substring(0, index).trim();
            String queryParamValue = queryParam.substring(index + 1).trim();
            List<String> values = new ArrayList<>();
            Set<String> uniqueValues = new HashSet<>();
            for (String val : queryParamValue.split(",")) {
                String decodedValue = URLDecoder.decode(val, "UTF-8");
                if (uniqueValues.add(decodedValue)) {
                    values.add(decodedValue);
                }
            }

            if (tempParamMap.containsKey(queryParamName)) {
                tempParamMap.get(queryParamName).addAll(values);
            } else {
                tempParamMap.put(queryParamName, values);
            }
        }

        for (Map.Entry<String, List<String>> entry : tempParamMap.entrySet()) {
            List<String> entryValue = entry.getValue();
            queryParamsMap.put(BStringUtils.fromString(entry.getKey()), BValueCreator
                    .createArrayValue(BStringUtils.fromStringArray(entryValue.toArray(new String[0]))));
        }
    }

    @SuppressWarnings("unchecked")
    public static BMap<BString, Object> getMatrixParamsMap(String path, HttpCarbonMessage carbonMessage) {
        BMap<BString, Object> matrixParamsBMap = BValueCreator.createMapValue();
        Map<String, Map<String, String>> pathToMatrixParamMap =
                (Map<String, Map<String, String>>) carbonMessage.getProperty(HttpConstants.MATRIX_PARAMS);
        Map<String, String> matrixParamsMap = pathToMatrixParamMap.get(path);
        if (matrixParamsMap != null) {
            for (Map.Entry<String, String> matrixParamEntry : matrixParamsMap.entrySet()) {
                matrixParamsBMap.put(BStringUtils.fromString(matrixParamEntry.getKey()),
                                     BStringUtils.fromString(matrixParamEntry.getValue()));
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
