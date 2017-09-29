/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.http.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilities related to dispatcher processing.
 */
public class DispatcherUtil {

    private static String[] allMethods = new String[]{Constants.HTTP_METHOD_GET, Constants.HTTP_METHOD_HEAD
            , Constants.HTTP_METHOD_POST, Constants.HTTP_METHOD_DELETE
            , Constants.HTTP_METHOD_PUT, Constants.HTTP_METHOD_OPTIONS};

    public static boolean isMatchingMethodExist(Resource resourceInfo, String method) {
        String[] rHttpMethods = getHttpMethods(resourceInfo);
        if (rHttpMethods == null) {
            return false;
        }
        for (String value : rHttpMethods) {
            if (value.equals(method)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getHttpMethods(Resource resourceInfo) {
        Annotation rConfigAnnAtchmnt = resourceInfo.getAnnotation(Constants.HTTP_PACKAGE_PATH,
                Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return null;
        }

        AnnAttrValue methodsAttrVal = rConfigAnnAtchmnt.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_METHODS);
        if (methodsAttrVal == null) {
            return null;
        }
        return getStringArray(methodsAttrVal.getAnnAttrValueArray());
    }

    public static String[] getConsumerList(Resource resourceInfo) {
        Annotation rConfigAnnAtchmnt = resourceInfo.getAnnotation(Constants.HTTP_PACKAGE_PATH,
                Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return null;
        }
        AnnAttrValue consumesAttrVal = rConfigAnnAtchmnt
                .getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_CONSUMES);
        if (consumesAttrVal == null) {
            return null;
        }
        return getStringArray(consumesAttrVal.getAnnAttrValueArray());
    }

    public static String[] getProducesList(Resource resourceInfo) {
        Annotation rConfigAnnAtchmnt = resourceInfo.getAnnotation(Constants.HTTP_PACKAGE_PATH,
                Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return null;
        }
        AnnAttrValue producesAttrVal = rConfigAnnAtchmnt.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_PRODUCES);
        if (producesAttrVal == null) {
            return null;
        }
        return getStringArray(producesAttrVal.getAnnAttrValueArray());
    }

    public static String[] getStringArray(AnnAttrValue[] annAttributeValues) {
        String[] values = new String[annAttributeValues.length];
        for (int i = 0; i < annAttributeValues.length; i++) {
            values[i] = annAttributeValues[i].getStringValue();
        }
        return values;
    }

    public static String getServiceBasePath(Service service) {
        String basePath = service.getName();
        Annotation annotationInfo = service.getAnnotation(Constants
                .HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

        if (annotationInfo != null) {
            AnnAttrValue annAttributeValue = annotationInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_BASE_PATH);
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

    public static String concatValues(List<String> stringValues, boolean spaceSeparated) {
        StringBuilder builder = new StringBuilder();
        String separator = spaceSeparated ? " " : ", ";
        for (int x = 0; x < stringValues.size(); ++x) {
            builder.append(stringValues.get(x));
            if (x != stringValues.size() - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
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
        return Arrays.stream(allMethods).collect(Collectors.toList());
    }
}
