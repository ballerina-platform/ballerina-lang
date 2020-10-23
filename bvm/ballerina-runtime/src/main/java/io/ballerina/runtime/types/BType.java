/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.types;

import io.ballerina.runtime.IdentifierUtils;
import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.util.exceptions.BallerinaException;

import java.util.Objects;

/**
 * {@code BType} represents a type in Ballerina.
 * <p>
 * Ballerina has variables of various types. The type system includes built-in primitive or value types,
 * a collection of built-in structured types, and arrays, record and iterator type constructors.
 * All variables of primitive types are allocated on the stack while all non-primitive types are
 * allocated on a heap using new.
 *
 * @since 0.995.0
 */
public abstract class BType implements Type {
    protected String typeName;
    protected Module pkg;
    protected Class<? extends Object> valueClass;
    private int hashCode;

    protected BType(String typeName, Module pkg, Class<? extends Object> valueClass) {
        this.typeName = IdentifierUtils.decodeIdentifier(typeName);
        this.pkg = pkg;
        this.valueClass = valueClass;
        if (pkg != null && typeName != null) {
            this.hashCode = Objects.hash(pkg, typeName);
        }
    }

    @SuppressWarnings("unchecked")
    public <V extends Object> Class<V> getValueClass() {
        return (Class<V>) valueClass;
    }

    /**
     * Get the default value of the type. This is the value of an uninitialized variable of this type.
     * For value types, this is same as the value get from {@code BType#getInitValue()}.
     *
     * @param <V> Type of the value
     * @return Default value of the type
     */
    public abstract <V extends Object> V getZeroValue();

    /**
     * Get the empty initialized value of this type. For reference types, this is the value of a variable,
     * when initialized with the empty initializer.
     * For value types, this is same as the default value (value get from {@code BType#getDefaultValue()}).
     *
     * @param <V> Type of the value
     * @return Init value of this type
     */
    public abstract <V extends Object> V getEmptyValue();

    public abstract int getTag();

    public String toString() {
        return (pkg == null || pkg.getName() == null || pkg.getName().equals(".")) ? typeName :
                pkg.getName() + ":" + typeName;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof BType) {
            BType other = (BType) obj;

            if (!this.typeName.equals(other.getName())) {
                return false;
            }

            Module thisModule = this.pkg;
            Module otherModule = other.pkg;

            if (thisModule == null) {
                return otherModule == null;
            }

            if (otherModule == null) {
                return false;
            }

            if (hasAllNullConstituents(thisModule)) {
                return hasAllNullConstituents(otherModule);
            }

            if (hasAllNullConstituents(otherModule)) {
                return false;
            }

            if (thisModule.getVersion() == null || otherModule.getVersion() == null) {
                return thisModule.getOrg().equals(otherModule.getOrg()) &&
                        thisModule.getName().equals(otherModule.getName());
            }

            return thisModule.equals(otherModule);
        }
        return false;
    }

    public boolean isNilable() {
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    public String getName() {
        return typeName;
    }

    public String getQualifiedName() {
        return pkg == null ? typeName : pkg.toString() + ":" + typeName;
    }

    public Module getPackage() {
        return pkg;
    }

    public boolean isPublic() {
        return false;
    }

    public boolean isNative() {
        return false;
    }

    public boolean isAnydata() {
        return this.getTag() <= TypeTags.ANYDATA_TAG;
    }

    public boolean isPureType() {
        return this.getTag() == TypeTags.ERROR_TAG || this.isAnydata();
    }

    public boolean isReadOnly() {
        return false;
    }

    public Type getImmutableType() {
        if (TypeChecker.isInherentlyImmutableType(this)) {
            return this;
        }

        // Selectively immutable types override this method.
        throw new BallerinaException(this.typeName + " cannot be immutable");
    }

    public void setImmutableType(IntersectionType immutableType) {
        // Do nothing since already set.
        // For types that immutable type may be set later, the relevant type overrides this method.
    }

    private boolean hasAllNullConstituents(Module module) {
        return module.getOrg() == null && module.getName() == null && module.getVersion() == null;
    }

    public Module getPkg() {
        return pkg;
    }
}
