/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.ballerinalang.langlib.java;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

/**
 * Contains utility methods required to implement Java related Ballerina functions in ballerina/java module.
 *
 * @since 1.0.0
 */
class JValues {

    static BError getJavaNullReferenceError() {
        return BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.JAVA_NULL_REFERENCE_ERROR,
                RuntimeErrors.JAVA_NULL_REFERENCE);
    }

    static void rangeCheck(long index, Object[] arr) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                                                           RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        if (index < 0 || index >= arr.length) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                                                           RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, arr.length);
        }
    }
}
