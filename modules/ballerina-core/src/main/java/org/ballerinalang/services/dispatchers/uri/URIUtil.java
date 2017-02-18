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


/**
 * Utilities related to URI processing.
 */
public class URIUtil {

    public static final String URI_PATH_DELIMITER = "/";

    public static String getFirstPathSegment(String path) {
        if (!path.startsWith(URI_PATH_DELIMITER)) {
            path = URI_PATH_DELIMITER.concat(path);
        }

        String[] pathSegs = path.split(URI_PATH_DELIMITER);
        if (pathSegs.length > 1) {
            return pathSegs[1];
        }
        return URI_PATH_DELIMITER;
    }

    public static   String getSubPath(String path) {
        if (path.startsWith(URI_PATH_DELIMITER)) {
            path = path.replaceFirst(URI_PATH_DELIMITER, "");
        }

        int index = path.indexOf(URI_PATH_DELIMITER);
        if (index != -1) {
            return path.substring(index, path.length());
        }
        return URI_PATH_DELIMITER;

    }
}
