/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.test.semtype;

import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import org.testng.annotations.Test;

// These are temporary sanity checks until we have actual types using cell types are implemented
public class CoreTests {

    @Test
    public void testCellTypes() {
        Env env = Env.getInstance();
        Context cx = Context.from(env);
        SemType intTy = Builder.intType();
        SemType readonlyInt = Builder.cellContaining(env, intTy, CellAtomicType.CellMutability.CELL_MUT_NONE);
        assert Core.isSubType(cx, readonlyInt, readonlyInt);
        SemType mutableInt = Builder.cellContaining(env, intTy, CellAtomicType.CellMutability.CELL_MUT_UNLIMITED);
        assert Core.isSubType(cx, mutableInt, mutableInt);
        assert Core.isSubType(cx, readonlyInt, mutableInt);
        assert !Core.isSubType(cx, mutableInt, readonlyInt);
    }

    @Test
    public void testCellTypeCaching() {
        Env env = Env.getInstance();
        SemType intTy = Builder.intType();
        SemType readonlyInt1 = Builder.cellContaining(env, intTy, CellAtomicType.CellMutability.CELL_MUT_NONE);
        SemType readonlyInt2 = Builder.cellContaining(env, intTy, CellAtomicType.CellMutability.CELL_MUT_NONE);
        assert readonlyInt1 == readonlyInt2;
    }

    @Test
    public void testSimpleList() {
        Env env = Env.getInstance();
        SemType intTy = Builder.intType();
        // int[]
        ListDefinition ld = new ListDefinition();
        SemType intListTy =
                ld.defineListTypeWrapped(env, new SemType[0], 0, intTy,
                        CellAtomicType.CellMutability.CELL_MUT_UNLIMITED);

        // int[1]
        ListDefinition ld1 = new ListDefinition();
        SemType[] members = {intTy};
        SemType intListTy1 =
                ld1.defineListTypeWrapped(env, members, 1, Builder.neverType(),
                        CellAtomicType.CellMutability.CELL_MUT_UNLIMITED);

        Context cx = Context.from(env);
        assert Core.isSubType(cx, intListTy1, intListTy);
    }
}
