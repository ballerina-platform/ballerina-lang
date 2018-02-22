/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile;

/**
 * {@code AttachedFunctionInfo} represents an attached function in the program file.
 * <p>
 * In Ballerina, you can attach functions to types, hence the name.
 *
 * @since 0.96
 */
public class AttachedFunctionInfo {
    public int nameCPIndex;
    public int signatureCPIndex;
    public int flags;

    public AttachedFunctionInfo(int nameCPIndex, int signatureCPIndex, int flags) {
        this.nameCPIndex = nameCPIndex;
        this.signatureCPIndex = signatureCPIndex;
        this.flags = flags;
    }

    // TODO We've got too many small classes in the current design. It is time to refactor!!!
}
