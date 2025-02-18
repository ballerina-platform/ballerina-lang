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

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;

/**
 * Represent TypeDescriptors whose type check results can be cached.
 *
 * @since 2201.12.0
 */
public interface CacheableTypeDescriptor extends Type {

    /**
     * Check whether the type check result of this type descriptor is cached for the given type descriptor.
     *
     * @param cx    Context in which the type check is performed
     * @param other Type descriptor to check the cached result for
     * @return Result of the type check if it is cached, {@code null}  otherwise
     */
    Boolean cachedTypeCheckResult(Context cx, CacheableTypeDescriptor other);

    /**
     * Cache the type check result of this type descriptor for the given type descriptor. Note that implementations of
     * this method could choose to not cache the result.
     *
     * @param other  Type descriptor to cache the result for
     * @param result Result of the type check
     */
    void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result);

    int typeId();
}
