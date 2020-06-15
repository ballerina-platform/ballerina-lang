/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * This is a special type introduced to be used in the return type of an extern function. In extern function return
 * types, typedesc typed parameters of the function can be referred. This type is basically a wrapper created around
 * that parameter. This contains the symbol of the parameter referred, and if there is one, the default value of the
 * parameter.
 *
 * @since 2.0.0
 */
public class BParameterizedType extends BType {

    public BVarSymbol paramSymbol;
    public BType paramValueType;

    public BParameterizedType(BType valueType, BVarSymbol paramSymbol, BTypeSymbol tSymbol, Name name) {
        super(TypeTags.PARAMETERIZED_TYPE, tSymbol);
        this.paramSymbol = paramSymbol;
        this.paramValueType = valueType;
        this.name = name;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public String toString() {
        return this.paramSymbol.name.toString();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
