/*
*  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.Module;

/**
 * {@code Type} represents a type in Ballerina.
 * <p>
 * Ballerina has variables of various types. The type system includes built-in primitive or value types,
 * a collection of built-in structured types, and arrays, record and iterator type constructors.
 * All variables of primitive types are allocated on the stack while all non-primitive types are
 * allocated on a heap using new.
 *
 * @since 2.0.0
 */
public interface Type {

    // TODO: remove default implementations when standard library types are updated
    /**
     * Set the referred type for this type once it has been calculated. This must be called the first time this
     * calculation is done for {@code Type#getReferredTypeCache()} to work properly. This is non-blocking and
     * will eventually become consistent. Expect {@code TypeUtils#getReferredType(Type)} to be referentially
     * transparent.
     *
     * @param type Type referred by this type. For non-reference types, this is the same type.
     */
    default void setCachedReferredType(Type type) {
    }
    /**
     * Get the type referred by this type if it has been already calculated. If it has not been already calculated, it
     * will return null. For non-reference types, this will return the same type. This is non-blocking and will
     * eventually become consistent. Expect {@code TypeUtils#getReferredType(Type)} to be referentially transparent.
     *
     * @return Referred type of the type
     */
    default Type getCachedReferredType() {
        return null;
    }

    /**
     * Set the implied type for this type once it has been calculated. This must be called the first time this
     * calculation is done for {@code Type#getImpliedTypeCache()} to work properly. This is non-blocking and
     * will eventually become consistent. Expect {@code TypeUtils#getImpliedType(Type)} to be referentially transparent.
     *
     * @param type Type implied by this type. For non-intersection types, this is the same type.
     */
    default void setCachedImpliedType(Type type) {
    }

    /**
     * Get the type implied by this type if it has been already calculated. If it has not been already calculated, it
     * will return null. For non-intersection types, this will return the same type. This is non-blocking and will
     * eventually become consistent. Expect {@code TypeUtils#getImpliedType(Type)} to be referentially transparent.
     *
     * @return Implied type of the type
     */
    default Type getCachedImpliedType() {
        return null;
    }

    /**
     * Get the default value of the type. This is the value of an uninitialized variable of this type.
     * For value types, this is same as the value get from {@code BType#getInitValue()}.
     *
     * @param <V> Type of the value
     * @return Default value of the type
     */
    <V extends Object> V getZeroValue();

    /**
     * Get the empty initialized value of this type. For reference types, this is the value of a variable,
     * when initialized with the empty initializer.
     * For value types, this is same as the default value (value get from {@code BType#getDefaultValue()}).
     *
     * @param <V> Type of the value
     * @return Init value of this type
     */
    <V extends Object> V getEmptyValue();

    int getTag();

    String toString();

    boolean equals(Object obj);

    boolean isNilable();

    int hashCode();

    String getName();

    String getQualifiedName();

    Module getPackage();

    boolean isPublic();

    boolean isNative();

    boolean isAnydata();

    boolean isPureType();

    boolean isReadOnly();

    long getFlags();

   /*
    * @deprecated Cast to @SelectivelyImmutableReferenceType and use getImmutableType.
    */
   @Deprecated
    Type getImmutableType();

    /*
     * @deprecated Cast to @SelectivelyImmutableReferenceType and use setImmutableType.
     */
    @Deprecated
    void setImmutableType(IntersectionType immutableType);

    /*
     * @deprecated use {@link Type#getPackage(Object, Type)} instead
     */
    // TODO: https://github.com/ballerina-platform/ballerina-lang/issues/42113
    @Deprecated
    Module getPkg();
}
