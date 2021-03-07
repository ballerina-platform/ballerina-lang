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

/**
 * Type after elevating the type of the visited node.
 * If not required, the type is fully visible.
 * Otherwise it would be one of any, error, any|error
 *
 * @since 2.0.0
 */
public enum ElevatedType {
    NONE("()"),
    ANY("any"),
    ERROR("error"),
    ANY_ERROR("(any|error)");

    private final String repr;

    ElevatedType(String repr) {
        this.repr = repr;
    }

    /**
     * NONE or ANY types can be assigned to any.
     *
     * @return Whether the variable that has this type
     * can be assigned to any type.
     */
    public boolean isAssignableToAny() {
        return this.equals(NONE) || this.equals(ANY);
    }

    @Override
    public String toString() {
        return repr;
    }
}
