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
package org.ballerinalang.model.types;

import org.ballerinalang.model.NodeLocation;

/**
 * {@code BuiltinTypeName} represents a builtin type name in Ballerina.
 * <p/>
 * e.g. int, boolean, json[], int[].
 *
 * @since 0.92
 */
public class BuiltinTypeName extends SimpleTypeName {

    public BuiltinTypeName(String name) {
        this.name = name;
    }

    public BuiltinTypeName(String name, int dimensions) {
        this(name);
        this.arrayType = true;
        this.dimensions = dimensions;
    }

    public BuiltinTypeName(NodeLocation location, String name) {
        this(name);
        this.location = location;
    }

    public BuiltinTypeName(NodeLocation location, String name, int dimensions) {
        this(name, dimensions);
        this.location = location;
    }

    public void setArrayType(int dimensions) {
        this.arrayType = true;
        this.dimensions = dimensions;
    }

    public BType resolveBType(TypeNameResolver typeNameResolver) {
        return typeNameResolver.resolve(this);
    }
}
