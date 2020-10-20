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

package org.ballerinalang.langlib.table;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BTable;

/**
 * Native implementation of lang.table:removeAll(table&lt;Type&gt;).
 *
 * @since 1.3.0
 */
public class RemoveAll {

    public static void removeAll(BTable tbl) {
        try {
            tbl.clear();
        } catch (io.ballerina.runtime.util.exceptions.BLangFreezeException e) {
            throw ErrorCreator.createError(StringUtils.fromString(e.getMessage()),
                                           StringUtils
                                                    .fromString("Failed to remove all from table: " + e.getDetail()));
        }
    }
}
