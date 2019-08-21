package org.ballerinalang.util;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Creates jars in file system using bootstrap pack and create class loader hierarchy for them.
 */
public class BootstrapRunner {
    private static PrintStream outStream = System.out;
    private static PrintStream errorStream = System.err;

    public static void generateJarBinary(String entryBir, String jarOutputPath,
                                          boolean dumpBir, String... birCachePaths) {
        String bootstrapHome = System.getProperty("ballerina.bootstrap.home");
        if (bootstrapHome == null) {
            generateJarBinaryViaCompiledBackend(entryBir, jarOutputPath, dumpBir, birCachePaths);
            return;
        }

        List<String> commands = new ArrayList<>();
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");
        if (isWindows) {
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add(bootstrapHome + "\\bin\\ballerina.bat");
        } else {
            commands.add("sh");
            commands.add(bootstrapHome + "/bin/ballerina");
        }
        commands.add("run");
        commands.add(bootstrapHome + "/bin/compiler_backend_jvm.balx");
        commands.add(entryBir);
        if (isWindows) {
            commands.add("\"\""); // no native map for test file
        } else {
            commands.add(""); // no native map for test file
        }
        commands.add(jarOutputPath);
        commands.add(dumpBir ? "true" : "false"); // dump bir
        commands.addAll(Arrays.asList(birCachePaths));

        ProcessBuilder balProcess = new ProcessBuilder(commands);
        Map<String, String> env = balProcess.environment();
        env.remove("BAL_JAVA_DEBUG");
        env.remove("JAVA_OPTS");

        try {
            Process process = balProcess.start();
            Scanner errorScanner = new Scanner(process.getErrorStream());
            Scanner outputScanner = new Scanner(process.getInputStream());

            boolean processEnded = process.waitFor(120 * 5, TimeUnit.SECONDS);

            while (outputScanner.hasNext()) {
                outStream.println(outputScanner.nextLine());
            }

            while (errorScanner.hasNext()) {
                errorStream.println(errorScanner.nextLine());
            }
            if (!processEnded) {
                throw new BLangCompilerException("failed to generate jar file within 120s.");
            }
            if (process.exitValue() != 0) {
                throw new BLangCompilerException("jvm code gen phase failed.");
            }
        } catch (IOException e) {
            throw new BLangCompilerException("could not run compiler_backend_jvm.balx", e);
        } catch (InterruptedException e) {
            throw new BLangCompilerException("jvm code gen interrupted", e);
        }
    }

    public static void generateJarBinaryViaCompiledBackend(String entryBir, String jarOutputPath,
                                                           boolean dumpBir, String... birCachePaths) {
        List<String> commands = new ArrayList<>();
        commands.add(entryBir);
        commands.add(getMapPath());
        commands.add(jarOutputPath);
        commands.add(dumpBir ? "true" : "false"); // dump bir
        commands.addAll(Arrays.asList(birCachePaths));
        try {
            Class<?> backendMain = Class.forName("ballerina.compiler_backend_jvm.___init");
            Method backendMainMethod = backendMain.getMethod("main", String[].class);
            Object[] params = new Object[]{commands.toArray(new String[0])};
            backendMainMethod.invoke(null, params);
        } catch (InvocationTargetException e) {
            throw new BLangCompilerException(e.getTargetException().getMessage());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new BLangCompilerException("could not invoke compiler backend", e);
        }
    }

    public static void generateJarBinaryViaCompiledBackend(Path tmpDir, String entryBir, String jarOutputPath,
                                                           boolean dumpBir, String... birCachePaths) {
        List<String> commands = new ArrayList<>();
        commands.add(entryBir);
        commands.add(getMapPath());
        commands.add(jarOutputPath);
        commands.add(dumpBir ? "true" : "false"); // dump bir
        commands.addAll(Arrays.asList(birCachePaths));
        try {

             //Load all Jars from target/tmp
            if (Files.exists(tmpDir)) {
                File file = new File(tmpDir.toString());
                loadAllJarsInTarget(file);
            }

            Class<?> backendMain = Class.forName("ballerina.compiler_backend_jvm.___init");
            Method backendMainMethod = backendMain.getMethod("main", String[].class);
            Object[] params = new Object[]{commands.toArray(new String[0])};
            backendMainMethod.invoke(null, params);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            throw new RuntimeException(target.getMessage(), target);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | MalformedURLException e) {
            throw new RuntimeException("could not invoke compiler backend", e);
        }
    }

