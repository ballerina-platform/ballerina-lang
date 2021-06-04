package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
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
import java.util.function.Function;

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

            File path = new File("/home/ushira/WSO2/Parse/Parse/build/mock_nballerina.jar");
            try {
                URLClassLoader cl = new URLClassLoader(new URL[]{path.toURI().toURL()});
                Class<?> c = cl.loadClass("$value$mocknBal");
                Method m = c.getDeclaredMethod("one", Strand.class);
                Object arg = new Object();
                Function<Object[], Object> func = objects -> {
                    try {
                        return m.invoke(null, objects[0]);
                    } catch (InvocationTargetException e) {
                        Throwable targetException = e.getTargetException();
                        if (targetException instanceof RuntimeException) {
                            throw (RuntimeException) targetException;
                        } else {
                            throw new RuntimeException(targetException);
                        }
                    } catch (IllegalAccessException e) {
                        throw new BallerinaException("Method has private access", e);
                    }
                };
                final Scheduler scheduler = new Scheduler(false);
                scheduler.schedule(new Object[1], func, null,
                        null, null, PredefinedTypes.TYPE_ANY, null, null);
                scheduler.start();
                console.println(c.getName());
            } catch (MalformedURLException | ClassNotFoundException  | NoSuchMethodException e) {
                console.println(e);
            }

            console.println("nbal active");
        }
    }
}
