/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.List;

/**
 * Old style extern function wrapper class.
 *
 * @since 1.2.0
 */
public class OldStyleExternalFunctionWrapper extends BIRFunctionWrapper implements ExternalFunctionWrapper {

    String jClassName;
    List<BType> jMethodPramTypes;
    String jMethodVMSig;

    public OldStyleExternalFunctionWrapper(String orgName, String moduleName, String version, BIRNode.BIRFunction func,
                                           String fullQualifiedClassName, String jvmMethodDescription,
                                           String jClassName, List<BType> jMethodPramTypes, String jMethodVMSig) {

        super(orgName, moduleName, version, func, fullQualifiedClassName, jvmMethodDescription);

        this.jClassName = jClassName;
        this.jMethodPramTypes = jMethodPramTypes;
        this.jMethodVMSig = jMethodVMSig;
    }
}
