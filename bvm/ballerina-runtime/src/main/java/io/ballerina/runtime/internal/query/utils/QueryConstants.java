/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

public final class QueryConstants {
    public static final BString VALUE_FIELD = StringUtils.fromString("value");
    public static final BString ERROR_FIELD = StringUtils.fromString("$error$");
    public static final BString VALUE_ACCESS_FIELD = StringUtils.fromString("$value$");
    public static final BString ORDER_KEY = StringUtils.fromString("$orderKey$");
    public static final BString ORDER_DIRECTION = StringUtils.fromString("$orderDirection$");
    public static final BString EMPTY_BSTRING = StringUtils.fromString("");
    public static final int GROUP_KEY_CONSTANT = 1;
}
