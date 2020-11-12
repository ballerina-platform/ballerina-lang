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

import io.ballerina.runtime.api.async.StrandMetadata;

import java.util.function.Function;

/**
 * <p>
 * Ballerina runtime value representation of a function pointer.
 * </p>
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 * @since 1.1.0
 */
public interface BFunctionPointer<T, R> extends BValue {

    /**
     * Execute the {@code Function} with given parameter array. Method can be used to call function pointer from
     * ballerina. This will directly invoke the function pointer without using ballerina scheduler. If function
     * pointer can have async code, then need to use @asyncCall method.
     *
     * @param t {@code Function to be executed}
     * @return The result of the executed function.
     */
    R call(T t);

    /**
     * Schedule and asynchronously execute the {@code Function} with given parameter array. Method can be used to
     * call function pointer from native function. This supports function pointers with async ballerina code.
     *
     * @param args  Function arguments.
     * @param metaData  meta data for newly creating strand which is used to execute the function pointer.
     * @return Future value received from invoking asynchronous function.
     */
    BFuture asyncCall(Object[] args, StrandMetadata metaData);

    /**
     * Schedule and asynchronously execute the {@code Function} with given parameter array. Method can be used to
     * call function pointer from native function. This supports function pointers with async ballerina code.
     *
     * @param args                 Function arguments.
     * @param resultHandleFunction Function used to process the result received after execution of function.
     * @param metaData             meta data for newly creating strand which is used to execute the function pointer.
     * @return Future value received from invoking asynchronous function.
     */
    BFuture asyncCall(Object[] args, Function<Object, Object> resultHandleFunction, StrandMetadata metaData);

    /**
     * Returns the {@code Function} the FP is pointed to.
     *
     * @return {@code Function}
     */
    Function<T, R> getFunction();
}
