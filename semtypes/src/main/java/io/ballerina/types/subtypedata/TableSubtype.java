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
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.Core;
import io.ballerina.types.SemType;
import io.ballerina.types.BasicTypeCode;

/**
 * TableSubtype.
 *
 * @since 2201.8.0
 */
public class TableSubtype {

    public static SemType tableContaining(SemType memberType) {
        Bdd ro = (Bdd) Core.subtypeData(memberType, BasicTypeCode.UT_MAPPING_RO);
        Bdd rw = (Bdd) Core.subtypeData(memberType, BasicTypeCode.UT_MAPPING_RW);
        SemType roSemtype = Core.createBasicSemType(BasicTypeCode.UT_TABLE_RO, ro);
        SemType rwSemtype = Core.createBasicSemType(BasicTypeCode.UT_TABLE_RW,
                RwTableSubtype.createRwTableSubtype(ro, rw));
        return Core.union(roSemtype, rwSemtype);
    }
}
