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

/**
 * Utilities related to dispatcher processing.
 */
public class DispatcherUtil {

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
}
