/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors;

import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.NativeUnit;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.SymbolScope.ScopeName;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code AbstractNativeConnector} represents a Native Ballerina Connector.
 *
 * @since 0.8.0
 */
public abstract class AbstractNativeConnector extends BType implements NativeUnit, Connector, BLangSymbol {
    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic = true;
    protected SymbolName symbolName;

    private List<ParameterDef> parameterDefs;

    private SimpleTypeName[] returnParamTypeNames;
    private SimpleTypeName[] argTypeNames;
    
    // Scope related variables
    protected SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;
    
    public AbstractNativeConnector(SymbolScope enclosingScope) {
        super(enclosingScope);
        this.parameterDefs = new ArrayList<>();
        this.symbolMap = new HashMap<>();
    }

    public abstract boolean init(BValue[] bValueRefs);

    public ParameterDef[] getParameterDefs() {
        return parameterDefs.toArray(new ParameterDef[parameterDefs.size()]);
    }


    //TODO Fix Issue#320
    /**
     * Get an instance of the Connector.
     *
     * @return an instance
     */
    public abstract AbstractNativeConnector  getInstance();


    // Methods in BLangSymbol interface

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
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
        return null;
    }
    
    // Methods in NativeUnit interface
    
    @Override
    public void setReturnParamTypeNames(SimpleTypeName[] returnParamTypes) {
        this.returnParamTypeNames = returnParamTypes;
    }
    
    @Override
    public void setArgTypeNames(SimpleTypeName[] argTypes) {
        this.argTypeNames = argTypes;
    }
    
    @Override
    public SimpleTypeName[] getArgumentTypeNames() {
        return argTypeNames;
    }
    
    @Override
    public SimpleTypeName[] getReturnParamTypeNames() {
        return returnParamTypeNames;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public void setPackagePath(String packagePath) {
        this.pkgPath = packagePath;
    }
    
    public void setStackFrameSize(int stackFrameSize) {
        // do nothing
    }

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }
    
    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        return ScopeName.CONNECTOR;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        return resolve(symbolMap, name);
    }

    // Methods in the BType interface
    @Override
    public <V extends BValue> V getDefaultValue() {
        return null;
    }
}
