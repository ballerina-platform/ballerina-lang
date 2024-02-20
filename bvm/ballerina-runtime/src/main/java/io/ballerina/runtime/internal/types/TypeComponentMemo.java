/*
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package io.ballerina.runtime.internal.types;

import io.ballerina.types.SemType;

/**
 * For most types we can calculate the {@code SemType} and {@code BType} components at construction time. But for types
 * such as unions whose members are mutable we have to recalculate these components every time we need them which is
 * expensive. As a compromise we will memoize the result first time we calculate it since we don't modify the type in
 * the middle of a type check
 *
 * @param semTypeComponent {@code SemType} part of the BType. For types whose type is only partially implemented as a
 *                                        {@code SemType} this will be just that part. For types where the type is fully
 *                                        implemented as a {@code SemType} this will contain the whole type. For those
 *                                        that haven't implemented at all as a {@code SemType} this will be the
 *                                        {@code NEVER} type
 * @param bTypeComponent {@code BType} part of the BType. For types whose type is only partially implemented as a
 *                                        {@code SemType} this will be the rest of the type. For types where the type is
 *                                        not implemented at all as {@code SemType} this will be the same as that type.
 *                                        For types which are fully implemented as a {@code SemType} this will be the
 *                                        {@code NEVER} type
 * @since 2201.9.0
 */
record TypeComponentMemo(SemType semTypeComponent, BType bTypeComponent) {

}
