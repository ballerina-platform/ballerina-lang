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

import io.ballerina.runtime.internal.types.BTypeIdSet;

/**
 * {@code BObjectType} represents a user defined object type in Ballerina.
 *
 * @since 2.0.0
 */
public interface ObjectType extends StructureType, SelectivelyImmutableReferenceType {

    void setMethods(MethodType[] methodTypes);

    MethodType[] getMethods();

    /**
     * Provides given @{@link ObjectType} is isolated.
     *
     * @return true if object is isolated otherwise false.
     */
    boolean isIsolated();

    /**
     * Provides given @{@link ObjectType} method is isolated.
     *
     * @param methodName method name
     * @return true if @{@link ObjectType} method is isolated otherwise false.
     */
    boolean isIsolated(String methodName);

    /**
     * Provides copy of type ids of the object
     *
     * @return copy of type id set
     */
    BTypeIdSet getTypeIdSet();

}
