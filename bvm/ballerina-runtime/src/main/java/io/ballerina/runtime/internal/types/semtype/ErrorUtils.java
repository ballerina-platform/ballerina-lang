/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_ERROR;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_MAPPING;
import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.Builder.basicSubType;
import static io.ballerina.runtime.api.types.semtype.RecAtom.createDistinctRecAtom;

/**
 * Utility methods for creating error types.
 *
 * @since 2201.11.0
 */
public final class ErrorUtils {

    private ErrorUtils() {
    }

    public static SemType errorDetail(SemType detail) {
        SubTypeData data = Core.subTypeData(detail, BT_MAPPING);
        if (data == AllOrNothing.ALL) {
            return Builder.getErrorType();
        } else if (data == AllOrNothing.NOTHING) {
            return Builder.getNeverType();
        }

        assert data instanceof Bdd;
        SubType sd = ((Bdd) data).intersect(Builder.getBddSubtypeRo());
        if (sd.equals(Builder.getBddSubtypeRo())) {
            return Builder.getErrorType();
        }
        return basicSubType(BT_ERROR, BErrorSubType.createDelegate(sd));
    }

    public static SemType errorDistinct(int distinctId) {
        assert distinctId >= 0;
        Bdd bdd = bddAtom(createDistinctRecAtom(-distinctId - 1));
        return basicSubType(BT_ERROR, BErrorSubType.createDelegate(bdd));
    }
}
