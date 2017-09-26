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
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

/**
 * Native function ballerina.user:getLocale.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.user",
        functionName = "getLocale",
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "Locale",
                                  structPackage = "ballerina.utils")},
        isPublic = true
)

@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns the current user's locale.")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "struct",
        value = "current user's locale")})
public class GetLocale extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        return new BValue[]{createLocale(context)};
    }

    private BStruct createLocale(Context context) {
        String language = System.getProperty("user.language");
        if (language == null) {
            language = "";
        }
        String country = System.getProperty("user.country");
        if (country == null) {
            country = "";
        }
        PackageInfo utilsPackageInfo = context.getProgramFile().getPackageInfo("ballerina.utils");
        StructInfo localeStructInfo = utilsPackageInfo.getStructInfo("Locale");
        return BLangVMStructs.createBStruct(localeStructInfo, language, country);
    }
}
