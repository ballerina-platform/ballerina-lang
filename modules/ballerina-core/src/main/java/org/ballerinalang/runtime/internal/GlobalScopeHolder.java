/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.runtime.internal;

import org.ballerinalang.bre.SymScope;
import org.ballerinalang.model.Symbol;
import org.ballerinalang.model.SymbolName;

/**
 * {@code GlobalScopeHolder} is the place where Ballerina Global symbol information is maintain.
 * <p>
 *
 * @since 0.8.0
 */
public class GlobalScopeHolder {

    private static GlobalScopeHolder instance = new GlobalScopeHolder();
    private SymScope globalScope = new SymScope(SymScope.Name.GLOBAL);

    private GlobalScopeHolder() {
    }

    public static GlobalScopeHolder getInstance() {
        return instance;
    }

    public void insert(SymbolName symName, Symbol symbol) {
        globalScope.insert(symName, symbol);
    }

    public SymScope getScope() {
        return globalScope;
    }
}
