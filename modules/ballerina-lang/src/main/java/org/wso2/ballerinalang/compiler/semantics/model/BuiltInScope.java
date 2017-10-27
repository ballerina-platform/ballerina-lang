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

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * @since 0.94
 */
public class BuiltInScope extends Scope {

    public BuiltInScope(BSymbol owner) {
        super(owner);
    }

    public void define(Name name, BSymbol symbol) {
        if (symbol.pkgID != null && symbol.pkgID.name.value.startsWith(Names.BUILTIN_PACKAGE.value)) {
            symbol.pkgID.name = Names.BUILTIN_PACKAGE;
        }
        super.define(name, symbol);
    }

    public ScopeEntry lookup(Name name) {
        return super.lookup(name);
    }
}
