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


import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    public static String getServiceBasePath(ServiceInfo service) {
        String basePath = service.getName();
        AnnAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants
                .HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

        if (annotationInfo != null) {
            AnnAttributeValue annAttributeValue = annotationInfo.getAttributeValue
                    (Constants.ANN_CONFIG_ATTR_BASE_PATH);
            if (annAttributeValue != null && annAttributeValue.getStringValue() != null &&
                    !annAttributeValue.getStringValue().trim().isEmpty()) {
                basePath = annAttributeValue.getStringValue();
            }
        }

        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        return basePath;
    }

    public static String concatValues(List<String> values) {
        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < values.size(); ++x) {
            sb.append(values.get(x));
            if (x != values.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static List<String> validateAllowMethods(List<String> cachedMethods) {
        if (cachedMethods != null && cachedMethods.size() != 0) {
            if (cachedMethods.contains(Constants.HTTP_METHOD_GET)) {
                cachedMethods.add(Constants.HTTP_METHOD_HEAD);
            }
            cachedMethods.add(Constants.HTTP_METHOD_OPTIONS);
            cachedMethods = cachedMethods.stream().distinct().collect(Collectors.toList());
        }
        return cachedMethods;
    }

    public static List<String> addAllMethods() {
        return Stream.of(Constants.HTTP_METHOD_GET, Constants.HTTP_METHOD_HEAD
                , Constants.HTTP_METHOD_POST, Constants.HTTP_METHOD_DELETE
                , Constants.HTTP_METHOD_PUT, Constants.HTTP_METHOD_OPTIONS).collect(Collectors.toList());
    }
}
