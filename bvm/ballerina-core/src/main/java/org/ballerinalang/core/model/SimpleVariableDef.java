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

import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.SimpleTypeName;

/**
 * {@code VariableDef} represent a Variable definition.
 * <p>
 * Ballerina has variables of various types. The type system includes built-in primitives,
 * a collection of built-in structured types and arrays and record type constructors.
 *
 * @since 0.90
 */
public class SimpleVariableDef implements VariableDef {
    protected NodeLocation location;
    protected WhiteSpaceDescriptor whiteSpaceDescriptor;
    protected SimpleTypeName typeName;
    protected BType type;
    protected Kind kind;
    protected int memOffset;

    // BLangSymbol related attributes
    protected Identifier identifier;
    protected String pkgPath;
    protected boolean isPublic = false;
    protected boolean isNative = false;

    public SimpleTypeName getTypeName() {
        return typeName;
    }

    public BType getType() {
        return type;
    }

    public void setType(BType type) {
        this.type = type;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public int getVarIndex() {
        return memOffset;
    }

    @Override
    public void setVarIndex(int index) {
        this.memOffset = index;
    }

    // Methods in BLangSymbol interface

    @Override
    public String getName() {
        //There are scenarios where identifier can be null
        //in a variabledef (parameterDef in returns) hence the null check
        if (identifier != null) {
            return identifier.getName();
        }
        return null;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public boolean isNative() {
        return isNative;
    }

    // Methods in Node interface

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }
}
