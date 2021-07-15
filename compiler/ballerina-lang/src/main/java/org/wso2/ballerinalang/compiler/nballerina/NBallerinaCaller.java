package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlDetails;
import io.ballerina.runtime.internal.launch.LaunchUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.BmpStringValue;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.MapValue;
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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Function;


/**
 * Call nBallerina backend.
 */
public class NBallerinaCaller {
    private  final PrintStream console = System.out;
    private static final CompilerContext.Key<NBallerinaCaller> NBALLERINA = new CompilerContext.Key<>();
    private final ModuleGen modGenerator;
    private String nBal;


    public static NBallerinaCaller getInstance(CompilerContext context) {
        NBallerinaCaller nbalCaller = context.get(NBALLERINA);
        if (nbalCaller == null) {
            nbalCaller = new NBallerinaCaller(context);
        }
        return nbalCaller;
    }

    private NBallerinaCaller(CompilerContext context) {
        context.put(NBALLERINA, this);
        CompilerOptions compilerOptions = CompilerOptions.getInstance(context);
        this.modGenerator = ModuleGen.getInstance(context);
        this.nBal = compilerOptions.get(CompilerOptionName.NBALLERINA);
    }

    public  boolean callnBallerina(BLangPackage pkgNode)  {
        if (nBal == null) {
            return false;
        }

        File path = new File(nBal);
        try {
            URLClassLoader cl = (URLClassLoader) AccessController.doPrivileged((PrivilegedAction) () -> {
                URLClassLoader cl1;
                try {
                    cl1 = new URLClassLoader(new URL[]{path.toURI().toURL()},
                            ClassLoader.getSystemClassLoader());
                } catch (SecurityException e) {
                    throw new BallerinaException("Security error getting classloader", e);
                } catch (MalformedURLException e) {
                throw new BallerinaException("Error in provided nBallerina path", e);
            }
                return cl1;
            });


            Class<?> c = cl.loadClass("wso2.nballerina$0046nback.0_1_0.nback");
            //Method m = c.getMethod("compileModule", Strand.class, BObject.class, Boolean.TYPE,
            //       Object.class, Boolean.TYPE);
            Method m = c.getMethod("compileModuleJBal", Strand.class, MapValue.class, Boolean.TYPE,
                    Object.class, Boolean.TYPE);

            Class<?> config = cl.loadClass("wso2.nballerina.0_1_0.$ConfigurationMapper");
            Method configMethod = config.getMethod("$configureInit", String[].class,
                    Path[].class, String.class, String.class);

            Class<?> init = cl.loadClass("wso2.nballerina.0_1_0.$_init");
            Method initMethod = init.getMethod("$moduleInit", Strand.class);
            Method startMethod = init.getMethod("$moduleStart", Strand.class);

            TomlDetails configurationDetails = LaunchUtils.getConfigurationDetails();
            configMethod.invoke(null, new String[]{}, configurationDetails.paths,
                    configurationDetails.secret, configurationDetails.configContent);

            Function<Object[], Object> funcInit = objects -> {
                try {
                    return initMethod.invoke(null, objects[0]);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    throw new BallerinaException("Error invoking nBallerina backend", targetException);
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };
            Function<Object[], Object> funcStart = objects -> {
                try {
                    return startMethod.invoke(null, objects[0]);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    throw new BallerinaException("Error invoking nBallerina backend", targetException);
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };

            Object bir = modGenerator.genMod(pkgNode);

            Scheduler scheduler = new Scheduler(false);

            Function<Object[], Object> func = objects -> {
                try {
                    return m.invoke(null, objects[0], bir, true, new BmpStringValue("out.ll"), true);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    throw new BallerinaException("Error invoking nBallerina backend", targetException);
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };

            scheduler.schedule(new Object[1], funcInit, null, null, null,
                    PredefinedTypes.TYPE_ANY, null, null);
            scheduler.schedule(new Object[1], funcStart, null, null, null,
                    PredefinedTypes.TYPE_ANY, null, null);
            final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null,
                    PredefinedTypes.TYPE_ANY, null, null);
            scheduler.start();

            final Throwable t = out.panic;
            if (t != null) {
                if (t instanceof io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException) {
                    throw new BLangRuntimeException(t.getMessage());
                } else if (t instanceof ErrorValue) {
                    throw new BLangRuntimeException(
                            "error: " + ((ErrorValue) t).getPrintableStackTrace());
                } else {
                    throw new BallerinaException("Error calling scheduler", t);
                }
            }

        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException e) {
            throw new BallerinaException("Error invoking nBallerina backend", e);
        } catch (InvocationTargetException e) {
            throw new BallerinaException("Error invoking nBallerina config", e.getTargetException());
        }

        console.println("nbal active");


        return true;
    }
}
