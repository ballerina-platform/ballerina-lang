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

/**
 * Generalized implementation of type check result cache. It is okay to access this from multiple threads but makes no
 * guarantee about the consistency of the cache under parallel access. Given result don't change due to race conditions
 * this should eventually become consistent.
 *
 * @since 2201.12.0
 */
public interface TypeCheckCache {

    Boolean cachedTypeCheckResult(CacheableTypeDescriptor other);

    void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result);

}
