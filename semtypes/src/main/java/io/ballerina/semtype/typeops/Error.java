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
package io.ballerina.semtype.typeops;

import io.ballerina.semtype.Core;
import io.ballerina.semtype.PredefinedType;
import io.ballerina.semtype.ProperSubtypeData;
import io.ballerina.semtype.RecAtom;
import io.ballerina.semtype.SemType;
import io.ballerina.semtype.SubtypeData;
import io.ballerina.semtype.UniformTypeCode;
import io.ballerina.semtype.subtypedata.AllOrNothingSubtype;
import io.ballerina.semtype.subtypedata.BddNode;

public class Error {
    public static SemType errorDetail(SemType detail) {
        SubtypeData sd = Core.subtypeData(detail, UniformTypeCode.UT_MAPPING_RO);
        if (sd instanceof AllOrNothingSubtype) {
            if (((AllOrNothingSubtype) sd).isAllSubtype()) {
                return PredefinedType.ERROR;
            } else {
                // XXX This should be reported as an error
                return PredefinedType.NEVER;
            }
        } else {
            return PredefinedType.uniformSubtype(UniformTypeCode.UT_ERROR, (ProperSubtypeData) sd);
        }
    }

    // distinctId must be >= 0
    public SemType errorDistinct(int distinctId) {
        BddNode bdd = BddCommonOps.bddAtom(RecAtom.createRecAtom(-distinctId - 1));
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_ERROR, bdd);
    }

}
