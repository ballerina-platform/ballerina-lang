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
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.symbols.BLangSymbol;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.8.0
 */
public class BLangProgram implements SymbolScope, Node {
    private static final SymbolName mainFuncSymboleName = new SymbolName("main.string[]");

    private Path programFilePath;
    private BLangPackage mainPackage;

    // Scope related variables
    private GlobalScope globalScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    private int sizeOfStaticMem;

    public BLangProgram(GlobalScope globalScope, Path programFilePath) {
        this.globalScope = globalScope;
        symbolMap = new HashMap<>();

        this.programFilePath = programFilePath;
    }

    public BLangProgram(GlobalScope globalScope) {
        this.globalScope = globalScope;
        symbolMap = new HashMap<>();
    }

    public Path getProgramFilePath() {
        return programFilePath;
    }

    public BLangPackage getMainPackage() {
        return mainPackage;
    }

    public void setMainPackage(BLangPackage mainPackage) {
        this.mainPackage = mainPackage;
    }

    public BallerinaFunction getMainFunction() {
        return (BallerinaFunction) mainPackage.resolveMembers(mainFuncSymboleName);
    }

    public BLangPackage[] getPackages() {
        return symbolMap.values().stream().map(symbol -> (BLangPackage) symbol).toArray(BLangPackage[]::new);
    }

    public int getSizeOfStaticMem() {
        return sizeOfStaticMem;
    }

    public void setSizeOfStaticMem(int sizeOfStaticMem) {
        this.sizeOfStaticMem = sizeOfStaticMem;
    }


    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        return ScopeName.PROGRAM;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return globalScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        BLangSymbol symbol = resolve(symbolMap, name);
        if (symbol != null) {
            return symbol;
        }

        if (name.getPkgPath() == null) {
            return null;
        }

        // resolve the package symbol first
        SymbolName pkgSymbolName = new SymbolName(name.getPkgPath());
        BLangSymbol pkgSymbol = symbolMap.get(pkgSymbolName);
        if (pkgSymbol == null) {
            return null;
        }

        return ((BLangPackage) pkgSymbol).resolveMembers(new SymbolName(name.getName()));
    }


    // Methods in the Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }
}
