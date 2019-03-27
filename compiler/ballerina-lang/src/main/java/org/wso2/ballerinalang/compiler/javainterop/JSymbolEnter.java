/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.javainterop;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.JClassTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.JPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * This class defines a package symbol for a given Java package.
 *
 * @since 0.991.0
 */
public class JSymbolEnter {
    private final SymbolTable symTable;
    private final JPackageClassLoader jPackageCL;

    private static final CompilerContext.Key<JSymbolEnter> JAVA_PACKAGE_SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    public static JSymbolEnter getInstance(CompilerContext context) {
        JSymbolEnter symbolEnter = context.get(JAVA_PACKAGE_SYMBOL_ENTER_KEY);
        if (symbolEnter == null) {
            symbolEnter = new JSymbolEnter(context);
        }

        return symbolEnter;
    }

    private JSymbolEnter(CompilerContext context) {
        context.put(JAVA_PACKAGE_SYMBOL_ENTER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.jPackageCL = JPackageClassLoader.getInstance(context);
    }

    public JPackageSymbol definePackageSymbol(PackageID packageId) {
        if (!jPackageCL.isPackageExists(packageId.name.value)) {
            return null;
        }

        return Symbols.createJPackageSymbol(packageId, symTable);
    }

    public JClassTypeSymbol defineClassSymbol(Scope scope, Name name, Class clazz) {

        // 0) Check for Java primitive types etc.
        // 1) public fields
        // 2) constructor
        //      If there are more than one constructor available, get the default constructor.
        // 3) public methods


        // If the public interface of this class has references to other classes,
        // then we need to define their package symbols and class symbols recursively. *********
        return null;
    }
}



