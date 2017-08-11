/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.loading;

import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NativeScope;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.util.exceptions.NativeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for native construct loading.
 */
public class NativeConstructLoadingTest {

    GlobalScope globalScope;
    NativeScope nativeScope;

    @BeforeClass
    public void setup() {
        globalScope = GlobalScope.getInstance();
        nativeScope = NativeScope.getInstance();
        BuiltInNativeConstructLoader.loadConstructs(nativeScope);
        BTypes.loadBuiltInTypes(globalScope);
    }

    @Test
    public void testLoadingExistingConstruct() {
        BLangSymbol pkgSymbol = nativeScope.resolve(new SymbolName("ballerina.lang.system"));
        Assert.assertTrue(pkgSymbol instanceof NativePackageProxy);

        BLangPackage bLangPkg = ((NativePackageProxy) pkgSymbol).load();
        BLangSymbol nativeFunctionSymbol = bLangPkg.resolve(new SymbolName("println.string"));
        Assert.assertTrue(nativeFunctionSymbol instanceof NativeUnitProxy);

        NativeUnit nativeUnit = ((NativeUnitProxy) nativeFunctionSymbol).load();
        Assert.assertTrue(nativeUnit instanceof Function);
    }

    @Test
    public void testLoadingNonExistingConstruct() {
        BLangSymbol pkgSymbol = nativeScope.resolve(new SymbolName("ballerina.lang.system"));
        Assert.assertTrue(pkgSymbol instanceof NativePackageProxy);

        BLangPackage bLangPkg = ((NativePackageProxy) pkgSymbol).load();
        BLangSymbol nativeFunctionSymbol = bLangPkg.resolve(new SymbolName("non.existing.function"));
        Assert.assertNull(nativeFunctionSymbol);
    }
    
    
    @Test(expectedExceptions = {NativeException.class},
          expectedExceptionsMessageRegExp = "internal error occured in 'ballerina.lang.system:unimplementedFunction'")
    public void testLoadingUnimplemenmtedConstruct() {
        registerNonExistingNativeFunction();

        BLangSymbol pkgSymbol = nativeScope.resolve(new SymbolName("ballerina.lang.system"));
        Assert.assertTrue(pkgSymbol instanceof NativePackageProxy);

        BLangPackage bLangPkg = ((NativePackageProxy) pkgSymbol).load();
        BLangSymbol nativeFunctionSymbol = bLangPkg.resolve(new SymbolName("unimplementedFunction"));
        Assert.assertTrue(nativeFunctionSymbol instanceof NativeUnitProxy);

        ((NativeUnitProxy) nativeFunctionSymbol).load();
    }
    
    private void registerNonExistingNativeFunction() {
        nativeScope.define(new SymbolName("ballerina.lang.system"), new NativePackageProxy(() -> {
            BLangPackage nativePackage = new BLangPackage(nativeScope);
            nativePackage.setPackagePath("ballerina.lang.system");
            nativePackage.define(new SymbolName("unimplementedFunction"), new NativeUnitProxy(() -> {
                NativeUnit nativeCallableUnit = null;
                try {
                    Class<?> nativeUnitClass =
                            Class.forName("org.ballerinalang.nativeimpl.lang.system.NonExistingClass");
                    nativeCallableUnit = ((NativeUnit) nativeUnitClass.getConstructor().newInstance());
                    nativeCallableUnit.setName("unimplementedFunction");
                    nativeCallableUnit.setPackagePath("ballerina.lang.system");
                    nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[] {});
                    nativeCallableUnit.setStackFrameSize(1);
                    nativeCallableUnit.setSymbolName(new SymbolName("unimplementedFunction"));
                } catch (Throwable t) {
                    throw new NativeException("internal error occured in 'ballerina.lang.system:unimplementedFunction'",
                        t);
                }
                return nativeCallableUnit;
            }));
            return nativePackage;
        }, nativeScope));
    }
}
