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
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.Core;
import io.ballerina.types.SemType;
import io.ballerina.types.UniformTypeCode;

/**
 * TableSubtype.
 *
 * @since 3.0.0
 */
public class TableSubtype {

    public static SemType tableContaining(SemType memberType) {
        Bdd ro = (Bdd) Core.subtypeData(memberType, UniformTypeCode.UT_MAPPING_RO);
        Bdd rw = (Bdd) Core.subtypeData(memberType, UniformTypeCode.UT_MAPPING_RW);
        SemType roSemtype = Core.createUniformSemType(UniformTypeCode.UT_TABLE_RO, ro);
        SemType rwSemtype = Core.createUniformSemType(UniformTypeCode.UT_TABLE_RW,
                RwTableSubtype.createRwTableSubtype(ro, rw));
        return Core.union(roSemtype, rwSemtype);
    }
}
