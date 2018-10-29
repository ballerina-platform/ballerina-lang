/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
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
package org.ballerinalang.connector.api;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.BServiceType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ObjectTypeInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.PackageVarInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * Utils for accessing runtime information for Ballerina Connector SPI.
 *
 * @since 0.965.0
 */
public final class BLangConnectorSPIUtil {

    /**
     * Get ConnectorEndPoint struct.
     *
     * @param context current invocation context.
     * @return ConnectorEndPoint struct.
     */
    public static Struct getConnectorEndpointStruct(Context context) {
        BValue result = context.getRefArgument(0);
        if (result == null || result.getType().getTag() != TypeTags.OBJECT_TYPE_TAG) {
            throw new BallerinaException("Can't get connector endpoint struct");
        }
        return ConnectorSPIModelHelper.createStruct((BMap<String, BValue>) result);
    }

    /**
     * Returns Service registered.
     *
     * Note: Call this util method when service is required, in register server connector SPI function.
     *
     * @param context invocation Context
     * @return register service.
     */
    public static Service getServiceRegistered(Context context) {
        BValue result = context.getRefArgument(1);
        if (result == null || result.getType().getTag() != TypeTags.TYPEDESC_TAG
                || ((BTypeDescValue) result).value().getTag() != TypeTags.SERVICE_TAG) {
            throw new BallerinaConnectorException("Can't get service reference");
        }
        final BServiceType serviceType = (BServiceType) ((BTypeDescValue) result).value();
        final ProgramFile programFile = context.getProgramFile();
        final Service service = getService(programFile, serviceType);
        BLangFunctions.invokeServiceInitFunction(service.getServiceInfo().getInitFunctionInfo());
        return service;
    }

    /**
     * Creates a VM struct value.
     *
     * @param context    current context
     * @param pkgPath    package path of the struct
     * @param structName name of the struct
     * @param values     values to be copied to struct field in the defined order
     * @return created struct
     */
    public static BMap<String, BValue> createBStruct(Context context, String pkgPath, String structName,
                                                     Object... values) {
        return createBStruct(context.getProgramFile(), pkgPath, structName, values);
    }

    public static BMap<String, BValue> createBStruct(ProgramFile programFile, String pkgPath, String structName,
                                                     Object... values) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        if (packageInfo == null) {
            throw new BallerinaConnectorException("module - " + pkgPath + " does not exist");
        }
        StructureTypeInfo structureInfo = packageInfo.getStructInfo(structName);
        return BLangVMStructs.createBStruct(structureInfo, values);
    }

    /**
     * This is a helper method to create a object in native code.
     *
     * WARNING - please be cautious when using this method, if you have non blocking calls inside the
     * object constructor, then using this method may cause thread blocking scenarios.
     *
     * @param context to be used
     * @param pkgPath of the object
     * @param structName of the object
     * @param values to be passed into constructor
     * @return created object
     */
    public static BMap<String, BValue> createObject(Context context, String pkgPath, String structName,
                                                    BValue... values) {
        return createObject(context.getProgramFile(), pkgPath, structName, values);
    }

    public static BMap<String, BValue> createObject(ProgramFile programFile, String pkgPath, String objectName,
                                                    BValue... values) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        if (packageInfo == null) {
            throw new BallerinaConnectorException("module - " + pkgPath + " does not exist");
        }
        StructureTypeInfo typeInfo = packageInfo.getStructInfo(objectName);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.OBJECT_TYPE_TAG) {
            throw new BallerinaConnectorException("object - " + objectName + " does not exist");
        }
        return BLangVMStructs.createObject((ObjectTypeInfo) typeInfo, values);
    }

    /**
     * Wrap BVM struct value to {@link Struct}.
     *
     * @param bStruct value.
     * @return wrapped value.
     */
    public static Struct toStruct(BMap<String, BValue> bStruct) {
        return ConnectorSPIModelHelper.createStruct(bStruct);
    }


    public static Service getServiceFromType(ProgramFile programFile, Value value) {
        if (value == null || value.getType() != Value.Type.TYPEDESC) {
            throw new BallerinaConnectorException("Can't get service reference");
        }
        final BTypeDescValue vmValue = (BTypeDescValue) value.getVMValue();
        if (vmValue.value().getTag() != TypeTags.SERVICE_TAG) {
            throw new BallerinaConnectorException("Can't get service reference, not service type.");
        }
        return getService(programFile, (BServiceType) vmValue.value());
    }

    public static BMap<String, BValue> getPackageEndpoint(ProgramFile programFile, String pkgName, String version,
                                             String endpointName) {
        String pkgID = getPackageID(pkgName, version);
        final PackageInfo packageInfo = programFile.getPackageInfo(pkgID);
        if (packageInfo == null) {
            throw new BallerinaConnectorException("Incorrect module name");
        }
        final PackageVarInfo packageVarInfo = packageInfo.getPackageVarInfo(endpointName);
        if (packageVarInfo == null) {
            throw new BallerinaConnectorException("Can't locate " + endpointName + " endpoint variable");
        }
        return (BMap<String, BValue>) programFile.globalMemArea.getRefField(packageInfo.pkgIndex,
                packageVarInfo.getGlobalMemIndex());
    }

    public static Service getService(ProgramFile programFile, BServiceType serviceType) {
        final ServiceInfo serviceInfo = programFile.getPackageInfo(serviceType.getPackagePath())
                .getServiceInfo(serviceType.getName());
        return ConnectorSPIModelHelper.createService(programFile, serviceInfo);
    }

    private static String getPackageID(String pkgName, String version) {
        if (version == null || Names.EMPTY.value.equals(version)) {
            return pkgName;
        }
        return String.join(Names.VERSION_SEPARATOR.value, pkgName, version);
    }
}
