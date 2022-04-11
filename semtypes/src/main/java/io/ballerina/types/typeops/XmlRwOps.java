/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.types.typeops;

import io.ballerina.types.Bdd;
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.XmlSubtype;

/**
 * xml read/write specific methods.
 *
 * @since 3.0.0
 */
public class XmlRwOps extends XmlCommonOps {

    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        return commonUnion(false, t1, t2);
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        return commonComplement(false, t);
    }


    boolean xmlBddEmpty(Context cx, Bdd bdd) {
        return Common.bddEvery(cx, bdd, null, null, XmlRwOps::xmlFormulaIsEmpty);
    }

    private static boolean xmlFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        int rwOnlyBits = collectAllBits(pos) & XmlSubtype.XML_PRIMITIVE_RW_MASK;
        return hasTotalNegative(rwOnlyBits, neg);
    }
}
