/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.util;

import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.testerina.core.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods.
 */
public class TesterinaUtils {

    private static PrintStream errStream = System.err;
    private static TesterinaRegistry registry = TesterinaRegistry.getInstance();

    public static void startService(ProgramFile programFile) {
        if (!programFile.isServiceEPAvailable()) {
            throw new BallerinaException(String.format("no services found in module: %s", programFile
                    .getEntryPkgName()));
        }
        PackageInfo servicesPackage = programFile.getEntryPackage();

        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);

        // Invoke package init function
        if (isPackageInitialized(programFile.getEntryPkgName())) {
            BLangFunctions.invokePackageInitFunctions(programFile);
            registry.addInitializedPackage(programFile.getEntryPkgName());
        }
        BLangFunctions.invokeVMUtilFunction(servicesPackage.getStartFunctionInfo());
    }

    public static boolean isPackageInitialized(String entryPkgName) {
        return !registry.getInitializedPackages().contains(entryPkgName);
    }

    /**
     * Cleans up any remaining testerina metadata.
     *
     * @param path The path of the Directory/File to be deleted
     */
    public static void cleanUpDir(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        } catch (IOException e) {
            errStream.println("Error occurred while deleting the dir : " + path.toString() + " with error : "
                                      + e.getMessage());
        }
    }

    /**
     * Initialize the debugger.
     *
     * @param programFile ballerina executable programFile
     * @param debugger    Debugger instance
     */
    public static void initDebugger(ProgramFile programFile, Debugger debugger) {
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }

    /**
     * Returns the full module name with org name for a given module.
     *
     * @param moduleName module name
     * @return full module name with organization name if org name exists
     */
    public static String getFullModuleName(String moduleName) {
        String orgName = registry.getOrgName();
        String version = registry.getVersion();
        // If the orgName is null there is no module, .bal execution
        if (orgName == null) {
            return ".";
        }
        if (orgName.isEmpty() || orgName.equals(Names.ANON_ORG.value)) {
            orgName = "";
        } else {
            orgName = orgName + Names.ORG_NAME_SEPARATOR;
        }

        if (version == null || version.isEmpty() || version.equals(Names.DEFAULT_VERSION.value)) {
            return orgName + moduleName + Names.VERSION_SEPARATOR + Names.DEFAULT_VERSION.value;
        }

        return orgName + moduleName + Names.VERSION_SEPARATOR + version;
    }

    /**
     * Set manifest configurations.
     *
     * @param sourceRoot source root path
     */
    public static void setManifestConfigs(Path sourceRoot) {
        Manifest manifest = TomlParserUtils.getManifest(sourceRoot);
        String orgName = manifest.getName();
        String version = manifest.getVersion();
        TesterinaRegistry.getInstance().setOrgName(orgName);
        TesterinaRegistry.getInstance().setVersion(version);
    }

    /**
     * Execute tests in build.
     *
     * @param sourceRootPath source root path
     * @param programFileMap map containing bLangPackage nodes along with their compiled program files
     */
    public static void executeTests(Path sourceRootPath, Map<BLangPackage, CompiledBinaryFile.ProgramFile>
            programFileMap) {
        // Load configuration file. The default config file is taken "ballerina.conf" in the source root path
        LauncherUtils.loadConfigurations(sourceRootPath, new HashMap<>(), null, false);

        // Set org-name and version to the TesterinaRegistry
        setManifestConfigs(sourceRootPath);

        BTestRunner testRunner = new BTestRunner();
        // Run the tests
        testRunner.runTest(programFileMap);

        if (testRunner.getTesterinaReport().isFailure()) {
            cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
            Runtime.getRuntime().exit(1);
        }
        cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
    }

    /**
     * Format error message.
     *
     * @param errorMsg error message
     * @return formatted error message
     */
    public static String formatError(String errorMsg) {
        StringBuilder newErrMsg = new StringBuilder();
        errorMsg = errorMsg.replaceAll("\n", "\n\t    ");
        List<String> msgParts = Arrays.asList(errorMsg.split("\n"));

        for (String msg : msgParts) {
            if (msgParts.indexOf(msg) != 0 && !msg.equals("\t    ")) {
                msg = "\t    \t" + msg.trim();
            }
            if (!msg.equals("\t    ")) {
                newErrMsg.append(msg).append("\n");
            }
        }
        return newErrMsg.toString();
    }
}
