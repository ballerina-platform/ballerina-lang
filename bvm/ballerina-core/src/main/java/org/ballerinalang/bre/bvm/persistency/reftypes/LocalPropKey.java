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

public class LocalPropKey {

    private String propKey;
    private String dataIdentifier;

    public LocalPropKey(String propKey, String dataIdentifier) {
        this.propKey = propKey;
        this.dataIdentifier = dataIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalPropKey that = (LocalPropKey) o;

        if (!propKey.equals(that.propKey)) return false;
        return dataIdentifier.equals(that.dataIdentifier);
    }

    @Override
    public int hashCode() {
        int result = propKey.hashCode();
        result = 31 * result + dataIdentifier.hashCode();
        return result;
    }
}
