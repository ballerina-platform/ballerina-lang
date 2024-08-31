/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.PureSemType;

public interface SemType {

    static SemType from(int all, int some, SubType[] subTypeData) {
        return new PureSemType(all, some, subTypeData);
    }

    static SemType from(int all) {
        return new PureSemType(all);
    }

    int all();

    int some();

    SubType[] subTypeData();

    CachedResult cachedSubTypeRelation(SemType other);

    void cacheSubTypeRelation(SemType other, boolean result);

    SubType subTypeByCode(int code);

    public enum CachedResult {
        TRUE,
        FALSE,
        NOT_FOUND
    }
}
