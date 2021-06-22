package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlDetails;
import io.ballerina.runtime.internal.launch.LaunchUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.FutureValue;
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
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Call nBallerina backend.
 */
public class nBallerinaCaller {
    private  final PrintStream console = System.out;
    private static final CompilerContext.Key<nBallerinaCaller> NBALLERINA = new CompilerContext.Key<>();
    private String nBal;

    public static nBallerinaCaller getInstance(CompilerContext context) {
        nBallerinaCaller nbalCaller = context.get(NBALLERINA);
        if (nbalCaller == null) {
            nbalCaller = new nBallerinaCaller(context);
        }
        return nbalCaller;
    }

    private nBallerinaCaller(CompilerContext context) {
        context.put(NBALLERINA, this);
        CompilerOptions compilerOptions = CompilerOptions.getInstance(context);
        this.nBal = compilerOptions.get(CompilerOptionName.NBALLERINA);
    }

    public  void callnBallerina(BLangPackage pkgNode)  {
        if (nBal != null) {
            File path = new File(nBal);
            try {
                URLClassLoader cl = new URLClassLoader(new URL[]{path.toURI().toURL()});

                Class<?> c = cl.loadClass("nballerina");
                Method m = c.getMethod("compile", Strand.class);

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

                Class<?> config = cl.loadClass("$ConfigurationMapper");
                Method configMethod = config.getMethod("$configureInit", String[].class,
                        Path[].class, String.class, String.class);
                TomlDetails configurationDetails = LaunchUtils.getConfigurationDetails();
                configMethod.invoke(null, new String[]{}, configurationDetails.paths,
                        configurationDetails.secret, configurationDetails.configContent);
                Scheduler scheduler = new Scheduler(false);

                final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null,
                        PredefinedTypes.TYPE_ANY, null, null);
                scheduler.start();

                final Throwable t = out.panic;
                if (t != null) {
                    if (t instanceof io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException) {
                        throw new BLangRuntimeException(t.getMessage());
                    }
                    if (t instanceof ErrorValue) {
                        throw new BLangRuntimeException(
                                "error: " + ((ErrorValue) t).getPrintableStackTrace());
                    }
                    throw (RuntimeException) t;
                }
            } catch (MalformedURLException | ClassNotFoundException | NoSuchMethodException
                    | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException());
            }

            console.println("nbal active");
        }
    }
}

