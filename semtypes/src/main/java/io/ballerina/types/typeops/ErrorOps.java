/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package io.ballerina.types.typeops;

import io.ballerina.types.BasicTypeOps;
import io.ballerina.types.Context;
import io.ballerina.types.SubtypeData;

/**
 * Basic type ops for error type.
 *
 * @since 2201.8.0
 */
public class ErrorOps extends CommonOps implements BasicTypeOps {

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        // TODO: implement bddPosMaybeEmpty
        throw new UnsupportedOperationException("Unimplemented");
    }
}
