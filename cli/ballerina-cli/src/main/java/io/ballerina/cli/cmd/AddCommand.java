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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
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
 * This class represents the "bal add" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = ADD_COMMAND, description = "Add a new module to Ballerina project")
public class AddCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;
    private boolean exitWhenFinish;
//    private Path homeCache;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"})
    private String template = "lib";

    public AddCommand() {
        this.userDir = Paths.get(System.getProperty("user.dir"));
        this.errStream = System.err;
        this.exitWhenFinish = true;
//        homeCache = RepoUtils.createAndGetHomeReposPath();
        CommandUtil.initJarFs();
    }

    public AddCommand(Path userDir, PrintStream errStream, boolean exitWhenFinish) {
        this(userDir, errStream, exitWhenFinish, RepoUtils.createAndGetHomeReposPath());
    }

    public AddCommand(Path userDir, PrintStream errStream, boolean exitWhenFinish, Path homeCache) {
        this.userDir = userDir;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
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
//            // Get templates from balas
//            for (String template : getBalaTemplates()) {
//                errStream.println("    - " + template);
//            }
//            return;
//        }

        // Check if inside a project repo
        Path projectPath = ProjectUtils.findProjectRoot(userDir);
        if (null == projectPath) {
            CommandUtil.printError(errStream,
                    "not a Ballerina project (or any parent up to mount point)\n" +
                            "You should run this command inside a Ballerina project.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if an argument is provided
        if (null == argList) {
            CommandUtil.printError(errStream,
                    "module name is not provided.",
                    "bal add <module-name> [-t|--template <template-name>]",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if more than one argument is provided
        if (1 != argList.size()) {
            CommandUtil.printError(errStream,
                    "too many arguments",
                    "bal add <module-name>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if the provided arg a valid module name
        String moduleName = argList.get(0);
        boolean matches = ProjectUtils.validateModuleName(moduleName);
        if (!matches) {
            CommandUtil.printError(errStream,
                    "invalid module name : '" + moduleName + "' :\n" +
                            "Module name can only contain alphanumerics, underscores and periods.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!ProjectUtils.validateNameLength(moduleName)) {
            CommandUtil.printError(errStream,
                                   "invalid module name : '" + moduleName + "' :\n" +
                                           "Maximum length of module name is 256 characters.",
                                   null,
                                   false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if the module already exists
        if (ProjectUtils.isModuleExist(projectPath, moduleName)) {
            CommandUtil.printError(errStream,
                    "a module already exists with the given name : '" + moduleName + "' :\n" +
                            "Existing module path "
                            + projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if the template exists
        if (!(template.equalsIgnoreCase("service") || template.equalsIgnoreCase("lib"))) {
            CommandUtil.printError(errStream,
                    "unsupported template provided. run 'bal add --help' to see available templates.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
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
                    "error occurred while creating module : " + "Insufficient Permission" + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        } catch (IOException | URISyntaxException e) {
            CommandUtil.printError(errStream,
                    "error occurred while creating module : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        errStream.println("Added new Ballerina module at " + userDir.relativize(projectPath
                .resolve(ProjectConstants.MODULES_ROOT)
                .resolve(moduleName)));
    }

    @Override
    public String getName() {
        return ADD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Add a new Ballerina module");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal add <module-name> [-t|--template <template-name>]\n");
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

//        private void applyBalaTemplate(Path modulePath, String template) {
//        // find all balas matching org and module name.
//        Path balaTemplate = findBalaTemplate(template);
//        if (balaTemplate != null) {
//            String moduleName = getModuleName(balaTemplate);
//
//            URI zipURI = URI.create("jar:" + balaTemplate.toUri().toString());
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

//    private String getModuleName(Path balaTemplate) {
//        Path balaName = balaTemplate.getFileName();
//        if (balaName != null) {
//            String fileName = balaName.toString();
//            return fileName.split("-")[0];
//        }
//        return "";
//    }

//    private Path findBalaTemplate(String template) {
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
//        String balaGlob = "glob:**/" + orgName + "/" + moduleName + "/" + version + "/*.bala";
//        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(balaGlob);
//        Path balaCache = this.homeCache.resolve(ProjectConstants.BALA_CACHE_DIR_NAME);
//        // Iterate directories
//        try (Stream<Path> walk = Files.walk(balaCache)) {
//
//            List<Path> balaList = walk
//                    .filter(pathMatcher::matches)
//                    .collect(Collectors.toList());
//
//            Collections.sort(balaList);
//            // get the latest
//            if (balaList.size() > 0) {
//                return balaList.get(balaList.size() - 1);
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
//        return homeCache.resolve(ProjectConstants.BALA_CACHE_DIR_NAME);
//    }
//
//    /**
//     * Iterate home cache and search for template balas.
//     *
//     * @return list of templates
//     */
//    private List<String> getBalaTemplates() {
//        List<String> templates = new ArrayList<>();
//        // get the path to home cache
//        Path balaCache = this.homeCache.resolve(ProjectConstants.BALA_CACHE_DIR_NAME);
//        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.bala");
//        // Iterate directories
//        try (Stream<Path> walk = Files.walk(balaCache)) {
//
//            List<Path> balaList = walk
//                    .filter(pathMatcher::matches)
//                    .filter(this::isTemplateBala)
//                    .collect(Collectors.toList());
//
//            // Convert the bala list to string list.
//            templates = balaList.stream()
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
//    private Module getModuleToml(Path balaPath) {
//        URI zipURI = URI.create("jar:" + balaPath.toUri().toString());
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

//    private boolean isTemplateBala(Path balaPath) {
//        URI zipURI = URI.create("jar:" + balaPath.toUri().toString());
//        try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
//            Path metaDataToml = zipfs.getPath("metadata", "MODULE.toml");
//            // We do a string comparison to be efficient.
//            return new String(Files.readAllBytes(metaDataToml), StandardCharsets.UTF_8)
//                    .contains("template = \"true\"");
//        } catch (IOException e) {
//            // we simply ignore the bala file
//        }
//        return false;
//    }
}
