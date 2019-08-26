/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;

import java.util.HashMap;

public class CloneUtils {

    /**
     * Returns a clone of `value`. A clone is a deep copy that does not copy immutable subtrees.A clone can therefore
     * safely be used concurrently with the original. It corresponds to the Clone(v) abstract operation, defined in
     * the Ballerina Language Specification.
     * @param value The value on which the function is invoked
     * @return String value of the value
     */
    public static Object cloneValue(Object value) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof RefValue)) {
            return value;
        }

        RefValue refValue = (RefValue) value;

        if (refValue.getType().getTag() == TypeTags.ERROR_TAG ||
            !TypeChecker.checkIsLikeType(refValue, BTypes.typePureType)) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.CLONE_ERROR, RuntimeErrors.
                    UNSUPPORTED_CLONE_OPERATION, refValue.getType());
        }

        return refValue.copy(new HashMap<>());
    }
}
