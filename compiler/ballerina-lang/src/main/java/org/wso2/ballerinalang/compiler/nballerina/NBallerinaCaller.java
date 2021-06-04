package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.internal.scheduling.Strand;
import org.ballerinalang.compiler.CompilerOptionName;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

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

            File path = new File("add_file_path");
            try {
                URLClassLoader cl = new URLClassLoader(new URL[]{path.toURI().toURL()}, null);
                Class<?> c = cl.loadClass("$value$mocknBal");
                Method m = c.getMethod("one", Strand.class);
                Object arg = new Object();
                m.invoke(null, arg);
                console.println(c.getName());
            } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e) {
                console.println(e);
            }

            console.println("nbal active");
        }
    }
}
