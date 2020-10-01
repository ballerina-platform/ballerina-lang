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
 * {@code AttachPoint} represents annotation attach point in ballerina.
 *
 * @since 1.0
 */
public class AttachPoint {

    public Point point;
    public boolean source;

    /**
     * {@code Point} represents annotation attach point enum in ballerina.
     *
     * @since 0.974.0
     */
    public enum Point {
        /**
         * Indicates Type Attach point.
         */
        TYPE("type"),
        /**
         * Indicates Object Type Attach point.
         */
        OBJECT("objecttype"),
        /**
         * Indicates Function Attach point.
         */
        FUNCTION("function"),
        /**
         * Indicates Object Method Attach point.
         */
        OBJECT_METHOD("objectfunction"),
        /**
         * Indicates Resource Attach point.
         */
        RESOURCE("resourcefunction"),
        /**
         * Indicates Parameter Attach point.
         */
        PARAMETER("parameter"),
        /**
         * Indicates Return Attach point.
         */
        RETURN("return"),
        /**
         * Indicates Service Attach point.
         */
        SERVICE("service"),
        /**
         * Indicates Field Attach point.
         */
        FIELD("field"),
        /**
         * Indicates Object Field Attach point.
         */
        OBJECT_FIELD("objectfield"),
        /**
         * Indicates Record Field Attach point.
         */
        RECORD_FIELD("recordfield"),
        /**
         * Indicates Object Attach point.
         */
        LISTENER("listener"),
        /**
         * Indicates listener Attach point.
         */
        ANNOTATION("annotation"),
        /**
         * Indicates external Attach point.
         */
        EXTERNAL("external"),
        /**
         * Indicates var Attach point.
         */
        VAR("var"),
        /**
         * Indicates const Attach point.
         */
        CONST("const"),
        /**
         * Indicate Worker Attach point.
         */
        WORKER("worker"),
        /**
         * Indicate class Attach point.
         */
        CLASS("class");

        private String value;

        Point(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private AttachPoint(Point point, boolean source) {
        this.point = point;
        this.source = source;
    }

    public static AttachPoint getAttachmentPoint(String value, boolean source) {
        for (Point point : Point.values()) {
            if (point.value.equals(value)) {
                return new AttachPoint(point, source);
            }
        }
        return null;
    }
}
