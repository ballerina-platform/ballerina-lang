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

package org.ballerinalang.services.dispatchers.uri;


import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;

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

    public static boolean isMatchingMethodExist(ResourceInfo resourceInfo, String method) {
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

    public static String[] getHttpMethods(ResourceInfo resourceInfo) {
        AnnAttachmentInfo rConfigAnnAtchmnt = resourceInfo.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return null;
        }

        AnnAttributeValue methodsAttrVal = rConfigAnnAtchmnt
                .getAttributeValue(Constants.ANN_RESOURCE_ATTR_METHODS);
        if (methodsAttrVal == null) {
            return null;
        }
        return getStringArray(methodsAttrVal.getAttributeValueArray());
    }

    public static String[] getConsumerList(ResourceInfo resourceInfo) {
        AnnAttachmentInfo rConfigAnnAtchmnt = resourceInfo.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return null;
        }
        AnnAttributeValue consumesAttrVal = rConfigAnnAtchmnt
                .getAttributeValue(Constants.ANN_RESOURCE_ATTR_CONSUMES);
        if (consumesAttrVal == null) {
            return null;
        }
        return getStringArray(consumesAttrVal.getAttributeValueArray());
    }

    public static String[] getProducesList(ResourceInfo resourceInfo) {
        AnnAttachmentInfo rConfigAnnAtchmnt = resourceInfo.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return null;
        }
        AnnAttributeValue producesAttrVal = rConfigAnnAtchmnt
                .getAttributeValue(Constants.ANN_RESOURCE_ATTR_PRODUCES);
        if (producesAttrVal == null) {
            return null;
        }
        return getStringArray(producesAttrVal.getAttributeValueArray());
    }

    public static String[] getStringArray(AnnAttributeValue[] annAttributeValues) {
        String[] values = new String[annAttributeValues.length];
        for (int i = 0; i < annAttributeValues.length; i++) {
            values[i] = annAttributeValues[i].getStringValue();
        }
        return values;
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
