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

/**
 * Any {@code Type} that contains selectively immutable types must implement this interface. It represents the type
 * against which {@code isLikeType} operation is performed.
 *
 * @since 2201.12.0
 */
public interface TypeWithAcceptedType {

    SemType acceptedTypeOf(Context cx);
}
