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
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.RwTableSubtype;
import io.ballerina.types.subtypedata.XmlSubtype;

/**
 * Readwrite table specific methods.
 *
 * @since 3.0.0
 */
public class TableRwOps implements UniformTypeOps {

    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        RwTableSubtype rwt1 = (RwTableSubtype) t1;
        RwTableSubtype rwt2 = (RwTableSubtype) t2;
        Bdd ro = BddCommonOps.bddUnion(rwt1.ro, rwt2.ro);
        Bdd rw = BddCommonOps.bddUnion(rwt1.rw, rwt2.rw);
        return RwTableSubtype.createRwTableSubtype(ro, rw);
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        RwTableSubtype rwt1 = (RwTableSubtype) t1;
        RwTableSubtype rwt2 = (RwTableSubtype) t2;
        Bdd ro = BddCommonOps.bddIntersect(rwt1.ro, rwt2.ro);
        Bdd rw = BddCommonOps.bddIntersect(rwt1.rw, rwt2.rw);
        return RwTableSubtype.createRwTableSubtype(ro, rw);
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        RwTableSubtype rwt1 = (RwTableSubtype) t1;
        RwTableSubtype rwt2 = (RwTableSubtype) t2;
        Bdd ro = BddCommonOps.bddDiff(rwt1.ro, rwt2.ro);
        Bdd rw = BddCommonOps.bddDiff(rwt1.rw, rwt2.rw);
        return RwTableSubtype.createRwTableSubtype(ro, rw);
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        RwTableSubtype rwt = (RwTableSubtype) t;
        Bdd ro = BddCommonOps.bddComplement(rwt.ro);
        Bdd rw = BddCommonOps.bddComplement(rwt.rw);
        return RwTableSubtype.createRwTableSubtype(ro, rw);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        RwTableSubtype rwt = (RwTableSubtype) t;
        boolean ro = MappingRoOps.mappingSubtypeIsEmpty(cx, rwt.ro);
        boolean rw = MappingCommonOps.mappingSubtypeIsEmpty(cx, rwt.rw);
        return ro && rw;
    }
}
