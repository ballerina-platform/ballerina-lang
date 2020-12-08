/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.core.model;

import org.ballerinalang.core.model.values.BValue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code Annotation} represents an Annotation node in Ballerina.
 * <p>
 * Annotation can be associated with various Ballerina concepts like Service, Resource, Functions, etc.
 *
 * @see <a href="https://github.com/wso2/ballerina/blob/master/docs/SyntaxSummary.md">Ballerina Syntax Summary</a>
 * @since 0.8.0
 */
public class AnnotationAttachment implements Node {

    private String name;
    private String pkgName;
    private String pkgPath;
    private Map<String, AnnotationAttributeValue> attributeNameValPairs = new HashMap<>();
    private NodeLocation location;
    private WhiteSpaceDescriptor whiteSpaceDescriptor;
    AnnotationAttachmentPoint attachedPoint;
    
    public AnnotationAttachment(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String name,
                                String pkgName, String pkgPath, Map<String, AnnotationAttributeValue> fieldValPairs) {
        this.location = location;
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        this.name = name;
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
        this.attributeNameValPairs = fieldValPairs;
    }

    /**
     * Get name of the annotation.
     *
     * @return name of the annotation
     */
    public String getName() {
        return name;
    }

    /**
     * Get the package name of the annotation.
     * 
     * @return Package name of the annotation
     */
    public String getPkgName() {
        return pkgName;
    }

    /**
     * Get the package path of the annotation.
     * 
     * @return Package path of the annotation
     */
    public String getPkgPath() {
        return pkgPath;
    }
    
    /**
     * Get Key-Value pairs of fields in the annotation.
     *
     * @return all Key-Value pairs
     */
    public Map<String, AnnotationAttributeValue> getAttributeNameValuePairs() {
        return attributeNameValPairs;
    }

    /**
     * Get the value of the Key-Value pair.
     *
     * @param attributeName attribute name
     * @return value of the attribute
     */
    public AnnotationAttributeValue getAttribute(String attributeName) {
        return attributeNameValPairs.get(attributeName);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    /**
     * Set the whitespace descriptor for this node.
     *
     * @param whiteSpaceDescriptor descriptor
     */
    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }

    /**
     * Set the construct where this annotation is attached.
     * 
     * @param attachedPoint Where an annotation can be attached
     */
    public void setAttachedPoint(AnnotationAttachmentPoint attachedPoint) {
        this.attachedPoint = attachedPoint;
    }
    
    /**
     * Get the construct where this annotation is attached.
     * 
     * @return the attachedPoint
     */
    public AnnotationAttachmentPoint getAttachedPoint() {
        return attachedPoint;
    }

    /**
     * Add a attribute name-value pair for the annotation.
     * 
     * @param name Name of the attribute
     * @param value Value of the attribute
     */
    public void addAttributeNameValuePair(String name, AnnotationAttributeValue value) {
        this.attributeNameValPairs.put(name, value);
    }
    
    @Override
    public String toString() {
        String nameValuePairs = attributeNameValPairs.entrySet().stream()
                .map(nameValue -> nameValue.getKey() + ":" + nameValue.getValue().toString())
                .collect(Collectors.joining(", "));
        
        return "@" + (pkgName == null ? "" : pkgName + ":") + name + "{" + nameValuePairs + "}";
    }
    
    
    /**
     * Builds an Annotation from parser events.
     *
     * @since 0.8.0
     */
    public static class AnnotationBuilder {
        private NodeLocation location;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;
        private String name;
        private String pkgName;
        private String pkgPath;
        private Map<String, AnnotationAttributeValue> attributeNameValPairs = new HashMap<>();

        /**
         * Set the source location of the annotation.
         * 
         * @param location Source location of the annotation
         */
        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }

        /**
         * Set the name of the annotation.
         * 
         * @param string Name of the annotation
         */
        public void setName(String string) {
            this.name = string;
        }

        /**
         * Set the package name of the annotation.
         * 
         * @param pkgName Package name of the annotation
         */
        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        /**
         * Set the package path of the annotation.
         * 
         * @param pkgPath Package path of the annotation
         */
        public void setPkgPath(String pkgPath) {
            this.pkgPath = pkgPath;
        }
        
        /**
         * Add a attribute name-value pair for the annotation.
         * 
         * @param name Name of the attribute
         * @param value Value of the attribute
         */
        public void addAttributeNameValuePair(String name, AnnotationAttributeValue value) {
            this.attributeNameValPairs.put(name, value);
        }

        public AnnotationAttachment build() {
            return new AnnotationAttachment(location, whiteSpaceDescriptor, name, pkgName,
                    pkgPath, attributeNameValPairs);
        }
    }


    /**
     * Get value of an annotation with a single literal attribute.
     * @return string value of the only attribute of the annotation.
     */
    public String getValue() {
        if (attributeNameValPairs.isEmpty()) {
            return null;
        }
        
        if (attributeNameValPairs.size() > 1) {
            throw new IllegalAccessError("annotation contains multiple attributes");
        }
        
        BValue literalVal = attributeNameValPairs.values().toArray(new AnnotationAttributeValue[0])[0]
                .getLiteralValue();
        if (literalVal != null) {
            return literalVal.stringValue();
        }
        
        return null;
    }
}
