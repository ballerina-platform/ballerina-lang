/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.values.BValue;

/**
 * {@code BType} represents a type in Ballerina.
 * <p>
 * Ballerina has variables of various types. The type system includes built-in primitive or value types,
 * a collection of built-in structured types, and arrays, record and iterator type constructors.
 * All variables of primitive types are allocated on the stack while all non-primitive types are
 * allocated on a heap using new.
 *
 * @since 0.8.0
 */
public abstract class BType implements BLangSymbol {
    protected String typeName;
    protected String pkgPath;
    protected SymbolName symbolName;
    protected SymbolScope symbolScope;
    protected Class<? extends BValue> valueClass;

    protected BType(SymbolScope symbolScope) {
        this.symbolScope = symbolScope;
    }

    protected BType(String typeName, String pkgPath, SymbolScope symbolScope, Class<? extends BValue> valueClass) {
        this.typeName = typeName;
        this.pkgPath = pkgPath;
        this.symbolName = new SymbolName(typeName, pkgPath);
        this.symbolScope = symbolScope;
        this.valueClass = valueClass;
    }

    @SuppressWarnings("unchecked")
    <V extends BValue> Class<V> getValueClass() {
        return (Class<V>) valueClass;
    }

    public abstract <V extends BValue> V getDefaultValue();

    public String toString() {
        return (pkgPath != null) ? pkgPath + ":" + typeName : typeName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BType) {
            BType other = (BType) obj;
            boolean namesEqual = this.typeName.equals(other.getName());

            // If both package paths are null or both package paths are not null,
            //    then check their names. If not return false
            return (this.pkgPath == null && other.getPackagePath() == null ||
                    this.pkgPath != null && other.getPackagePath() != null) && namesEqual;
        }
        return false;
    }

    public int hashCode() {
        return (pkgPath + ":" + typeName).hashCode();
    }

    // Methods in BLangSymbol interface

    @Override
    public String getName() {
        return typeName;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return symbolScope;
    }
}
