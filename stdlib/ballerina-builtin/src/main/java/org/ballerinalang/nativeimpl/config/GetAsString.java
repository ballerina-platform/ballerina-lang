///*
// * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package org.ballerinalang.nativeimpl.config;
//
//import org.ballerinalang.bre.Context;
//import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
//import org.ballerinalang.config.ConfigRegistry;
//import org.ballerinalang.model.types.TypeKind;
//import org.ballerinalang.model.values.BString;
//import org.ballerinalang.natives.annotations.Argument;
//import org.ballerinalang.natives.annotations.BallerinaFunction;
//import org.ballerinalang.natives.annotations.ReturnType;
//
///**
// * Native function ballerina.config:getAsString.
// *
// * @since 0.966.0
// */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "config",
//        functionName = "getAsString",
//        args = {@Argument(name = "configkey", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.STRING)},
//        isPublic = true
//)
//public class GetAsString extends BlockingNativeCallableUnit {
//
//    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();
//
//    @Override
//    public void execute(Context context) {
//        String configKey = context.getStringArgument(0);
//        // TODO: Change the signature of getAsString() once default params are available
//        String globalValue = configRegistry.getConfigOrDefault(configKey, null);
//        context.setReturnValues(globalValue != null ? new BString(globalValue) : null);
//    }
//}
