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
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;
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

    private CodeGenerator codeGenerator;
    private BIREmitter birEmitter;

    public static BackendDriver getInstance(CompilerContext context) {
        BackendDriver backendDriver = context.get(BACKEND_DRIVER);
        if (backendDriver == null) {
            backendDriver = new BackendDriver(context);
        }
        return backendDriver;
    }

    private BackendDriver(CompilerContext context) {
        context.put(BACKEND_DRIVER, this);

        this.codeGenerator = CodeGenerator.getInstance(context);
        this.birEmitter = BIREmitter.getInstance(context);
    }

    public void execute(BIRNode.BIRPackage birPackage, boolean dumpBIR) {
        // Emit BIR to console if dump-bir flag is set
        if (dumpBIR) {
            birEmitter.emit(birPackage);
        }

        // Generate JVM bytecode from BIR
        codeGen(birPackage);
    }

    private void codeGen(BIRNode.BIRPackage birPackage) {
        this.codeGenerator.generate(birPackage);
    }
}
