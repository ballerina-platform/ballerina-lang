/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.Runtime;

/**
 * <p>
 * Ballerina runtime value representation of a function pointer.
 * </p>
 *
 *
 * @since 1.1.0
 */
public interface BFunctionPointer extends BValue {

    /**
     * Execute the {@code Function} with given parameter array. Method can be used to call function pointer from
     * ballerina. This will directly invoke the function pointer without using ballerina scheduler. If function
     * pointer can have async code, then need to use @asyncCall method.
     *
     * @param runtime Current Runtime
     * @param t {@code Function to be executed}
     * @return The result of the executed function.
     */
    Object call(Runtime runtime, Object... t);
}
