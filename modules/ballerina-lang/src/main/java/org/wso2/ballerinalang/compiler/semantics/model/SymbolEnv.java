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
package org.wso2.ballerinalang.compiler.semantics.model;

import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

/**
 * @since 0.94
 */
public class SymbolEnv {

    public Scope scope;

    public BLangNode node;

    public BLangPackage enclPkg;

    public BLangConnector enclConnector;

    public BLangService enclService;

    public BLangInvokableNode enclInvokable;

    public SymbolEnv enclEnv;

    public SymbolEnv(BLangNode node, Scope scope) {
        this.scope = scope;
        this.node = node;
        enclPkg = null;
        enclConnector = null;
        enclService = null;
        enclInvokable = null;
        enclEnv = null;
    }
}
