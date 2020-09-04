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

import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;
import java.util.Map;

/**
 * A map that keeps JVM stack the index values against the variables.
 *
 * @since 1.2.0
 */
public class BIRVarToJVMIndexMap {

    private int localVarIndex = 0;
    private final Map<String, Integer> jvmLocalVarIndexMap = new HashMap<>();

    private void add(BIRNode.BIRVariableDcl varDcl) {

        String varRefName = this.getVarRefName(varDcl);
        this.jvmLocalVarIndexMap.put(varRefName, this.localVarIndex);

        BType bType = varDcl.type;

        if (TypeTags.isIntegerTypeTag(bType.tag) || bType.tag == TypeTags.FLOAT) {
            this.localVarIndex = this.localVarIndex + 2;
        } else if (bType.tag == JTypeTags.JTYPE) {
            JType jType = (JType) bType;
            if (jType.jTag == JTypeTags.JLONG || jType.jTag == JTypeTags.JDOUBLE) {
                this.localVarIndex = this.localVarIndex + 2;
            } else {
                this.localVarIndex = this.localVarIndex + 1;
            }
        } else {
            this.localVarIndex = this.localVarIndex + 1;
        }
    }

    private String getVarRefName(BIRNode.BIRVariableDcl varDcl) {

        return varDcl.name.value;
    }

    public int addToMapIfNotFoundAndGetIndex(BIRNode.BIRVariableDcl varDcl) {

        String varRefName = this.getVarRefName(varDcl);
        if (!(this.jvmLocalVarIndexMap.containsKey(varRefName))) {
            this.add(varDcl);
        }

        Integer index = this.jvmLocalVarIndexMap.get(varRefName);
        return index != null ? index : -1;
    }
}
