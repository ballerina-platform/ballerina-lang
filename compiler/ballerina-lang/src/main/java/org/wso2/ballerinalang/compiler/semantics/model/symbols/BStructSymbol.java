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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BStructSymbol} represents a struct symbol in a scope.
 *
 * @since 0.964.0
 */
public class BStructSymbol extends BTypeSymbol {

    public List<BAttachedFunction> attachedFuncs;
    public BAttachedFunction initializerFunc;
    public BAttachedFunction defaultsValuesInitFunc;

    public BStructSymbol(int kind, int flags, Name name, PackageID pkgID, BType type, BSymbol owner) {
        super(kind, flags, name, pkgID, type, owner);
        this.attachedFuncs = new ArrayList<>(0);
    }

    /**
     * A wrapper class which hold an attached function of a struct.
     *
     * @since 0.95.7
     */
    public static class BAttachedFunction {
        public Name funcName;
        public BInvokableType type;
        public BInvokableSymbol symbol;

        public BAttachedFunction(Name funcName, BInvokableSymbol symbol,
                                 BInvokableType type) {
            this.funcName = funcName;
            this.type = type;
            this.symbol = symbol;
        }
    }
}
