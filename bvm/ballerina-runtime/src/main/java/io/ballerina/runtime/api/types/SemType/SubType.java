/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.runtime.api.types.SemType;

import io.ballerina.runtime.internal.types.semtype.SubTypeData;

/**
 * Describe set of operation supported by each basic Type
 *
 * @since 2201.10.0
 */
public interface SubType {

    SubType union(SubType other);

    SubType intersect(SubType other);

    default SubType diff(SubType other) {
        return this.intersect(other.complement());
    }

    SubType complement();

    boolean isEmpty();

    boolean isAll();

    boolean isNothing();

    SubTypeData data();
}
