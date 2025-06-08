/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.runtime.internal.utils;

import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.values.FPValue;

/**
 * Util methods required for handling function pointers.
 *
 * @since 2201.13.0
 */
public final class FunctionPointerUtils {

    @SuppressWarnings("unused")
    /*
     * Used for codegen fp calls to reduce jvm instructions.
     */
    public static Object callFP(FPValue fp, Strand strand) {
        return fp.function.apply(new Object[] {strand});
    }

}
