/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.elements;

/**
 * {@code AttachPoint} represents annotation attach point enum in ballerina.
 *
 * @since 0.974.0
 */
public enum AttachPoint {

    //TODO check with composer team and remove string representations.
    /**
     * Indicates Service Attach point.
     */
    SERVICE("service"),
    /**
     * Indicates Resource Attach point.
     */
    RESOURCE("resource"),
    /**
     * Indicates Function Attach point.
     */
    FUNCTION("function"),
    /**
     * Indicates Object Attach point.
     */
    OBJECT("object"),
    /**
     * Indicates Type Attach point.
     */
    TYPE("type"),
    /**
     * Indicates Endpoint Attach point.
     */
    ENDPOINT("endpoint"),
    /**
     * Indicates Parameter Attach point.
     */
    PARAMETER("parameter"),
    /**
     * Indicates Annotation Attach point.
     */
    ANNOTATION("annotation");

    private String value;

    AttachPoint(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AttachPoint getAttachmentPoint(String value) {
        for (AttachPoint attachmentPoint : AttachPoint.values()) {
            if (attachmentPoint.value.equals(value)) {
                return attachmentPoint;
            }
        }
        return null;
    }
}
