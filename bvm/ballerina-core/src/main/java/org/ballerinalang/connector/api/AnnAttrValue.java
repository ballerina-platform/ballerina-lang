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
package org.ballerinalang.connector.api;

/**
 * {@code AnnAttrValue} This API provides the functionality to access annotation attribute values in the
 * respective server connector.
 *
 * @since 0.94
 */
@Deprecated
public interface AnnAttrValue {

    /**
     * This method return the annotation attribute value type.
     *
     * @return value type of the attribute.
     */
    AnnotationValueType getType();

    /**
     * This method returns int attribute value.
     *
     * @return int value.
     */
    long getIntValue();

    /**
     * This method returns float attribute value.
     *
     * @return float value.
     */
    double getFloatValue();

    /**
     * This method returns string attribute value.
     *
     * @return string value.
     */
    String getStringValue();

    /**
     * This method returns boolean attribute value.
     *
     * @return boolean value.
     */
    boolean getBooleanValue();

    /**
     * This method return annotation attribute value.
     *
     * @return annotation value.
     */
    Annotation getAnnotation();

    /**
     * This method returns attribute value array.
     *
     * @return attribute value array.
     */
    AnnAttrValue[] getAnnAttrValueArray();

}
