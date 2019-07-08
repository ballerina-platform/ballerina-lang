package org.ballerinalang.util;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

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
            throw new BLangCompilerException("ballerina.bootstrap.home property is not set");
        }

        List<String> commands = new ArrayList<>();
        commands.add(entryBir);
        commands.add(""); // no native map for test file
        commands.add(jarOutputPath);
        commands.add(dumpBir ? "true" : "false"); // dump bir
        commands.addAll(Arrays.asList(birCachePaths));

        ProcessBuilder balProcess = new ProcessBuilder(commands);
        Map<String, String> environment = balProcess.environment();
        environment.remove("BAL_JAVA_DEBUG");
        environment.remove("JAVA_OPTS");

        try {
            Class<?> backendMain = Class.forName("ballerina.compiler_backend_jvm.___init");
            Method backendMainMethod = backendMain.getMethod("main", String[].class);
            Object[] params = new Object[]{commands.toArray(new String[0])};
            backendMainMethod.invoke(null, params);
        } catch (ClassNotFoundException | NoSuchMethodException |
                IllegalAccessException | InvocationTargetException e) {
            throw new BLangCompilerException("could not invoke compiler backend", e);
        }
    }

    private static void pipeIo(Scanner outputScanner, PrintStream outStream) {
        Thread ioInherit = new Thread(() -> {
            while (outputScanner.hasNext()) {
                outStream.println(outputScanner.nextLine());
            }
        }, "io-pipe");
        ioInherit.setDaemon(true);
        ioInherit.start();
    }

    public static void writeNonEntryPkgs(List<BPackageSymbol> imports, Path birCache, Path importsBirCache,
                                         Path jarTargetDir, boolean dumpBir)
            throws IOException {
        for (BPackageSymbol pkg : imports) {
            PackageID id = pkg.pkgID;
            if (!"ballerina".equals(id.orgName.value)) {
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
            throw new RuntimeException("Compiled binary jar is not found");
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
