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

package io.ballerina.runtime.api.types.semtype;

// SEMTYPE-TODO: revisit this after fully implementing semtypes. Added this to match nBallerina where this is just a
//   type alias to int. Maybe not needed here due to the way we have modeled type hierarchy (need to check if doing
//   instancof checks on this is faster than checking if some is 0)

/**
 * Represents a union of basic types.
 *
 * @since 2201.10.0
 */
public interface BasicTypeBitSet {

    default int some() {
        return 0;
    }

    default SubType[] subTypeData() {
        return new SubType[0];
    }
}
