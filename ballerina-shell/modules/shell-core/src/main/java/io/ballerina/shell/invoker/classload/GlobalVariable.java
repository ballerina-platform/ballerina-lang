/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.shell.invoker.classload;

import io.ballerina.shell.utils.Identifier;

import java.util.Objects;

/**
 * A global variable in the REPL.
 * Needs information such as variable name, its code and
 * whether it is an any/error/(any|error)
 *
 * @since 2.0.0
 */
public class GlobalVariable {
    private final String type;
    private final Identifier variableName;
    private final boolean isAssignableToAny;
    private final String qualifiersAndMetadata;
    private final boolean isDeclaredWithVar;

    public GlobalVariable(String type, boolean isDeclaredWithVar, Identifier variableName,
                          boolean isAssignableToAny, String qualifiersAndMetadata) {
        this.type = Objects.requireNonNull(type);
        this.isAssignableToAny = isAssignableToAny;
        this.qualifiersAndMetadata = Objects.requireNonNull(qualifiersAndMetadata);
        this.variableName = variableName;
        this.isDeclaredWithVar = isDeclaredWithVar;
    }

    public String getType() {
        return this.type;
    }

    public boolean isDeclaredWithVar() {
        return this.isDeclaredWithVar;
    }

    public Identifier getVariableName() {
        return variableName;
    }

    public boolean isAssignableToAny() {
        return isAssignableToAny;
    }

    public String getQualifiersAndMetadata() {
        return qualifiersAndMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GlobalVariable that = (GlobalVariable) o;
        return variableName.equals(that.variableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName);
    }

    @Override
    public String toString() {
        String elevType = isAssignableToAny ? "any" : "any|error";
        return String.format("<%s> %s %s %s", elevType, qualifiersAndMetadata, type, variableName);
    }
}
