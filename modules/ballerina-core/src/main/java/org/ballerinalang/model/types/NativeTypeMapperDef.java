/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.values.BValueType;

import java.util.function.Function;

/**
 * Type mapper represents a native conversion
 */
public class NativeTypeMapperDef implements BLangSymbol {

    private Function<BValueType, BValueType> typeMapper;
    private SymbolScope symbolScope;
    private SymbolName symbolName;

    public NativeTypeMapperDef(Function<BValueType, BValueType> typeMapper, SymbolScope symbolScope,
                               SymbolName symbolName) {
        this.typeMapper = typeMapper;
        this.symbolScope = symbolScope;
        this.symbolName = symbolName;
    }
    @Override
    public String getName() {
        return symbolName.getName();
    }

    @Override
    public String getPackagePath() {
        return null;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return symbolScope;
    }
}
