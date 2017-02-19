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
package org.ballerinalang.natives.connectors;

import org.ballerinalang.model.Connector;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<ParameterDef> parameterDefs;
    private String[] argNames;
    private SimpleTypeName[] argTypeNames;
    private SimpleTypeName[] returnParamTypeNames;
    private List<NativeUnitProxy> actions;
    
    // Scope related variables
    private Map<SymbolName, BLangSymbol> symbolMap;
    
    public AbstractNativeConnector(SymbolScope enclosingScope) {
        super(enclosingScope);
        this.parameterDefs = new ArrayList<>();
        this.symbolMap = new HashMap<>();
        this.actions = new ArrayList<NativeUnitProxy>();
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
        return typeName;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return false;
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

    public void setArgNames(String[] argNames) {
        this.argNames = argNames;
    }

    @Override
    public SimpleTypeName[] getArgumentTypeNames() {
        return argTypeNames;
    }

    public String[] getArgumentNames() {
        return argNames;
    }

    @Override
    public SimpleTypeName[] getReturnParamTypeNames() {
        return returnParamTypeNames;
    }
    
    @Override
    public void setName(String name) {
        this.typeName = name;
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
        return symbolScope;
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

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    /**
     * Resolve a symbol in the current scope only. SymbolName will not be resolved in the enclosing scopes.
     * 
     * @param name SymbolName to lookup.
     * @return Symbol in the current scope. Null if the symbol name is not available in the current scope
     */
    public BLangSymbol resolveMembers(SymbolName name) {
        return symbolMap.get(name);
    }
    
    /**
     * Add an action to this connector.
     * 
     * @param actionName Symbol name of the action
     * @param actionSymbol Symbol of the action
     */
    public void addAction(SymbolName actionName, BLangSymbol actionSymbol) {
        if (!(actionSymbol instanceof NativeUnitProxy)) {
            throw new BallerinaException("incompatible type for action '" + actionName.getName() + "'");
        }
        this.actions.add((NativeUnitProxy) actionSymbol);
        define(actionName, actionSymbol);
    }
    
    /**
     * Get all actions associated with the connector
     * 
     * @return Actions associated with the connector
     */
    public NativeUnitProxy[] getActions() {
        return actions.toArray(new NativeUnitProxy[actions.size()]);
    }
}
