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

import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeCode;

/**
 * Represent a 3-tuple containing paired-up subtype data.
 *
 * @since 3.0.0
 */
public class SubtypePair {
    public final UniformTypeCode uniformTypeCode;
    public final SubtypeData subtypeData1;
    public final SubtypeData subtypeData2;

    private SubtypePair(UniformTypeCode uniformTypeCode, SubtypeData subtypeData1, SubtypeData subtypeData2) {
        this.uniformTypeCode = uniformTypeCode;
        this.subtypeData1 = subtypeData1;
        this.subtypeData2 = subtypeData2;
    }

    public static SubtypePair create(UniformTypeCode uniformTypeCode,
                                     SubtypeData subtypeData1, SubtypeData subtypeData2) {
        return new SubtypePair(uniformTypeCode, subtypeData1, subtypeData2);
    }
}
