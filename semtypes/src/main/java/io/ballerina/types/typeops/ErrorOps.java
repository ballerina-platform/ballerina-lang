/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.typeops;

import io.ballerina.types.BasicTypeOps;
import io.ballerina.types.Bdd;
import io.ballerina.types.BddMemo;
import io.ballerina.types.Common;
import io.ballerina.types.Context;
import io.ballerina.types.SubtypeData;

/**
 * Basic type ops for error type.
 *
 * @since 2201.8.0
 */
public class ErrorOps extends CommonOps implements BasicTypeOps {

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        Bdd b = (Bdd) t;
        BddMemo mm = cx.mappingMemo.get(b);
        BddMemo m;
        if (mm == null) {
            m = BddMemo.from(b);
            cx.mappingMemo.put(m.bdd, m);
        } else {
            m = mm;
            BddMemo.MemoStatus res = m.isEmpty;
            if (res == BddMemo.MemoStatus.NOT_SET) {
                return true;
            } else {
                return res == BddMemo.MemoStatus.TRUE;
            }
        }
        boolean isEmpty = Common.bddEveryPositive(cx, b, null, null, MappingOps::mappingFormulaIsEmpty);
        m.setIsEmpty(isEmpty);
        return isEmpty;
    }
}
