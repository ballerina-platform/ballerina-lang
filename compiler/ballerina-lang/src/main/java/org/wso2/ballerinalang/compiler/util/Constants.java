/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.ballerinalang.compiler.util;

/**
 * Class that holds constant values.
 *
 * since 0.980.0
 */
public class Constants {

    private Constants() {
    }

    public static final int INFERRED_ARRAY_INDICATOR = -2;
    public static final int OPEN_ARRAY_INDICATOR = -1;

    public static final String MAIN_FUNCTION_NAME = "main";
    public static final String WORKER_LAMBDA_VAR_PREFIX = "0";

    public static final String SKIP_TESTS = "false";
    public static final String OPEN_SEALED_ARRAY = "*";

    public static final int INIT_METHOD_SPLIT_SIZE = 50;

    public static final int MIN_UNICODE = 0xD800;
    public static final int MIDDLE_LIMIT_UNICODE = 0xDFFF;
    public static final int MAX_UNICODE = 0x10FFFF;

    public static final String DESUGARED_MAPPING_CONSTR_KEY = "$mapping$var$"; // TODO: 5/2/20 remove 

    public static final String REMOVE_IF_HAS_KEY = "removeIfHasKey()";

    public static final String REMOVE = "remove()";

    public static final String STRING_TYPE = "string";

}
