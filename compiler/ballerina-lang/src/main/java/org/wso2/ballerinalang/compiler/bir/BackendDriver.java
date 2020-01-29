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
package org.wso2.ballerinalang.compiler.bir;

import org.wso2.ballerinalang.compiler.bir.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.bir.desuger.BIRDesuger;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Similar to CompilerDriver, this class drives the backend code generation of BIR packages through various phases
 * such as desugar and code generation.
 *
 * @since 1.2.0
 */
public class BackendDriver {

    private static final CompilerContext.Key<BackendDriver> BACKEND_DRIVER = new CompilerContext.Key<>();

    private BIRDesuger birDesuger;
    private CodeGenerator codeGenerator;

    public static BackendDriver getInstance(CompilerContext context) {
        BackendDriver codeGenDriver = context.get(BACKEND_DRIVER);
        if (codeGenDriver == null) {
            codeGenDriver = new BackendDriver(context);
        }
        return codeGenDriver;
    }

    private BackendDriver(CompilerContext context) {
        context.put(BACKEND_DRIVER, this);

        this.birDesuger = BIRDesuger.getInstance(context);
        this.codeGenerator = CodeGenerator.getInstance(context);
    }

    public void execute(BIRNode.BIRPackage birPackage) {
        // Desuger phase on BIR
        desugar(birPackage);

        // Generate JVM bytecode from BIR
        codeGen(birPackage);
    }


    private BIRNode.BIRPackage desugar(BIRNode.BIRPackage birPackage) {
        return this.birDesuger.perform(birPackage);
    }

    private void codeGen(BIRNode.BIRPackage birPackage) {
        this.codeGenerator.generate(birPackage);
    }
}
