/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.cli.cmd;

import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

import static io.ballerina.cli.cmd.Constants.ADD_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.guessPkgName;

/**
 * This class represents the "ballerina add" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = ADD_COMMAND, description = "Add a new module to Ballerina project")
public class AddCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;
//    private Path homeCache;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"})
    private String template = "lib";

    public AddCommand() {
        userDir = Paths.get(System.getProperty("user.dir"));
        errStream = System.err;
//        homeCache = RepoUtils.createAndGetHomeReposPath();
        CommandUtil.initJarFs();
    }

    public AddCommand(Path userDir, PrintStream errStream) {
        this(userDir, errStream, RepoUtils.createAndGetHomeReposPath());
    }

    public AddCommand(Path userDir, PrintStream errStream, Path homeCache) {
        this.userDir = userDir;
        this.errStream = errStream;
        CommandUtil.initJarFs();
//        this.homeCache = homeCache;
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(ADD_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        // TODO: 11/13/20 to be enabled once it gets finalised.
//        if (list) {
//            errStream.println("Available templates:");
//            for (String template : getTemplates()) {
//                errStream.println("    - " + template);
//            }
//            // Get templates from balos
//            for (String template : getBaloTemplates()) {
//                errStream.println("    - " + template);
//            }
//            return;
//        }

        // Check if inside a project repo
        Path projectPath = ProjectUtils.findProjectRoot(userDir);
        if (null == projectPath) {
            CommandUtil.printError(errStream,
                    "not a ballerina project (or any parent up to mount point)\n" +
                            "You should run this command inside a ballerina project", null, false);
            return;
        }

        // Check if an argument is provided
        if (null == argList) {
            CommandUtil.printError(errStream,
                    "The following required arguments were not provided:\n" +
                            "    <module-name>",
                    "ballerina add <module-name> [-t|--template <template-name>]",
                    true);
            return;
        }

        // Check if more than one argument is provided
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina add <project-name>",
                    true);
            return;
        }

        // Check if the provided arg a valid module name
        String moduleName = argList.get(0);
        boolean matches = ProjectUtils.validateModuleName(moduleName);
        if (!matches) {
            CommandUtil.printError(errStream,
                    "Invalid module name : '" + moduleName + "' :\n" +
                            "Module name can only contain alphanumerics, underscores and periods " +
                            "and the maximum length is 256 characters",
                    null,
                    false);
            return;
        }

        // Check if the module already exists
        if (ProjectUtils.isModuleExist(projectPath, moduleName)) {
            CommandUtil.printError(errStream,
                    "A module already exists with the given name : '" + moduleName + "' :\n" +
                            "Existing module path "
                            + projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName),
                    null,
                    false);
            return;
        }

        // Check if the template exists
        if (!(template.equalsIgnoreCase("service") || template.equalsIgnoreCase("lib"))) {
            CommandUtil.printError(errStream,
                    "Using Ballerina Central module templates is not yet supported.",
                    null,
                    false);
            return;
        }

        try {
            Path moduleDirPath = projectPath.resolve(ProjectConstants.MODULES_ROOT);
            if (!Files.exists(moduleDirPath)) {
                Files.createDirectory(moduleDirPath);
            }
            createModule(projectPath, moduleName, template);
        } catch (AccessDeniedException e) {
            CommandUtil.printError(errStream,
                    "error: Error occurred while creating module : " + "Insufficient Permission",
                    null,
                    false);
            return;
        } catch (IOException | URISyntaxException e) {
            CommandUtil.printError(errStream,
                    "Error occurred while creating module : " + e.getMessage(),
                    null,
                    false);
            return;
        }

        errStream.println("Added new ballerina module at '" + userDir.relativize(projectPath
                .resolve(ProjectConstants.MODULES_ROOT)
                .resolve(moduleName)) + "'");
    }

    @Override
    public String getName() {
        return ADD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("add a new ballerina module");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina add <module-name> [-t|--template <template-name>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void createModule(Path projectPath, String moduleName, String template)
            throws IOException, URISyntaxException {
        Path modulePath = projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName);
        Files.createDirectories(modulePath);
        // We will be creating following in the module directory
        // - modules/
        // -- mymodule/
        // --- main.bal       <- Contains default main method.
        CommandUtil.applyTemplate(modulePath, template);
        Path source = modulePath.resolve(template.toLowerCase(Locale.getDefault()) + ".bal");
        Files.move(source, source.resolveSibling(guessPkgName(moduleName) + ".bal"),
                StandardCopyOption.REPLACE_EXISTING);
    }

//        private void applyBaloTemplate(Path modulePath, String template) {
//        // find all balos matching org and module name.
//        Path baloTemplate = findBaloTemplate(template);
//        if (baloTemplate != null) {
//            String moduleName = getModuleName(baloTemplate);
//
//            URI zipURI = URI.create("jar:" + baloTemplate.toUri().toString());
//            try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
//                // Copy sources
//                Path srcDir = zipfs.getPath("/modules").resolve(moduleName);
//                // We do a string comparison to be efficient.
//                Files.walkFileTree(srcDir, new FileUtils.Copy(srcDir, modulePath));
//
//                // Copy resources
//                Path resourcesDir = zipfs.getPath("/" + ProjectConstants.RESOURCE_DIR_NAME);
//                Path moduleResources = modulePath.resolve(ProjectConstants.RESOURCE_DIR_NAME);
//                Files.createDirectories(moduleResources);
//                // We do a string comparison to be efficient.
//                Files.walkFileTree(resourcesDir, new FileUtils.Copy(resourcesDir, moduleResources));
//                // Copy Module.md
//                Path moduleMd = zipfs.getPath("/docs").resolve(ProjectConstants.MODULE_MD_FILE_NAME);
//                Path toModuleMd = modulePath.resolve(ProjectConstants.MODULE_MD_FILE_NAME);
//                Files.copy(moduleMd, toModuleMd, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException e) {
//                CommandUtil.printError(errStream,
//                        "Error while applying template : " + e.getMessage(),
//                        null,
//                        false);
//                Runtime.getRuntime().exit(1);
//            }
//        }
//    }

//    private String getModuleName(Path baloTemplate) {
//        Path baloName = baloTemplate.getFileName();
//        if (baloName != null) {
//            String fileName = baloName.toString();
//            return fileName.split("-")[0];
//        }
//        return "";
//    }

//    private Path findBaloTemplate(String template) {
//        // Split the template in to parts
//        String[] orgSplit = template.split("/");
//        String orgName = orgSplit[0].trim();
//        String moduleName = "";
//        String version = "*";
//        String modulePart = (orgSplit.length > 1) ? orgSplit[1] : "";
//        String[] moduleSplit = modulePart.split(":");
//        moduleName = moduleSplit[0].trim();
//        version = (moduleSplit.length > 1) ? moduleSplit[1].trim() : version;
//
//        String baloGlob = "glob:**/" + orgName + "/" + moduleName + "/" + version + "/*.balo";
//        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(baloGlob);
//        Path baloCache = this.homeCache.resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
//        // Iterate directories
//        try (Stream<Path> walk = Files.walk(baloCache)) {
//
//            List<Path> baloList = walk
//                    .filter(pathMatcher::matches)
//                    .collect(Collectors.toList());
//
//            Collections.sort(baloList);
//            // get the latest
//            if (baloList.size() > 0) {
//                return baloList.get(baloList.size() - 1);
//            } else {
//                return null;
//            }
//        } catch (IOException e) {
//            CommandUtil.printError(errStream,
//                    "Unable to read home cache",
//                    null,
//                    false);
//            Runtime.getRuntime().exit(1);
//        }
//
//        return homeCache.resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
//    }
//
//    /**
//     * Iterate home cache and search for template balos.
//     *
//     * @return list of templates
//     */
//    private List<String> getBaloTemplates() {
//        List<String> templates = new ArrayList<>();
//        // get the path to home cache
//        Path baloCache = this.homeCache.resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
//        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.balo");
//        // Iterate directories
//        try (Stream<Path> walk = Files.walk(baloCache)) {
//
//            List<Path> baloList = walk
//                    .filter(pathMatcher::matches)
//                    .filter(this::isTemplateBalo)
//                    .collect(Collectors.toList());
//
//            // Convert the balo list to string list.
//            templates = baloList.stream()
//                    .map(this::getModuleToml)
//                    .filter(o -> o != null)
//                    .map(m -> {
//                        return m.getModule_organization() + "/" + m.getModule_name();
//                    })
//                    .distinct()
//                    .collect(Collectors.toList());
//        } catch (IOException e) {
//            CommandUtil.printError(errStream,
//                    "Unable to read home cache",
//                    null,
//                    false);
//            Runtime.getRuntime().exit(1);
//        }
//        // filter template modules
//        return templates;
//    }
//
//    private Module getModuleToml(Path baloPath) {
//        URI zipURI = URI.create("jar:" + baloPath.toUri().toString());
//        try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
//            Path metaDataToml = zipfs.getPath("metadata", "MODULE.toml");
//            // We do a string comparison to be efficient.
//            String content = new String(Files.readAllBytes(metaDataToml), StandardCharsets.UTF_8);
//            Toml toml = new Toml().read(content);
//            return toml.to(Module.class);
//        } catch (IOException e) {
//            return null;
//        }
//    }

//    private boolean isTemplateBalo(Path baloPath) {
//        URI zipURI = URI.create("jar:" + baloPath.toUri().toString());
//        try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
//            Path metaDataToml = zipfs.getPath("metadata", "MODULE.toml");
//            // We do a string comparison to be efficient.
//            return new String(Files.readAllBytes(metaDataToml), StandardCharsets.UTF_8)
//                    .contains("template = \"true\"");
//        } catch (IOException e) {
//            // we simply ignore the balo file
//        }
//        return false;
//    }
}
