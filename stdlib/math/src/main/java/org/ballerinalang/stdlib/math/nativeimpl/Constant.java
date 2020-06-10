/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.math.nativeimpl;

import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Constants related to math operations.
 */
public class Constant {

    static final String MATH_ERROR = "ArithmeticError";

    public static final String ILLEGAL_ARGUMENT_ERROR_MSG = "End range must be greater than the start range";

    public static final String DIVIDE_BY_ZERO_ERROR_MSG = "Division by zero occurred";

    public static final String OVERFLOW_ERROR_MSG = "Overflow occurred";

    private static final String PACKAGE_NAME = "math";

    static final BPackage MATH_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, PACKAGE_NAME, "1.0.0");

}
