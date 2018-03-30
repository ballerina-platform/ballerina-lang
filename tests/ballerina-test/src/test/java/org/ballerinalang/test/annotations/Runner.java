package org.ballerinalang.test.annotations;

import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

public class Runner {

    public static void main(String[] args) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, "/home/natasha/Documents/github_repos/ballerina/bvm/ballerina-core/src/main/ballerina/pull");
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());

        Compiler compiler = Compiler.getInstance(context);
        compiler.build();
    }
}