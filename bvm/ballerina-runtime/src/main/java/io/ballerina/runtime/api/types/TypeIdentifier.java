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

package io.ballerina.runtime.api.types;

import io.ballerina.runtime.api.Module;

/**
 * Identifier that can be used to uniquely identify a named type.
 *
 * @param pkg      Ballerina module in which type was defined in
 * @param typeName Name of the type
 * @since 2201.12.0
 */
public record TypeIdentifier(Module pkg, String typeName) {

    public TypeIdentifier {
        assert typeName != null;
        assert pkg != null;
    }
}
