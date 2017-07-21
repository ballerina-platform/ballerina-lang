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

import org.ballerinalang.natives.connectors.AbstractNativeAction;

import java.util.Objects;

/**
 * {@code ActionInfo} contains metadata of a Ballerina action entry in the program file.
 *
 * @since 0.87
 */
public class ActionInfo extends CallableUnitInfo {

    private AbstractNativeAction nativeAction;
    private ConnectorInfo connectorInfo;

    public ActionInfo(String pkgPath, int pkgCPIndex, String actionName, int actionNameCPIndex) {
        this.pkgPath = pkgPath;
        this.pkgCPIndex = pkgCPIndex;
        this.name = actionName;
        this.nameCPIndex = actionNameCPIndex;

        codeAttributeInfo = new CodeAttributeInfo();
        attributeInfoMap.put(AttributeInfo.CODE_ATTRIBUTE, codeAttributeInfo);
    }

    public AbstractNativeAction getNativeAction() {
        return nativeAction;
    }

    public void setNativeAction(AbstractNativeAction nativeAction) {
        this.nativeAction = nativeAction;
    }

    public ConnectorInfo getConnectorInfo() {
        return connectorInfo;
    }

    public void setConnectorInfo(ConnectorInfo connectorInfo) {
        this.connectorInfo = connectorInfo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ActionInfo && pkgCPIndex == (((ActionInfo) obj).pkgCPIndex)
                && nameCPIndex == (((ActionInfo) obj).nameCPIndex);
    }
}