    private static void loadAllJarsInTarget(final File targetFolder) throws MalformedURLException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (final File file : targetFolder.listFiles()) {
            if (file.isDirectory()) {
                loadAllJarsInTarget(file);
            } else {
                URL url = file.toURI().toURL();
                URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, url);
            }
        }
    }

    private static String getMapPath() {
        String ballerinaNativeMap = System.getenv("BALLERINA_NATIVE_MAP");
        return ballerinaNativeMap == null ? "" : ballerinaNativeMap;
    }

    public static void writeNonEntryPkgs(List<BPackageSymbol> imports, Path birCache, Path importsBirCache,
                                         Path jarTargetDir, boolean dumpBir)
            throws IOException {
        for (BPackageSymbol pkg : imports) {
            PackageID id = pkg.pkgID;
            //Todo: ballerinax check shouldn't be here. This should be fixed by having a proper package hierarchy.
            //Todo: Remove ballerinax check after fixing it by the packerina team
            if (!"ballerina".equals(id.orgName.value) && !"ballerinax".equals(id.orgName.value)) {
                writeNonEntryPkgs(pkg.imports, birCache, importsBirCache, jarTargetDir, dumpBir);

                byte[] bytes = PackageFileWriter.writePackage(pkg.birPackageFile);
                Path pkgBirDir = importsBirCache.resolve(id.orgName.value)
                                                .resolve(id.name.value)
                                                .resolve(id.version.value.isEmpty() ? "0.0.0" : id.version.value);
                Files.createDirectories(pkgBirDir);
                Path pkgBir = pkgBirDir.resolve(id.name.value + ".bir");
                Files.write(pkgBir, bytes);

                String jarOutputPath = jarTargetDir.resolve(id.name.value + ".jar").toString();
                generateJarBinary(pkgBir.toString(), jarOutputPath, dumpBir, birCache.toString(),
                                  importsBirCache.toString());
            }
        }
    }

    public static JBallerinaInMemoryClassLoader createClassLoaders(BLangPackage bLangPackage,
                                                                   Path systemBirCache,
                                                                   Path buildRoot,
                                                                   Optional<Path> jarTargetRoot,
                                                                   boolean dumpBir) throws IOException {
        byte[] bytes = PackageFileWriter.writePackage(bLangPackage.symbol.birPackageFile);
        String fileName = calcFileNameForJar(bLangPackage);
        Files.createDirectories(buildRoot);
        Path intermediates = Files.createTempDirectory(buildRoot, fileName + "-");
        Path entryBir = intermediates.resolve(fileName + ".bir");
        Path jarTarget = jarTargetRoot.orElse(intermediates).resolve(fileName + ".jar");
        Files.write(entryBir, bytes);

        Path importsBirCache = intermediates.resolve("imports").resolve("bir-cache");
        Path importsTarget = importsBirCache.getParent().resolve("generated-bir-jar");
        Files.createDirectories(importsTarget);

        writeNonEntryPkgs(bLangPackage.symbol.imports, systemBirCache, importsBirCache, importsTarget, dumpBir);
        generateJarBinary(entryBir.toString(), jarTarget.toString(), dumpBir, systemBirCache.toString(),
                          importsBirCache.toString());

        if (!Files.exists(jarTarget)) {
            throw new RuntimeException("Compiled binary jar is not found: " + jarTarget);
        }

        return new JBallerinaInMemoryClassLoader(jarTarget, importsTarget.toFile());
    }

    private static String calcFileNameForJar(BLangPackage bLangPackage) {
        PackageID pkgID = bLangPackage.pos.src.pkgID;
        Name sourceFileName = pkgID.sourceFileName;
        if (sourceFileName != null) {
            return sourceFileName.value.replaceAll("\\.bal$", "");
        }
        return pkgID.name.value;
    }
}

