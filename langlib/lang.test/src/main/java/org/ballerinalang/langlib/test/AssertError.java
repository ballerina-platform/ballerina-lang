/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.test;

import io.ballerina.jvm.TypeChecker;
import io.ballerina.jvm.api.BErrorCreator;
import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.TypeTags;

/**
 * Native implementation of assertError(anydata|error value).
 *
 * @since 1.3.0
 */
public class AssertError {
    public static void assertError(Object value) {
        if (TypeChecker.getType(value).getTag() != TypeTags.ERROR_TAG) {
            throw BErrorCreator.createError(BStringUtils.fromString("{ballerina/lang.test}AssertionError"),
                                            BStringUtils.fromString("expected an error type"));
        }
    }
}
