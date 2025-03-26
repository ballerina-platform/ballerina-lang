/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package io.ballerina.types;

import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BddNode;

import static io.ballerina.types.BasicTypeCode.BT_ERROR;
import static io.ballerina.types.Core.subtypeData;
import static io.ballerina.types.PredefinedType.BDD_SUBTYPE_RO;
import static io.ballerina.types.PredefinedType.ERROR;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.basicSubtype;
import static io.ballerina.types.RecAtom.createDistinctRecAtom;
import static io.ballerina.types.typeops.BddCommonOps.bddAtom;
import static io.ballerina.types.typeops.BddCommonOps.bddIntersect;

/**
 * Contain functions found in error.bal file.
 *
 * @since 2201.12.0
 */
public class Error {
    public static SemType errorDetail(SemType detail) {
        SubtypeData mappingSd = subtypeData(detail, BasicTypeCode.BT_MAPPING);
        if (mappingSd instanceof AllOrNothingSubtype allOrNothingSubtype) {
            if (allOrNothingSubtype.isAllSubtype()) {
                return ERROR;
            } else {
                // XXX This should be reported as an error
                return NEVER;
            }
        }

        SubtypeData sd = bddIntersect((Bdd) mappingSd, BDD_SUBTYPE_RO);
        if (sd.equals(BDD_SUBTYPE_RO)) {
            return ERROR;
        }
        return basicSubtype(BT_ERROR, (ProperSubtypeData) sd);
    }

    public static SemType errorDistinct(int distinctId) {
        assert distinctId >= 0;
        BddNode bdd = bddAtom(createDistinctRecAtom(-distinctId - 1));
        return basicSubtype(BT_ERROR, bdd);
    }
}
