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
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.PackageVarInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.debugger.dto.VariableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * {@code ExpressionEvaluator} Handles expression evaluation related actions.
 *
 * @since 0.981
 */
public class ExpressionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);
    private static final String STATUS = "status";
    private static final String RESULTS = "results";
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final String INTERNAL_ERROR = "{\"status\":\"failure\", \"results\":\"internal error\"}";

    /**
     * Method to evaluate a variable.
     *
     * @param ctx             Current worker execution context.
     * @param currentExecLine Current execution line.
     * @param variableName    Name of the variable to be evaluated.
     * @return Evaluated results.
     */
    public String evaluateVariable(WorkerExecutionContext ctx, LineNumberInfo currentExecLine, String variableName) {
        int currentExecLineNum = currentExecLine.getLineNumber();

        String defaultMessage = constructJsonResults(false, "cannot find local variable '" + variableName + "'");

        List<LocalVariableInfo> localVars = getLocalVariables(ctx);
        for (LocalVariableInfo var : localVars) {
            if (var.getVariableName().equals(variableName)) {
                if ((var.getScopeStartLineNumber() < currentExecLineNum) &&
                        (currentExecLineNum <= var.getScopeEndLineNumber())) {
                    VariableDTO variableDTO = Debugger.constructLocalVariable(ctx, var);
                    return constructJsonResults(true, variableDTO.getValue());
                }
                // Variable found, but not in the current scope
                return defaultMessage;
            }
        }

        PackageVarInfo[] globalVars = getPackageVariables(ctx);
        for (PackageVarInfo var : globalVars) {
            if (var.getName().equals(variableName)) {
                VariableDTO variableDTO = Debugger.constructGlobalVariable(ctx, var);
                return constructJsonResults(true, variableDTO.getValue());
            }
        }

        // Return default message if the specified variable not found in the current scope
        return defaultMessage;
    }

    private List<LocalVariableInfo> getLocalVariables(WorkerExecutionContext ctx) {
        LocalVariableAttributeInfo localVarAttrInfo = (LocalVariableAttributeInfo) ctx.workerInfo.
                getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
        return localVarAttrInfo.getLocalVariables();
    }

    private PackageVarInfo[] getPackageVariables(WorkerExecutionContext ctx) {
        return ctx.programFile.getPackageInfo(ctx.programFile.getEntryPkgName()).
                getPackageInfoEntries();
    }

    private String constructJsonResults(boolean isSuccess, String value) {
        StringWriter writer = new StringWriter();
        JsonGenerator gen = new JsonGenerator(writer);

        try {
            gen.startObject();

            gen.writeFieldName(STATUS);
            gen.writeString(isSuccess ? SUCCESS : FAILURE);

            gen.writeFieldName(RESULTS);
            gen.writeString(value);

            gen.endObject();
            gen.flush();
        } catch (IOException e) {
            log.error("error while constructing json results after evaluating given expression, " + e.getMessage(), e);
            return INTERNAL_ERROR;
        }
        return writer.toString();
    }
}
