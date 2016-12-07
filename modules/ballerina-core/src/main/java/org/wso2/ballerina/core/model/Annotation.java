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

package org.wso2.ballerina.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Annotation} represents an Annotation in Ballerina.
 * <p>
 * Annotation can be associated with various Ballerina concepts like Service, Resource, Functions, etc.
 *
 * @see <a href="https://github.com/wso2/ballerina/blob/master/docs/SyntaxSummary.md">Ballerina Syntax Summary</a>
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Annotation {

    private String name, value;
    private Map<String, String> keyValPairs = new HashMap<>();

    public Annotation(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Annotation(String name, Map<String, String> keyValPairs) {
        this.name = name;
        this.keyValPairs = keyValPairs;
    }

    /**
     * Get name of the annotation
     *
     * @return name of the annotation
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value of the annotation
     *
     * @return value of the annotation
     */
    public String getValue() {
        return value;
    }

    /**
     * Get Key-Value pairs in the annotation
     *
     * @return all Key-Value pairs
     */
    public Map getKeyValuePairs() {
        return keyValPairs;
    }

    /**
     * Get the value of the Key-Value pair
     *
     * @param key key
     * @return value of the Key-Value pair
     */
    public String getValueOfKeyValuePair(String key) {
        return keyValPairs.get(key);
    }

}
