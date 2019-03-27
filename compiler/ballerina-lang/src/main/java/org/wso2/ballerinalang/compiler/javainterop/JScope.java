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

import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * This class represents a scope implementation for a Java package symbol.
 *
 * @since 0.991.0
 */
public class JScope extends Scope {

    public JScope(BSymbol owner) {
        super(owner);
    }

    // 1) Check whether it is already defined if yes, then return it.
    // 2) Attempt to load the class, if CNF, return NOT_FOUND_ENTRY
    // 3) Now, inspect the loaded class for public instance variables and public method signatures,
    //      and create a object symbol and a type based on this information.
    public ScopeEntry lookup(Name name) {

        String qualifiedClassName = this.owner.pkgID.name.value + "." + name.value;
        try {
            Class clazz = Class.forName(qualifiedClassName);
            System.out.println(clazz.getCanonicalName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // TODO use the JPkgClassLoader


        return NOT_FOUND_ENTRY;
    }
}
