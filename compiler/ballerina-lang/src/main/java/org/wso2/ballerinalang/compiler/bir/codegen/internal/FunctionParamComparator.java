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
package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;

import java.util.Comparator;

/**
 * BIR Function Param comparator to sort according to RETURN, ARG varkinds and TEMP vars according to their names.
 *
 * @since 1.2.0
 */
public class FunctionParamComparator implements Comparator<BIRNode.BIRVariableDcl> {

    @Override
    public int compare(BIRNode.BIRVariableDcl o1, BIRNode.BIRVariableDcl o2) {
        if (o1.kind != o2.kind) {
            return Integer.compare(getWeight(o1), getWeight(o2));
        }
        if (o1.kind == VarKind.TEMP) {
            return Integer.compare(o1.name.value.hashCode(), o2.name.value.hashCode());
        }
        return 0;
    }

    private int getWeight(BIRNode.BIRVariableDcl variableDcl) {

        return switch (variableDcl.kind) {
            case RETURN -> 1;
            case ARG -> 2;
            case TEMP -> 3;
            default -> 4;
        };
    }
}
