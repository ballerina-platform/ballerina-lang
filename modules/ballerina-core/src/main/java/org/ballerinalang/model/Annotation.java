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
public class Annotation implements Node {

    // TODO Refactor these instance variables
    private String name;
    private SymbolName symbolName;
    private String value;
    private Map<String, String> keyValPairs = new HashMap<>();
    private Map<SymbolName, String> elementPair = new HashMap<>();
    private NodeLocation location;

    public Annotation(NodeLocation location, SymbolName name, String value, Map<SymbolName, String> keyValPairs) {
        this.location = location;
        this.symbolName = name;
        this.value = value;
        this.elementPair = keyValPairs;
    }

    /**
     * Get name of the annotation.
     *
     * @return name of the annotation
     */
    public String getName() {
        if (this.name != null) {
            return name;
        } else {
            return this.symbolName.getName();
        }
    }

    /**
     * Get the value of the annotation.
     *
     * @return value of the annotation
     */
    public String getValue() {
        return value;
    }

    /**
     * Get Key-Value pairs in the annotation.
     *
     * @return all Key-Value pairs
     */
    public Map getKeyValuePairs() {
        return keyValPairs;
    }

    /**
     * Get the value of the Key-Value pair.
     *
     * @param key key
     * @return value of the Key-Value pair
     */
    public String getValueOfKeyValuePair(String key) {
        return keyValPairs.get(key);
    }

    /**
     * Get all element pairs defined with an annotation.
     * @return all element paris with key-values.
     */
    public Map getElementPairs() {
        return elementPair;
    }

    /**
     * Get the value of the symbol in an annotation.
     * @param symbolName key of the element
     * @return value of the element
     */
    public String getValueOfElementPair(SymbolName symbolName) {
        return elementPair.get(symbolName);
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
     * Builds an Annotation from parser events.
     *
     * @since 0.8.0
     */
    public static class AnnotationBuilder {
        private NodeLocation location;
        private SymbolName name;
        private String value;
        private Map<SymbolName, String> keyValPairs = new HashMap<>();

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setName(SymbolName name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void addKeyValuePair(SymbolName key, String value) {
            this.keyValPairs.put(key, value);
        }

        public Annotation build() {
            return new Annotation(location, name, value, keyValPairs);
        }
    }
}
