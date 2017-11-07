/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.codegen;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * Represents a enumerator in the compiled Ballerina program.
 *
 * @since 0.95
 */
public class EnumeratorInfo {

    public int nameCPIndex;
    public int enumeratorIndex;
    public BType enumeratorType;

    public EnumeratorInfo(int nameCPIndex, int enumeratorIndex, BType enumeratorType) {
        this.nameCPIndex = nameCPIndex;
        this.enumeratorIndex = enumeratorIndex;
        this.enumeratorType = enumeratorType;
    }
}
