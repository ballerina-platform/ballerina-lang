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
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpService;

import java.util.ArrayList;
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

    public static boolean isMatchingMethodExist(HttpResource resourceInfo, String method) {
        if (resourceInfo.getMethods() == null) {
            return false;
        }
        return resourceInfo.getMethods().contains(method);
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

    public static String[] getStringArray(AnnAttrValue[] annAttributeValues) {
        String[] values = new String[annAttributeValues.length];
        for (int i = 0; i < annAttributeValues.length; i++) {
            values[i] = annAttributeValues[i].getStringValue();
        }
        return values;
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
        //cachedMethods cannot be null here.
        if (cachedMethods.isEmpty()) {
            return cachedMethods;
        }
        if (cachedMethods.contains(Constants.HTTP_METHOD_GET)) {
            cachedMethods.add(Constants.HTTP_METHOD_HEAD);
        }
        cachedMethods.add(Constants.HTTP_METHOD_OPTIONS);
        cachedMethods = cachedMethods.stream().distinct().collect(Collectors.toList());
        return cachedMethods;
    }

    public static List<String> addAllMethods() {
        return Arrays.stream(allMethods).collect(Collectors.toList());
    }

    public static List<String> getValueList(AnnAttrValue annAttrValue, List<String> defaultVals) {
        if (annAttrValue.getAnnAttrValueArray() == null) {
            return defaultVals;
        }
        List<String> list = new ArrayList<>();
        for (AnnAttrValue attr : annAttrValue.getAnnAttrValueArray()) {
            list.add(attr.getStringValue().trim());
        }
        if (list.isEmpty()) {
            return defaultVals;
        }
        return list;
    }

    public static List<String> getAllResourceMethods(HttpService service) {
        List<String> cachedMethods = new ArrayList();
        for (HttpResource resource : service.getResources()) {
            if (resource.getMethods() == null) {
                cachedMethods.addAll(DispatcherUtil.addAllMethods());
                break;
            }
            cachedMethods.addAll(resource.getMethods());
        }
        return validateAllowMethods(cachedMethods);
    }
}
