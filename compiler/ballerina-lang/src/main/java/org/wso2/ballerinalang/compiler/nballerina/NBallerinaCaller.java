package org.wso2.ballerinalang.compiler.nballerina;

import org.ballerinalang.compiler.CompilerOptionName;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.PrintStream;

import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getBooleanValueIfSet;

/**
 * Call nBallerina backend.
 */
public class NBallerinaCaller {
    private static final PrintStream console = System.out;
    public static void callnBallerina(BLangPackage pkgNode, CompilerContext context) {
        CompilerOptions compilerOptions = CompilerOptions.getInstance(context);
        boolean nBal = getBooleanValueIfSet(compilerOptions, CompilerOptionName.NBAL);
        if (nBal) {
            console.println("nbal active");
        }
    }
}
