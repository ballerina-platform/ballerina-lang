/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.table;

import io.ballerina.runtime.api.values.BTable;

/**
 * Extern function to check existence of key.
 * ballerina.model.table:hasKey(KeyType)
 *
 * @since 1.3.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.table", functionName = "hasKey",
//        args = {@Argument(name = "tbl", type = TypeKind.TABLE), @Argument(name = "key", type = TypeKind.ANYDATA)},
//        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
//        isPublic = true
//)
public class HasKey {

    @Deprecated
    public static boolean hasKey(BTable tbl, Object key) {
        return tbl.containsKey(key);
    }
}

