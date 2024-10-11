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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.SubType;

/**
 * Represents the corresponding subtype pairs of two semtypes.
 *
 * @param typeCode the type code of the semtype
 * @param subType1 the first subtype. This will if the first semtype don't have
 *                 this subtype
 * @param subType2 the second subtype. This will if the second semtype don't
 *                 have this subtype
 * @since 2201.11.0
 */
public record SubtypePair(int typeCode, SubType subType1, SubType subType2) {

    public SubtypePair {
        if (subType1 == null && subType2 == null) {
            throw new IllegalArgumentException("both subType1 and subType2 cannot be null");
        }
    }
}
