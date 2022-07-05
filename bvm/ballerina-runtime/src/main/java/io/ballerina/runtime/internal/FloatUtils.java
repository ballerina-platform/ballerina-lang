/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Common utility methods used for float operations.
 *
 * @since 2201.1.0
 */
public class FloatUtils {

    public static BString getBStringIfInfiniteOrNaN(double x) {
        if (Double.isInfinite(x) || Double.isNaN(x)) {
            return StringUtils.fromString(StringUtils.getStringValue(x, null));
        }
        return null;
    }

    public static boolean checkFractionDigitsWithinRange(long noOfFractionDigits) {
        return noOfFractionDigits > Integer.MAX_VALUE ||
                (noOfFractionDigits * Long.bitCount(noOfFractionDigits) > Integer.MAX_VALUE);
    }
}
