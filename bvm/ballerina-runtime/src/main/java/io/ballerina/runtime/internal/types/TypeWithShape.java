/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.Optional;

/**
 * Types that are not basic types and have values whose shape could be different form the actual type (i.e. not handles)
 * must implement this interface. Note that multiple values could share the same instance of TypeWithShape. Ideally
 * different objects should be able to do their shape calculations in a non-blocking manner, even when they share the
 * same instance of {@code TypeWithShape}.
 *
 * @since 2201.12.0
 */
public interface TypeWithShape extends TypeWithAcceptedType {

    Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object);

    Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object);

    boolean couldInherentTypeBeDifferent();
}