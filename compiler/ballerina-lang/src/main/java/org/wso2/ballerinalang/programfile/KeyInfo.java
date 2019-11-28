/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Represents a key in constant map literals.
 *
 * @since 0.990.4
 */
@Deprecated
public class KeyInfo {

    public String name;
    public int cpIndex = -1;

    public KeyInfo(String name) {
        this.name = name;
    }

    KeyInfo(String name, int nameCPIndex) {
        this.name = name;
        this.cpIndex = nameCPIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof KeyInfo)) {
            return false;
        }
        KeyInfo keyInfo = (KeyInfo) obj;
        return name.equals(keyInfo.name) && cpIndex == keyInfo.cpIndex;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + (cpIndex != -1 ? " : " + cpIndex : "");
    }
}
