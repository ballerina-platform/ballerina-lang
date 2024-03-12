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

import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.Bdd;
import io.ballerina.types.Core;
import io.ballerina.types.SemType;

/**
 * TableSubtype.
 *
 * @since 2201.8.0
 */
public class TableSubtype {

    public static SemType tableContaining(SemType memberType) {
        Bdd bdd = (Bdd) Core.subtypeData(memberType, BasicTypeCode.BT_MAPPING);
        return Core.createBasicSemType(BasicTypeCode.BT_TABLE, bdd);
    }
}
