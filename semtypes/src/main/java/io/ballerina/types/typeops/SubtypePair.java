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

import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.ProperSubtypeData;

/**
 * Represent a 3-tuple containing paired-up subtype data.
 *
 * @since 2201.8.0
 */
public class SubtypePair {
    public final BasicTypeCode basicTypeCode;
    public final ProperSubtypeData subtypeData1;
    public final ProperSubtypeData subtypeData2;

    private SubtypePair(BasicTypeCode basicTypeCode, ProperSubtypeData subtypeData1,
                        ProperSubtypeData subtypeData2) {
        this.basicTypeCode = basicTypeCode;
        this.subtypeData1 = subtypeData1;
        this.subtypeData2 = subtypeData2;
    }

    public static SubtypePair create(BasicTypeCode basicTypeCode,
                                     ProperSubtypeData subtypeData1, ProperSubtypeData subtypeData2) {
        return new SubtypePair(basicTypeCode, subtypeData1, subtypeData2);
    }
}
