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


import java.util.Objects;

/**
 * {@code ImportPackageInfo} contains metadata of a dependent package.
 *
 * @since 0.963.0
 */
@Deprecated
public class ImportPackageInfo {

    public int orgNameCPIndex;
    public int nameCPIndex;
    public int versionCPIndex;

    public ImportPackageInfo(int orgNameCPIndex, int nameCPIndex, int versionCPIndex) {
        this.orgNameCPIndex = orgNameCPIndex;
        this.nameCPIndex = nameCPIndex;
        this.versionCPIndex = versionCPIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImportPackageInfo that = (ImportPackageInfo) o;
        return orgNameCPIndex == that.orgNameCPIndex && 
                nameCPIndex == that.nameCPIndex &&
                versionCPIndex == that.versionCPIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameCPIndex, versionCPIndex);
    }
}
