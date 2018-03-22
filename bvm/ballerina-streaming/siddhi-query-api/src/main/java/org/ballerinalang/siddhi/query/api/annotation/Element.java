/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.siddhi.query.api.annotation;

import org.ballerinalang.siddhi.query.api.SiddhiElement;

/**
 * Annotation element.
 */
public class Element implements SiddhiElement {

    private static final long serialVersionUID = 1L;
    private final String key;
    private final String value;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public Element(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (key != null) {
            return key + " = '" + value + "'";
        } else {
            return "'" + value + "'";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Element)) {
            return false;
        }

        Element element = (Element) o;

        if (key != null ? !key.equals(element.key) : element.key != null) {
            return false;
        }
        if (value != null ? !value.equals(element.value) : element.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    @Override
    public void setQueryContextStartIndex(int[] lineAndColumn) {
        queryContextStartIndex = lineAndColumn;
    }

    @Override
    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public void setQueryContextEndIndex(int[] lineAndColumn) {
        queryContextEndIndex = lineAndColumn;
    }
}
