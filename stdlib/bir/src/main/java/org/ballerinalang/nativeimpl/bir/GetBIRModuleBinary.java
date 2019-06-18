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
package org.ballerinalang.nativeimpl.bir;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.IOException;

import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.nativeimpl.bir.BIRModuleUtils.COMPILER_PACKAGE_LOADER;

/**
 * Native class for the bir:getBIRModuleBinary function.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "bir",
        functionName = "getBIRModuleBinary",
        args = {
                @Argument(name = "birContext", type = OBJECT),
                @Argument(name = "modId", type = RECORD),
        },
        returnType = {
                @ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE)
        }

)
public class GetBIRModuleBinary extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> birContextRecord = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> modIdRecord = (BMap<String, BValue>) context.getRefArgument(1);

        // Get the PackageCache and Names objects from the birContext
        PackageLoader packageLoader = (PackageLoader) birContextRecord.getNativeData(COMPILER_PACKAGE_LOADER);
        Names names = (Names) birContextRecord.getNativeData(BIRModuleUtils.COMPILER_NAMES);

        // Create a packageID
        String org = modIdRecord.get("org").stringValue();
        String name = modIdRecord.get("name").stringValue();
        String version = modIdRecord.get("modVersion").stringValue();
        boolean isUnnamed = ((BBoolean) modIdRecord.get("isUnnamed")).booleanValue();
        String sourceFilename = modIdRecord.get("sourceFilename").stringValue();
        PackageID modId;
        if (isUnnamed) {
            modId = new PackageID(names.fromString(org), sourceFilename, names.fromString(version));
        } else {
            modId = new PackageID(names.fromString(org), names.fromString(name), names.fromString(version));
        }

        BPackageSymbol pkgSymbol = packageLoader.loadPackageSymbol(modId, null, null);
        if (pkgSymbol == null) {
            throw new BLangRuntimeException("unable to find package " + modId);
        }
        try {
            // Create binary array from the Serialized the BIR model.
            BValueArray binaryArray = new BValueArray(PackageFileWriter.writePackage(pkgSymbol.birPackageFile));

            // Set the return value
            context.setReturnValues(binaryArray);
        } catch (IOException e) {
            //TODO panic here - rajith
        }
    }
}
