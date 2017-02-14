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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 0.8.0
 */
public class BLangProgram implements SymbolScope, Node {
    private static final SymbolName mainFuncSymboleName = new SymbolName("main.string[]");

    private Category programCategory;
    private BLangPackage mainPackage;
    private List<String> entryPoints = new ArrayList<>();
    private List<BLangPackage> servicePackageList = new ArrayList<>();

    // Scope related variables
    private GlobalScope globalScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    private int sizeOfStaticMem;

    public BLangProgram(GlobalScope globalScope, Category programCategory) {
        this.globalScope = globalScope;
        this.programCategory = programCategory;
        symbolMap = new HashMap<>();
    }

    public Category getProgramCategory() {
        return programCategory;
    }

    public BLangPackage getMainPackage() {
        return mainPackage;
    }

    public void setMainPackage(BLangPackage mainPackage) {
        this.mainPackage = mainPackage;
    }

    public BallerinaFunction getMainFunction() {
        BallerinaFunction mainFunction = (BallerinaFunction) mainPackage.resolveMembers(mainFuncSymboleName);
        if (mainFunction == null || mainFunction.getReturnParameters().length != 0) {
            throw new RuntimeException("cannot find main function");
        }

        return mainFunction;
    }

    public void addServicePackage(BLangPackage bLangPackage) {
        servicePackageList.add(bLangPackage);
    }

    public BLangPackage[] getServicePackages() {
        return servicePackageList.toArray(new BLangPackage[0]);
    }

    public String[] getEntryPoint() {
        return entryPoints.toArray(new String[0]);
    }

    public void addEntryPoint(String entryPoint) {
        this.entryPoints.add(entryPoint);
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
        return resolve(symbolMap, name);
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


    /**
     * Indicates whether this program is a main program or a service program.
     *
     * @since 0.8.0
     */
    public enum Category {
        SERVICE_PROGRAM("service", ".bsz"),
        MAIN_PROGRAM("main", "bpz");

        String name;
        String extension;

        Category(String name, String extension) {
            this.name = name;
            this.extension = extension;
        }

        public String getName() {
            return name;
        }

        public String getExtension() {
            return extension;
        }
    }
}
