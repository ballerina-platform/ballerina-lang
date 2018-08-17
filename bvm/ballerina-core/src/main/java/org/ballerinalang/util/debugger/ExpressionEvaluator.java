/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.debugger;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.PackageVarInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.debugger.dto.VariableDTO;

import java.util.Arrays;
import java.util.List;

public class ExpressionEvaluator {

    public String evaluateVariable(WorkerExecutionContext ctx, LineNumberInfo currentExecLine, String variableName) {
        int currentExecLineNum = currentExecLine.getLineNumber();

        List<PackageVarInfo> globalVars = getPackageVariables(ctx);
        for (PackageVarInfo var : globalVars) {
            if (var.getName().equals(variableName)) {
                VariableDTO variableDTO = Debugger.constructGlobalVariable(ctx, var);
                return variableDTO.getValue();
            }
        }

        List<LocalVariableInfo> localVars = getLocalVariables(ctx);
        for (LocalVariableInfo var : localVars) {
            if (var.getVariableName().equals(variableName) && (var.getScopeStartLineNumber() < currentExecLineNum) &&
                    (currentExecLineNum <= var.getScopeEndLineNumber())) {
                VariableDTO variableDTO = Debugger.constructLocalVariable(ctx, var);
                return variableDTO.getValue();
            }
        }

        // Return default message if the specified variable not found in the current scope
        return "Cannot find local variable '" + variableName + "'";
    }

    private List<LocalVariableInfo> getLocalVariables(WorkerExecutionContext ctx) {
        LocalVariableAttributeInfo localVarAttrInfo = (LocalVariableAttributeInfo) ctx.workerInfo.
                getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
        return localVarAttrInfo.getLocalVariables();
    }

    private List<PackageVarInfo> getPackageVariables(WorkerExecutionContext ctx) {
        PackageVarInfo[] packageVars = ctx.programFile.getPackageInfo(ctx.programFile.getEntryPkgName()).
                getPackageInfoEntries();
        return Arrays.asList(packageVars);
    }
}
