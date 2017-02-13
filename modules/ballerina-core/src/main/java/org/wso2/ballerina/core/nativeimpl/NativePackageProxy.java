/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl;

import org.ballerinalang.util.repository.PackageRepository;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.CompilationUnit;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;

import java.util.function.Supplier;

/**
 * Proxy class Native packages. This proxy class loads a native package and hold it.
 * This class must override all the methods in the {@link BLangPackage} and must 
 * delegate to the {@link BLangPackage} instance thats is loaded by this proxy.
 *
 * @since 0.8.0
 */
public class NativePackageProxy extends BLangPackage {
    private Supplier<BLangPackage> nativePackageSupplier;
    private BLangPackage nativePackage;
    
    public NativePackageProxy(Supplier<BLangPackage> nativePackageSupplier, GlobalScope globalScope) {
        super(globalScope);
        this.nativePackageSupplier = nativePackageSupplier;
    }
    
    public BLangPackage load() {
        if (nativePackage == null) {
            nativePackage = this.nativePackageSupplier.get();
        }

        return nativePackage;
    }
    
    // Overridden methods form BLangPackage
    
    @Override
    public void setPackagePath(String pkgPath) {
        if (nativePackage != null) {
            nativePackage.setPackagePath(pkgPath);
        } else {
            handleProxyNotLoadedException();
        }
    }

    @Override
    public BallerinaFile[] getBallerinaFiles() {
        if (nativePackage != null) {
            return nativePackage.getBallerinaFiles();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public void setBallerinaFiles(BallerinaFile[] ballerinaFiles) {
        if (nativePackage != null) {
            nativePackage.setBallerinaFiles(ballerinaFiles);
        } else {
            handleProxyNotLoadedException();
        }
    }

    @Override
    public ImportPackage[] getImportPackages() {
        if (nativePackage != null) {
            return nativePackage.getImportPackages();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public void setImportPackages(ImportPackage[] importPackages) {
        if (nativePackage != null) {
            nativePackage.setImportPackages(importPackages);
        } else {
            handleProxyNotLoadedException();
        }
    }

    @Override
    public CompilationUnit[] getCompilationUntis() {
        if (nativePackage != null) {
            return nativePackage.getCompilationUntis();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public void addDependentPackage(BLangPackage bLangPackage) {
        if (nativePackage != null) {
            nativePackage.addDependentPackage(bLangPackage);
        } else {
            handleProxyNotLoadedException();
        }
    }

    @Override
    public BLangPackage[] getDependentPackages() {
        if (nativePackage != null) {
            return nativePackage.getDependentPackages();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public boolean isSymbolsDefined() {
        if (nativePackage != null) {
            return nativePackage.isSymbolsDefined();
        } else {
            handleProxyNotLoadedException();
        }
        return false;
    }

    @Override
    public void setSymbolsDefined(boolean symbolsDefined) {
        if (nativePackage != null) {
            nativePackage.setSymbolsDefined(symbolsDefined);
        } else {
            handleProxyNotLoadedException();
        }
    }

    @Override
    public PackageRepository getPackageRepository() {
        if (nativePackage != null) {
            return nativePackage.getPackageRepository();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public void setPackageRepository(PackageRepository pkgRepo) {
        if (nativePackage != null) {
            nativePackage.setPackageRepository(pkgRepo);
        } else {
            handleProxyNotLoadedException();
        }
    }

    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        if (nativePackage != null) {
            return ScopeName.PACKAGE;
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        if (nativePackage != null) {
            return nativePackage.getEnclosingScope();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        if (nativePackage != null) {
            nativePackage.define(name, symbol);
        } else {
            handleProxyNotLoadedException();
        }
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        if (nativePackage != null) {
            return nativePackage.resolve(name);
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    public BLangSymbol resolveMembers(SymbolName name) {
        if (nativePackage != null) {
            return nativePackage.resolveMembers(name);
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    // Methods in the BLangSymbol interface

    @Override
    public String getName() {
        if (nativePackage != null) {
            return nativePackage.getName();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public String getPackagePath() {
        if (nativePackage != null) {
            return nativePackage.getPackagePath();
        }
        return null;
    }

    @Override
    public boolean isPublic() {
        if (nativePackage != null) {
            return nativePackage.isPublic();
        } else {
            handleProxyNotLoadedException();
        }
        return true;
    }

    @Override
    public boolean isNative() {
        if (nativePackage != null) {
            return nativePackage.isNative();
        } else {
            handleProxyNotLoadedException();
        }
        return false;
    }

    @Override
    public SymbolName getSymbolName() {
        if (nativePackage != null) {
            return nativePackage.getSymbolName();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }

    @Override
    public SymbolScope getSymbolScope() {
        if (nativePackage != null) {
            return nativePackage.getSymbolScope();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }


    // Methods in the Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        if (nativePackage != null) {
            nativePackage.accept(visitor);;
        } else {
            handleProxyNotLoadedException();
        }
    }

    @Override
    public NodeLocation getNodeLocation() {
        if (nativePackage != null) {
            return nativePackage.getNodeLocation();
        } else {
            handleProxyNotLoadedException();
        }
        return null;
    }
    
    private void handleProxyNotLoadedException(){
        throw new BallerinaException("cannot perform operation. package is not loaded.");
    }
}
