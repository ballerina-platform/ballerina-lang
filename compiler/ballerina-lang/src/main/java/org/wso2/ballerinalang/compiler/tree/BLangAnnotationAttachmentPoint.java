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

package org.wso2.ballerinalang.compiler.tree;

/**
 * @since 0.94
 */
public class BLangAnnotationAttachmentPoint {

    public AttachmentPoint attachmentPoint;

    public BLangAnnotationAttachmentPoint(AttachmentPoint attachmentPoint) {
        this.attachmentPoint = attachmentPoint;
    }

    public AttachmentPoint getAttachmentPoint() {
        return attachmentPoint;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BLangAnnotationAttachmentPoint)) {
            return false;
        }

        BLangAnnotationAttachmentPoint other = (BLangAnnotationAttachmentPoint) obj;
        return attachmentPoint.equals(other.getAttachmentPoint());
    }

    /**
     * Enum to represent attachment point.
     */
    public enum AttachmentPoint {
        SERVICE("service"),
        RESOURCE("resource"),
        FUNCTION("function"),
        OBJECT("object"),
        TYPE("type"),
        ENDPOINT("endpoint"),
        PARAMETER("parameter"),
        ANNOTATION("annotation");

        private String value;

        AttachmentPoint(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static AttachmentPoint getAttachmentPoint(String value) {
            for (AttachmentPoint attachmentPoint : AttachmentPoint.values()) {
                if (attachmentPoint.value.equals(value)) {
                    return attachmentPoint;
                }
            }
            return null;
        }

    }
}
