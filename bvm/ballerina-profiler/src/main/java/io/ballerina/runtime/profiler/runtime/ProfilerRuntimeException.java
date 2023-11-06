/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.profiler.runtime;

/**
 * This class is used as a runtime exception class for profiler.
 *
 * @since 2201.8.0
 */
public class ProfilerRuntimeException extends RuntimeException {

    public ProfilerRuntimeException(String message) {
        super(message);
    }

    public ProfilerRuntimeException(String message, Throwable e) {
        super(message, e);
    }
}
