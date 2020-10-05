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
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.symbols.BLangSymbol;
import org.ballerinalang.core.model.values.BValue;

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
    protected Class<? extends BValue> valueClass;

    protected BType(String typeName, String pkgPath, Class<? extends BValue> valueClass) {
        this.typeName = typeName;
        this.pkgPath = pkgPath;
        this.valueClass = valueClass;
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> Class<V> getValueClass() {
        return (Class<V>) valueClass;
    }

    /**
     * Get the default value of the type. This is the value of an uninitialized variable of this type.
     * For value types, this is same as the value get from {@code BType#getInitValue()}.
     *
     * @param <V> Type of the value
     * @return Default value of the type
     */
    public abstract <V extends BValue> V getZeroValue();

    /**
     * Get the empty initialized value of this type. For reference types, this is the value of a variable,
     * when initialized with the empty initializer.
     * For value types, this is same as the default value (value get from {@code BType#getDefaultValue()}).
     *
     * @param <V> Type of the value
     * @return Init value of this type
     */
    public abstract <V extends BValue> V getEmptyValue();

    public abstract int getTag();
    
    public String toString() {
        return (pkgPath == null || pkgPath.equals(".")) ? typeName : pkgPath + ":" + typeName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BType) {
            BType other = (BType) obj;
            boolean namesEqual = this.typeName.equals(other.getName());

            // If both package paths are null or both package paths are not null,
            //    then check their names. If not return false
            if (this.pkgPath == null && other.getPackagePath() == null) {
                return namesEqual;
            } else if (this.pkgPath != null && other.getPackagePath() != null) {
                return this.pkgPath.equals(other.getPackagePath()) && namesEqual;
            }
        }
        return false;
    }

    public int hashCode() {
        return (pkgPath + ":" + typeName).hashCode();
    }

    public boolean isNilable() {
        return false;
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
}
