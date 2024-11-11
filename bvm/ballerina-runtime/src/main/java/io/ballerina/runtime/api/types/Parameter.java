/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.api.types;

/**
 * {@code Parameter} represents the parameter of a function in ballerina.
 *
 * @since 2.0
 */
public class Parameter {
    public final String name;
    public final boolean isDefault;
    public final String defaultFunctionName;
    public Type type;

    public Parameter(String name, boolean isDefault, String defaultFunctionName, Type type) {
        this.name = name;
        this.isDefault = isDefault;
        this.defaultFunctionName = defaultFunctionName;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Parameter that)) {
            return false;
        }

        return this.name.equals(that.name) && this.type.equals(that.type) && this.isDefault == that.isDefault;
    }

}
