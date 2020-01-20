/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.cmd;

import org.apache.commons.lang3.SystemUtils;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to implement "langserver start" command for ballerina. Ex: ballerina start-langserver [--debug debugPort]
 * [--classpath classpath] [--experimental] [--debug-log] [--trace-log] [--help|-h]
 *
 * @since 1.1.0
 */
@CommandLine.Command(name = "start-language-server", description = "start Ballerina language server")
public class LangServerStartCmd implements BLauncherCmd {
    private static final String CMD_NAME = "start-language-server";
    private static final String BALLERINA_HOME;

    static {
        BALLERINA_HOME = System.getProperty("ballerina.home");
    }

    @CommandLine.Option(names = "--classpath", description = "custom class path for language server")
    private String customClasspath;

    @CommandLine.Option(names = "--experimental", description = "start language server in experimental mode")
    private boolean experimental;

    @CommandLine.Option(names = "--debug-log", description = "start language server in debug log enabled")
    private boolean debugLog;

    @CommandLine.Option(names = "--trace-log", description = "start language server in trace log enabled")
    private boolean traceLog;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        try {
            // Add Language Server Libraries
            Path langServerLibs = Paths.get(BALLERINA_HOME, "lib", "tools", "lang-server", "lib");
            try (Stream<Path> walk = Files.walk(langServerLibs)) {
                List<Path> result = walk
                        .filter(jFile -> Files.isRegularFile(jFile) && jFile.getFileName().toString().endsWith(".jar"))
                        .collect(Collectors.toList());
                result.forEach(s -> addClassPath(s.toFile()));
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("Could not start language server");
            }

            // Add custom class paths
            addCustomClassPaths(customClasspath);
            // Set flags
            System.setProperty("experimental", Boolean.toString(experimental));
            System.setProperty("ballerina.debugLog", Boolean.toString(debugLog));
            System.setProperty("ballerina.traceLog", Boolean.toString(traceLog));
            System.setProperty("ballerina.home", BALLERINA_HOME);

            // Start Language Server
            LogManager.getLogManager().reset();
            Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
            globalLogger.setLevel(java.util.logging.Level.OFF);
            org.ballerinalang.langserver.launchers.stdio.Main.startServer(System.in, System.out);
        } catch (Throwable e) {
            throw LauncherUtils.createLauncherException("Could not start language server");
        }
    }

    private void addCustomClassPaths(String customClasspath) {
        if (customClasspath == null || customClasspath.isEmpty()) {
            return;
        }
        String separator = SystemUtils.IS_OS_WINDOWS ? ";" : ":";
        Arrays.stream(customClasspath.split(separator)).forEach(path -> addClassPath(new File(path)));
    }

    private static void addClassPath(File path) {
        if (path == null) {
            return;
        }
        try {
            // Here we use reflections, since `URLClassLoader.addURL` is a private method
            URI url = path.toURI();
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<URLClassLoader> urlClass = URLClassLoader.class;
            Method method = urlClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(urlClassLoader, url.toURL());
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }
}
