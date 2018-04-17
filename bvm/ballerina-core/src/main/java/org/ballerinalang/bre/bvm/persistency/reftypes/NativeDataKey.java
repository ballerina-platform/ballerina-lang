/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre.bvm.persistency.reftypes;

public class NativeDataKey {

    private String pkgPath;
    private String structName;
    private String dataKey;
    private String dataIdentifier;

    public NativeDataKey(String pkgPath, String structName, String dataKey, String dataIdentifier) {
        this.pkgPath = pkgPath;
        this.structName = structName;
        this.dataKey = dataKey;
        this.dataIdentifier = dataIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NativeDataKey that = (NativeDataKey) o;

        if (!pkgPath.equals(that.pkgPath)) return false;
        if (!structName.equals(that.structName)) return false;
        if (!dataKey.equals(that.dataKey)) return false;
        return dataIdentifier.equals(that.dataIdentifier);
    }

    @Override
    public int hashCode() {
        int result = pkgPath.hashCode();
        result = 31 * result + structName.hashCode();
        result = 31 * result + dataKey.hashCode();
        result = 31 * result + dataIdentifier.hashCode();
        return result;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public String getStructName() {
        return structName;
    }

    public void setStructName(String structName) {
        this.structName = structName;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getDataIdentifier() {
        return dataIdentifier;
    }

    public void setDataIdentifier(String dataIdentifier) {
        this.dataIdentifier = dataIdentifier;
    }
}
