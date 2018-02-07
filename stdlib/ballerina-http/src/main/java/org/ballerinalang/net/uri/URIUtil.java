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

import java.util.HashMap;
import java.util.Map;

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

    public static String removeMatrixParams(String path, Map<String, Map<String, String>> matrixParams) {
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
            if (splitPathSegment.length > 1) {
                for (int i = 1; i < splitPathSegment.length; i++) {
                    String[] splitMatrixParam = splitPathSegment[i].split("=");
                    if (splitMatrixParam.length != 2) {
                        throw new BallerinaConnectorException("Found non matrix parameter in " + path);
                    }
                    segmentMatrixParams.put(splitMatrixParam[0], splitMatrixParam[1]);
                }
            }
            matrixParams.put(pathToMatrixParam, segmentMatrixParams);
        }

        if (pathSplits.length > 1) {
            for (int i = 1; i < pathSplits.length; i++) {
                pathToMatrixParam = pathToMatrixParam.concat("?").concat(pathSplits[i]);
            }
        }
        return pathToMatrixParam;
    }
}
