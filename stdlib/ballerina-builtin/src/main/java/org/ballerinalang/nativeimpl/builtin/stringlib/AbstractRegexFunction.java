/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.builtin.stringlib;

import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;

import java.util.regex.Pattern;

/**
 * Abstract native function which serves as base class for writing Regex related native functions.
 */
public abstract class AbstractRegexFunction extends BlockingNativeCallableUnit {

    /**
     * Validate Regex struct in Ballerina. Validate whether the string is in compiled form if not
     * compile it and store as native data for later reference.
     *
     * @param regex a regex string
     * @return Pattern compiled Pattern
     */
    protected Pattern validatePattern(String regex) {
        return Pattern.compile(regex);
    }
}
