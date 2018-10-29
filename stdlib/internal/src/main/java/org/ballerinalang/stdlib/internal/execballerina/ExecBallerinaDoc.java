/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.internal.execballerina;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Extern function ballerina/internal:execBallerinaDoc.
 *
 * @since 0.975.1
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "execBallerinaDoc",
        args = {@Argument(name = "moduleList", type = TypeKind.ARRAY),
                @Argument(name = "sourceRoot", type = TypeKind.STRING),
                @Argument(name = "outputPath", type = TypeKind.STRING),
                @Argument(name = "templatesPath", type = TypeKind.STRING),
                @Argument(name = "exclude", type = TypeKind.ARRAY),
                @Argument(name = "includeNatives", type = TypeKind.BOOLEAN),
                @Argument(name = "envVars", type = TypeKind.MAP),
                @Argument(name = "config", type = TypeKind.STRING),
                @Argument(name = "verbose", type = TypeKind.BOOLEAN)
        },
        returnType = {@ReturnType(type = TypeKind.STRING),
                      @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class ExecBallerinaDoc extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(ExecBallerinaDoc.class);
    
    @Override
    public void execute(Context context) {
        
        Properties property = System.getProperties();
        String path = (String) property.get("ballerina.home");
        
        String commandToPass = buildCommand(context);
        
        if (null != commandToPass) {
            // Building command
            String balCommand = Paths.get(path, "bin", "ballerina") + " doc " + commandToPass;
            
            Process process;
            BufferedReader reader = null;
            BufferedReader readerEr = null;
            try {
                // Running the command
                StringBuilder output = new StringBuilder();
                process = Runtime.getRuntime().exec(balCommand);
                process.waitFor();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets
                        .UTF_8));
                readerEr = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets
                        .UTF_8));
                
                String lineEr;
                while ((lineEr = readerEr.readLine()) != null) {
                    output.append(lineEr).append("\n");
                }
                
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                String adjusted = output.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
                context.setReturnValues(new BString(adjusted));
            } catch (InterruptedException e) {
                String msg = "Error occurred while waiting for ballerina command to finish: " + e.getMessage();
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
                throw new BallerinaException(e.getMessage());
            } catch (IOException e) {
                String msg = "Error occurred executing command or reading out of the command: " + e.getMessage();
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    String msg = "Error occurred closing standard output: " + e.getMessage();
                    log.error(msg);
                    context.setReturnValues(BLangVMErrors.createError(context, msg));
                }
                try {
                    if (readerEr != null) {
                        readerEr.close();
                    }
                } catch (IOException e) {
                    String msg = "Error occurred closing standard error output: " + e.getMessage();
                    log.error(msg);
                    context.setReturnValues(BLangVMErrors.createError(context, msg));
                }
            }
        }
    }
    
    /**
     * Build the command to pass to ballerina doc command.
     * @param context The function context.
     * @return The command.
     */
    private static String buildCommand(Context context) {
        StringBuilder commandToPass = new StringBuilder(" ");
        
        // Module list
        BStringArray bPackageList = (BStringArray) context.getRefArgument(0);
        
        // Source root path
        Path sourceRootPath;
        BValue bSourceRootPath = context.getNullableRefArgument(1);
        if (null != bSourceRootPath) {
            sourceRootPath = getSourceRootPath(bSourceRootPath.stringValue());
            commandToPass.append("--sourceroot ").append(bSourceRootPath.stringValue()).append(" ");
        } else {
            sourceRootPath = getSourceRootPath(null);
        }
        
        if (Files.exists(sourceRootPath, LinkOption.NOFOLLOW_LINKS)) {
            SourceDirectory srcDirectory = new FileSystemProjectDirectory(sourceRootPath);
            List<String> packageList = srcDirectory.getSourcePackageNames();
            
            // Validating package list
            String missingPackage = null;
            for (String packageName : bPackageList.getStringArray()) {
                if (!packageName.equals("") && !packageList.contains(packageName)) {
                    missingPackage = packageName;
                    break;
                }
            }
            
            if (null == missingPackage) {
                return getCommandAsString(context, commandToPass, bPackageList, packageList);
            } else {
                String msg = "module does not exists to generate api docs: " + missingPackage;
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
                return null;
            }
        } else {
            String msg = "non existing or invalid source root path found: " + sourceRootPath;
            log.error(msg);
            context.setReturnValues(BLangVMErrors.createError(context, msg));
            return null;
        }
    }
    
    /**
     * Get the command as a string parsing the variables.
     * @param context Ballerina function context.
     * @param commandToPass The command builder.
     * @param bPackageList List of packages requested to generate docs.
     * @param packageList List of packages in the project.
     * @return The command.
     */
    private static String getCommandAsString(Context context, StringBuilder commandToPass, BStringArray bPackageList,
                                             List<String> packageList) {
        // Output path
        BValue outputDir = context.getNullableRefArgument(2);
        if (null != outputDir) {
            Path outputPath = Paths.get(outputDir.stringValue());
            if (Files.exists(outputPath)) {
                commandToPass.append("-o ").append(outputDir).append(" ");
            } else {
                String msg = "non existing or invalid output path found: " + outputPath;
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
                return null;
            }
        }
        
        // Template path
        BValue templateDir = context.getNullableRefArgument(3);
        if (null != templateDir) {
            Path templatePath = Paths.get(templateDir.stringValue());
            if (Files.exists(templatePath)) {
                commandToPass.append("-t ").append(templateDir).append(" ");
            } else {
                String msg = "non existing or invalid templates path found: " + templatePath;
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
                return null;
            }
        }
        
        // Exclude packages
        BValue bExclude = context.getNullableRefArgument(4);
        if (bExclude instanceof BStringArray) {
            BStringArray bExcludePackages = (BStringArray) bExclude;
            String missingExcludePackage = null;
            for (String packageName : bExcludePackages.getStringArray()) {
                if (!packageList.contains(packageName)) {
                    missingExcludePackage = packageName;
                    break;
                }
            }
            if (null == missingExcludePackage) {
                commandToPass.append("--exclude ").append(bExclude.stringValue()).append(" ");
            } else {
                String msg = "invalid exclude module found: " + missingExcludePackage;
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
                return null;
            }
        }
        
        // Exclude packages
        boolean includeNatives = context.getBooleanArgument(0);
        if (includeNatives) {
            commandToPass.append("-n ").append(" ");
        }
        
        // Environment variables
        BValue bEnvVars = context.getNullableRefArgument(5);
        if (bEnvVars instanceof BMap) {
            BMap<String, BString> envVapMap = (BMap) bEnvVars;
            if (envVapMap.size() > 0) {
                commandToPass.append(" -e ");
            }
            for (Map.Entry<String, BString> envVarEntry : envVapMap.getMap().entrySet()) {
                commandToPass
                        .append(envVarEntry.getKey())
                        .append("=")
                        .append(envVarEntry.getValue().stringValue())
                        .append(" ");
            }
        }
        
        // Config path
        BValue configDir = context.getNullableRefArgument(6);
        if (null != configDir) {
            Path configPath = Paths.get(configDir.stringValue());
            if (Files.exists(configPath)) {
                commandToPass.append(" --config ").append(configDir).append(" ");
            } else {
                String msg = "non existing or invalid config path found: " + configPath;
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
                return null;
            }
        }
        
        // Verbose
        boolean verbose = context.getBooleanArgument(1);
        if (verbose) {
            commandToPass.append(" -v ").append(" ");
        }
        
        // Package List
        for (String packageName : bPackageList.getStringArray()) {
            commandToPass.append(packageName).append(" ");
        }
        
        return commandToPass.toString();
    }
    
    /**
     * Validate and get the source root path of package(s).
     * @param sourceRoot The source root.
     * @return The source root path.
     */
    private static Path getSourceRootPath(String sourceRoot) {
        // Get source root path.
        Path sourceRootPath;
        if (sourceRoot == null || sourceRoot.isEmpty()) {
            sourceRootPath = Paths.get(System.getProperty("user.dir"));
        } else {
            try {
                sourceRootPath = Paths.get(sourceRoot).toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new RuntimeException("error reading from directory: " + sourceRoot + " reason: " +
                                           e.getMessage(), e);
            }
            
            if (!Files.isDirectory(sourceRootPath, LinkOption.NOFOLLOW_LINKS)) {
                throw new RuntimeException("source root must be a directory");
            }
        }
        return sourceRootPath;
    }
}
