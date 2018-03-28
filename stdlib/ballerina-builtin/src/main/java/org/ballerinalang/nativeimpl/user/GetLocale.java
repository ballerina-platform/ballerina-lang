/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.nativeimpl.user;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

/**
 * Native function ballerina.user:getLocale.
 *
 * @since 0.94.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "user",
        functionName = "getLocale",
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Locale",
                                  structPackage = "ballerina.utils")},
        isPublic = true
)
public class GetLocale extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        context.setReturnValues(createLocale(context));
    }

    private BStruct createLocale(Context context) {
        String language = System.getProperty("user.language");
        if (language == null) {
            language = BTypes.typeString.getZeroValue().stringValue();
        }
        String country = System.getProperty("user.country");
        if (country == null) {
            country = BTypes.typeString.getZeroValue().stringValue();
        }
        PackageInfo utilsPackageInfo = context.getProgramFile().getPackageInfo("ballerina.util");
        StructInfo localeStructInfo = utilsPackageInfo.getStructInfo("Locale");
        return BLangVMStructs.createBStruct(localeStructInfo, language, country);
    }
}
