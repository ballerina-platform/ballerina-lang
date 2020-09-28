/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.cmd;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.toml.model.Module;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.packerina.cmd.Constants.ADD_COMMAND;

/**
 * New command for adding a new module.
 */
@CommandLine.Command(name = ADD_COMMAND, description = "Add a new module to Ballerina project")
public class AddCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;
    private static FileSystem jarFs;
    private static Map<String, String> env;
    private Path homeCache;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"})
    private String template = "main";

    @CommandLine.Option(names = {"--list", "-l"})
    private boolean list = false;

    public AddCommand() {
        userDir = Paths.get(System.getProperty("user.dir"));
        errStream = System.err;
        homeCache = RepoUtils.createAndGetHomeReposPath();
        initJarFs();
    }

    public AddCommand(Path userDir, PrintStream errStream) {
        this(userDir, errStream, RepoUtils.createAndGetHomeReposPath());
    }

    public AddCommand(Path userDir, PrintStream errStream, Path homeCache) {
        this.userDir = userDir;
        this.errStream = errStream;
        initJarFs();
        this.homeCache = homeCache;
    }

    private void initJarFs() {
        URI uri = null;
        try {
            uri = AddCommand.class.getClassLoader().getResource("create_cmd_templates").toURI();
            if (uri.toString().contains("!")) {
                final String[] array = uri.toString().split("!");
                if (null == jarFs) {
                    env = new HashMap<>();
                    jarFs = FileSystems.newFileSystem(URI.create(array[0]), env);
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(ADD_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (list) {
            errStream.println("Available templates:");
            for (String template : getTemplates()) {
                errStream.println("    - " + template);
            }
            // Get templates from balos
            for (String template : getBaloTemplates()) {
                errStream.println("    - " + template);
            }
            return;
        }

        // Check if inside a project repo
        Path projectPath = ProjectDirs.findProjectRoot(userDir);
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

        // Check if more then one argument is provided
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina add <project-name>",
                    true);
            return;
        }

        // Check if the provided arg a valid module name
        String moduleName = argList.get(0);
        boolean matches = RepoUtils.validatePkg(moduleName);
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
        if (ProjectDirs.isModuleExist(projectPath, moduleName)) {
            CommandUtil.printError(errStream,
                    "A module already exists with the given name : '" + moduleName + "' :\n" +
                            "Existing module path "
                            + projectPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(moduleName),
                    null,
                    false);
            return;
        }

        // Check if the template exists
        if (!getTemplates().contains(template) && findBaloTemplate(template) == null) {
            CommandUtil.printError(errStream,
                    "Template not found, use `ballerina add --list` to view available templates.",
                    null,
                    false);
            return;
        }

        try {
            createModule(projectPath, moduleName, template);
        } catch (ModuleCreateException e) {
            CommandUtil.printError(errStream,
                    "Error occurred while creating module : " + e.getMessage(),
                    null,
                    false);
            return;
        }

        errStream.println("Added new ballerina module at '" + userDir.relativize(projectPath
                .resolve(ProjectDirConstants.SOURCE_DIR_NAME)
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
            throws ModuleCreateException {
        Path modulePath = projectPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(moduleName);
        try {
            Files.createDirectories(modulePath);

            // We will be creating following in the module directory
            // - src/
            // -- mymodule/
            // --- Module.md      <- module level documentation
            // --- main.bal       <- Contains default main method.
            // --- resources/     <- resources for the module (available at runtime)
            // --- tests/         <- tests for this module (e.g. unit tests)
            // ---- main_test.bal  <- test file for main
            // ---- resources/    <- resources for these tests
            if (getTemplates().contains(template)) {
                applyTemplate(modulePath, template);
            } else {
                applyBaloTemplate(modulePath, template);
            }

        } catch (AccessDeniedException e) {
            throw new ModuleCreateException("Insufficient Permission");
        } catch (IOException | TemplateException e) {
            throw new ModuleCreateException(e.getMessage());
        }
    }

    private void applyBaloTemplate(Path modulePath, String template) {
        // find all balos matching org and module name.
        Path baloTemplate = findBaloTemplate(template);
        if (baloTemplate != null) {
            String moduleName = getModuleName(baloTemplate);

            URI zipURI = URI.create("jar:" + baloTemplate.toUri().toString());
            try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
                // Copy sources
                Path srcDir = zipfs.getPath("/src").resolve(moduleName);
                // We do a string comparison to be efficient.
                Files.walkFileTree(srcDir, new Copy(srcDir, modulePath));

                // Copy resources
                Path resourcesDir = zipfs.getPath("/" + ProjectDirConstants.RESOURCE_DIR_NAME);
                Path moduleResources = modulePath.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
                Files.createDirectories(moduleResources);
                // We do a string comparison to be efficient.
                Files.walkFileTree(resourcesDir, new Copy(resourcesDir, moduleResources));
                // Copy Module.md
                Path moduleMd = zipfs.getPath("/docs").resolve(ProjectDirConstants.MODULE_MD_FILE_NAME);
                Path toModuleMd = modulePath.resolve(ProjectDirConstants.MODULE_MD_FILE_NAME);
                Files.copy(moduleMd, toModuleMd, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                CommandUtil.printError(errStream,
                        "Error while applying template : " + e.getMessage(),
                        null,
                        false);
                Runtime.getRuntime().exit(1);
            }
        }
    }

    private String getModuleName(Path baloTemplate) {
        Path baloName = baloTemplate.getFileName();
        if (baloName != null) {
            String fileName = baloName.toString();
            return fileName.split("-")[0];
        }
        return "";
    }

    private Path findBaloTemplate(String template) {
        // Split the template in to parts
        String[] orgSplit = template.split("/");
        String orgName = orgSplit[0].trim();
        String moduleName = "";
        String version = "*";
        String modulePart = (orgSplit.length > 1) ? orgSplit[1] : "";
        String[] moduleSplit = modulePart.split(":");
        moduleName = moduleSplit[0].trim();
        version = (moduleSplit.length > 1) ? moduleSplit[1].trim() : version;

        String baloGlob = "glob:**/" + orgName + "/" + moduleName + "/" + version + "/*.balo";
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(baloGlob);
        Path baloCache = this.homeCache.resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
        // Iterate directories
        try (Stream<Path> walk = Files.walk(baloCache)) {

            List<Path> baloList = walk
                    .filter(pathMatcher::matches)
                    .collect(Collectors.toList());

            Collections.sort(baloList);
            // get the latest
            if (baloList.size() > 0) {
                return baloList.get(baloList.size() - 1);
            } else {
                return null;
            }
        } catch (IOException e) {
            CommandUtil.printError(errStream,
                    "Unable to read home cache",
                    null,
                    false);
            Runtime.getRuntime().exit(1);
        }


        return homeCache.resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
    }

    private List<String> getTemplates() {
        try {
            Path templateDir = getTemplatePath();
            Stream<Path> walk = Files.walk(templateDir, 1);

            List<String> templates = walk.filter(Files::isDirectory)
                    .filter(directory -> !templateDir.equals(directory))
                    .filter(directory -> directory.getFileName() != null)
                    .map(directory -> directory.getFileName())
                    .map(fileName -> fileName.toString())
                    .collect(Collectors.toList());

            if (null != AddCommand.jarFs) {
                return templates.stream().map(t -> t
                        .replace(AddCommand.jarFs.getSeparator(), ""))
                        .collect(Collectors.toList());
            } else {
                return templates;
            }

        } catch (IOException | TemplateException e) {
            // we will return an empty list if error.
            return new ArrayList<String>();
        }
    }

    /**
     * Iterate home cache and search for template balos.
     *
     * @return list of templates
     */
    private List<String> getBaloTemplates() {
        List<String> templates = new ArrayList<>();
        // get the path to home cache
        Path baloCache = this.homeCache.resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.balo");
        // Iterate directories
        try (Stream<Path> walk = Files.walk(baloCache)) {

            List<Path> baloList = walk
                    .filter(pathMatcher::matches)
                    .filter(this::isTemplateBalo)
                    .collect(Collectors.toList());

            // Convert the balo list to string list.
            templates = baloList.stream()
                    .map(this::getModuleToml)
                    .filter(o -> o != null)
                    .map(m -> {
                        return m.getModule_organization() + "/" + m.getModule_name();
                    })
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            CommandUtil.printError(errStream,
                    "Unable to read home cache",
                    null,
                    false);
            Runtime.getRuntime().exit(1);
        }
        // filter template modules
        return templates;
    }

    private Module getModuleToml(Path baloPath) {
        URI zipURI = URI.create("jar:" + baloPath.toUri().toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path metaDataToml = zipfs.getPath("metadata", "MODULE.toml");
            // We do a string comparison to be efficient.
            String content = new String(Files.readAllBytes(metaDataToml), Charset.forName("UTF-8"));
            Toml toml = new Toml().read(content);
            return toml.to(Module.class);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isTemplateBalo(Path baloPath) {
        URI zipURI = URI.create("jar:" + baloPath.toUri().toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path metaDataToml = zipfs.getPath("metadata", "MODULE.toml");
            // We do a string comparison to be efficient.
            return new String(Files.readAllBytes(metaDataToml), Charset.forName("UTF-8"))
                    .contains("template = \"true\"");
        } catch (IOException e) {
            // we simply ignore the balo file
        }
        return false;
    }

    private Path getTemplatePath() throws TemplateException {
        try {
            URI uri = getClass().getClassLoader().getResource("create_cmd_templates").toURI();
            if (uri.toString().contains("!")) {
                final String[] array = uri.toString().split("!");
                return jarFs.getPath(array[1]);
            } else {
                return Paths.get(uri);
            }
        } catch (URISyntaxException e) {
            throw new TemplateException(e.getMessage());
        }
    }

    private void applyTemplate(Path modulePath, String template) throws TemplateException {
        Path templateDir = getTemplatePath().resolve(template);

        try {
            Files.walkFileTree(templateDir, new Copy(templateDir, modulePath));
        } catch (IOException e) {
            throw new TemplateException(e.getMessage());
        }
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;


        public Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        public Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {

            Files.copy(file, toPath.resolve(fromPath.relativize(file).toString()), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }

    static class TemplateException extends Exception {
        public TemplateException(String message) {
            super(message);
        }
    }

    static class ModuleCreateException extends Exception {
        public ModuleCreateException(String message) {
            super(message);
        }
    }
}
