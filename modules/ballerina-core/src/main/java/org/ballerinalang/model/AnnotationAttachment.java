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

package org.ballerinalang.model;

import java.util.HashMap;
import java.util.Map;

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
    private SymbolName symbolName;
    private Map<SymbolName, AnnotationAttributeValue> attributeNameValPairs = new HashMap<>();
    private NodeLocation location;
    AttachmentPoint attachedPoint;
    
    public AnnotationAttachment(NodeLocation location, SymbolName name, String pkgName, String pkgPath,
            Map<SymbolName, AnnotationAttributeValue> fieldValPairs) {
        this.location = location;
        this.symbolName = name;
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

    public String getPkgName() {
        return pkgName;
    }

    public String getPkgPath() {
        return pkgPath;
    }
    
    /**
     * Get symbol name of the annotation.
     *
     * @return symbol name of the annotation
     */
    public SymbolName getSymbolName() {
        return this.symbolName;
    }

    /**
     * Get Key-Value pairs of fields in the annotation.
     *
     * @return all Key-Value pairs
     */
    public Map<SymbolName, AnnotationAttributeValue> getAttributeNameValuePairs() {
        return attributeNameValPairs;
    }

    /**
     * Get the value of the Key-Value pair.
     *
     * @param attributeName attribute name
     * @return value of the attribute
     */
    public AnnotationAttributeValue getValue(String attributeName) {
        return attributeNameValPairs.get(attributeName);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }
    
    /**
     * Set the construct where this annotation is attached.
     * 
     * @param attachedPoint 
     */
    public void setAttachedPoint(AttachmentPoint attachedPoint) {
        this.attachedPoint = attachedPoint;
    }
    
    /**
     * Get the construct where this annotation is attached.
     * 
     * @return the attachedPoint
     */
    public AttachmentPoint getAttachedPoint() {
        return attachedPoint;
    }

    /**
     * Builds an Annotation from parser events.
     *
     * @since 0.8.0
     */
    public static class AnnotationBuilder {
        private NodeLocation location;
        private SymbolName name;
        private String pkgName;
        private String pkgPath;
        private Map<SymbolName, AnnotationAttributeValue> attributeNameValPairs = new HashMap<>();

        /**
         * Set the source location of the annotation.
         * 
         * @param location Source location of the annotation
         */
        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        /**
         * Set the name of the annotation.
         * 
         * @param name Name of the annotation
         */
        public void setName(SymbolName name) {
            this.name = name;
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
        public void addAttributeNameValuePair(SymbolName name, AnnotationAttributeValue value) {
            this.attributeNameValPairs.put(name, value);
        }

        public AnnotationAttachment build() {
            return new AnnotationAttachment(location, name, pkgName, pkgPath, attributeNameValPairs);
        }
    }
}
