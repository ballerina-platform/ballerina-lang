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
package org.ballerinalang.natives;

import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.GlobalScope;

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
}
