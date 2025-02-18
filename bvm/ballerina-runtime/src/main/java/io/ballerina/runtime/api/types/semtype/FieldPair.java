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
 * Represent the matching fields types of two mapping atomic types.
 *
 * @param name   name of the field
 * @param type1  type of the field in the first mapping
 * @param type2  type of the field in teh second mapping
 * @param index1 corresponding index of the field in the first mapping. If matching field is rest value is {@code null}
 * @param index2 corresponding index of the field in the second mapping. If matching field is rest value is
 *               {@code null}
 * @since 2201.12.0
 */
public record FieldPair(String name, SemType type1, SemType type2, Integer index1, Integer index2) {

}
