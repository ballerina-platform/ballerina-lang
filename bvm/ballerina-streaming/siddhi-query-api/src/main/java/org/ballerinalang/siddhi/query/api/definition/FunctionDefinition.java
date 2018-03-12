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

package org.ballerinalang.siddhi.query.api.definition;

import org.ballerinalang.siddhi.query.api.SiddhiElement;

/**
 * Siddhi inline function definition.
 */
public class FunctionDefinition implements SiddhiElement {
    private static final long serialVersionUID = 42L;
    private String language;
    private String body;
    private String id;
    private Attribute.Type returnType;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public Attribute.Type getReturnType() {
        return returnType;
    }

    public String getLanguage() {
        return language;
    }

    public String getBody() {
        return body;
    }

    public String getId() {
        return id;
    }

    public FunctionDefinition language(String language) {
        this.language = language;
        return this;
    }

    public FunctionDefinition body(String body) {
        this.body = body;
        return this;
    }

    public FunctionDefinition id(String functionID) {
        this.id = functionID;
        return this;
    }

    public FunctionDefinition type(Attribute.Type type) {
        this.returnType = type;
        return this;
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
