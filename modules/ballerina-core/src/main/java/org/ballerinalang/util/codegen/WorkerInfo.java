/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.codegen;

import java.util.Objects;

/**
 * {@code WorkerInfo} contains metadata of a Ballerina worker entry in the program file.
 *
 * @since 0.90
 */
public class WorkerInfo extends CallableUnitInfo {

    public WorkerInfo(String pkgPath, int pkgCPIndex, String workerName, int workerNameCPIndex) {
        this.pkgPath = pkgPath;
        this.pkgCPIndex = pkgCPIndex;
        this.name = workerName;
        this.nameCPIndex = workerNameCPIndex;

        codeAttributeInfo = new CodeAttributeInfo();
        attributeInfoMap.put(AttributeInfo.CODE_ATTRIBUTE, codeAttributeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorkerInfo && pkgCPIndex == (((WorkerInfo) obj).pkgCPIndex)
                && nameCPIndex == (((WorkerInfo) obj).nameCPIndex);
    }
}
