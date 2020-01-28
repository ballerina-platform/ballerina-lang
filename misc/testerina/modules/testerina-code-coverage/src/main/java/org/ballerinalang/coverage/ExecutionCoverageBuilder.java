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
package org.ballerinalang.coverage;

import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.CACHES_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.JSON_CACHE_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.SOURCE_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TARGET_COVERAGE_DIRECTORY;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TEST_DIR_NAME;

/**
 * Java class to generate the coverage report.
 *
 * @since 1.2.0
 */
public class ExecutionCoverageBuilder {

    private Path sourceRootPath;
    private Path targetPath;
    private Path compiledSourceJarPath;
    private String orgName;
    private Path balHome;
    private String packageName;
    private String moduleJarName;
    private String javaCommand;
    private String [] programArgs;
    private transient PrintStream out = System.out;
    private transient PrintStream err = System.err;

    public ExecutionCoverageBuilder(Path sourceRootPath, Path targetDirPath, Path testJarPath, String [] programArgs,
                                    String orgName, String moduleJarName, String packageName) {
        this.sourceRootPath = sourceRootPath;
        this.targetPath = targetDirPath;
        this.orgName = orgName;
        this.packageName = packageName;
        this.compiledSourceJarPath = testJarPath;
        this.moduleJarName = moduleJarName;
        this.programArgs = programArgs.clone();
        this.balHome = Paths.get(System.getProperty(BALLERINA_HOME));
        this.javaCommand = System.getProperty("java.command");
    }

    public boolean generateExecFile() {
        if (!generateDirectories()) {
            throw LauncherUtils.createLauncherException("Couldn't create the directories -> Coverage, Extracted.");
        }
        Path balDependencyPaths = this.balHome.resolve(BALLERINA_HOME_BRE).resolve(BALLERINA_HOME_LIB);
        Path jsonCachePath = this.targetPath.resolve(CACHES_DIR_NAME).resolve(JSON_CACHE_DIR_NAME)
                .resolve(moduleJarName);
        String launcherClassName = CoverageConstants.TESTERINA_EXECUTOR_CLASS_NAME;
        String filePathSeparator = System.getProperty("file.separator");
        String classPathSeparator = System.getProperty("path.separator");
        String classPaths = balDependencyPaths.toString() + filePathSeparator + "*" + classPathSeparator
                + this.compiledSourceJarPath.toString();
        // building the java command
        List<String> commands = new ArrayList<>();
        commands.add(javaCommand);
        commands.add("-javaagent:"
                + balDependencyPaths.resolve(CoverageConstants.AGENT_FILE_NAME).toString()
                + "=destfile="
                + this.targetPath
                .resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName)
                .resolve(CoverageConstants.EXEC_FILE_NAME).toString());
        commands.add("-cp");
        commands.add(classPaths);
        commands.add(launcherClassName);
        if (this.programArgs != null) {
            commands.addAll(Lists.of(this.programArgs));
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.environment().put("testerina.tesetsuite.path", jsonCachePath.toString());
            Process proc = processBuilder.start();
            proc.waitFor();
            // Then retreive the process output
            InputStream in = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            int outputStreamLength;

            byte[] b = new byte[in.available()];
            outputStreamLength = in.read(b, 0, b.length);
            if (outputStreamLength > 0) {
                this.out.println(new String(b, StandardCharsets.UTF_8));
            }

            byte[] c = new byte[err.available()];
            outputStreamLength = err.read(c, 0, c.length);
            if (outputStreamLength > 0) {
                this.err.println(new String(c, StandardCharsets.UTF_8));
            }

            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    // got the code from https://stackoverflow.com/questions/1529611/
    // how-to-write-a-java-program-which-can-extract-a-jar-file-and-store-its-data-in-s
    public void unzipCompiledSource() {
        boolean directoriesCreated = false;
        InputStream is = null;
        FileOutputStream fo = null;
        try (JarFile jarFile = new JarFile(new File(this.compiledSourceJarPath.toString()))) {
            Enumeration<JarEntry> enu = jarFile.entries();
            while (enu.hasMoreElements()) {
                String destDir = this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName)
                        .resolve(CoverageConstants.EXTRACTED_DIRECTORY_NAME).toString();
                JarEntry je = enu.nextElement();
                File fl = new File(destDir, je.getName());
                if (!fl.exists()) {
                    directoriesCreated = fl.getParentFile().mkdirs();
                    fl = new File(destDir, je.getName());
                }
                if (je.isDirectory()) {
                    continue;
                }
                is = jarFile.getInputStream(je);
                fo = new FileOutputStream(fl);
                while (is.available() > 0) {
                    fo.write(is.read());
                }
            }
            if (directoriesCreated) {
                this.out.print("");
            }
            String path = this.targetPath
                    .resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName)
                    .resolve(CoverageConstants.EXTRACTED_DIRECTORY_NAME).toString();
            removeUnnecessaryFilesFromUnzip(path);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception ignored) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {

                }
            }
            if (fo != null) {
                try {
                    fo.close();
                } catch (Exception ignored) {

                }
            }
        }
    }

    public void createSourceFileDirectory() {
        // to copy the source files to coverage directory
        String src = this.sourceRootPath.resolve(SOURCE_DIR_NAME).toString();
        String dest = this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName)
                .resolve(this.orgName).toString();
        File srcDir = new File(src);
        File destDir = new File(dest);
        if (!srcDir.exists()) {
            throw new RuntimeException("Directory doesn't exists");
        } else {
            try {
                copyFolder(srcDir, destDir);
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                this.err.println("couldn't create the source file directory : " + e);
            }
        }
        // delete already copied test source
        String deletingDir = this.targetPath
                .resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName).resolve(this.orgName)
                .resolve(this.packageName).resolve(TEST_DIR_NAME).toString();
        File deletingFile = new File(deletingDir);
        boolean deletedCopiedTestDir = deleteDirectory(deletingFile);
        if (deletedCopiedTestDir) {
            // to copy the test source from coverage directory
            String testSrc = this.sourceRootPath.resolve(SOURCE_DIR_NAME).resolve(this.packageName)
                    .resolve(TEST_DIR_NAME).toString();
            Path testDir = this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName)
                    .resolve(this.orgName).resolve(this.packageName).resolve(TEST_DIR_NAME).resolve(TEST_DIR_NAME);
            File testSrcDir = new File(testSrc);
            boolean createTestDir = generateDirectoryForGivenPath(testDir);
            File testDestDir = new File(testDir.toString());
            if (!testSrcDir.exists() && !createTestDir) {
                throw new RuntimeException("Directory doesn't exists");
            } else {
                try {
                    copyFolder(testSrcDir, testDestDir);
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    this.err.println("couldn't create the source file directory : " + e);
                }
            }
        }
    }

    public void generateCoverageReport() {
        String execFilePath = this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName)
                .resolve(CoverageConstants.EXEC_FILE_NAME).toString();
        String classFilesPath = this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName)
                .resolve(CoverageConstants.EXTRACTED_DIRECTORY_NAME).resolve(this.orgName).toString();
        String commonDir = this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY).resolve(this.moduleJarName).toString();
        // building the java command
        List<String> commands = new ArrayList<>();
        commands.add(javaCommand);
        commands.add("-jar");
        commands.add(this.balHome.resolve(BALLERINA_HOME_BRE).resolve(BALLERINA_HOME_LIB)
                .resolve(CoverageConstants.CLI_FILE_NAME).toString());
        commands.add("report");
        commands.add(execFilePath);
        commands.add("--classfiles");
        commands.add(classFilesPath);
        commands.add("--sourcefiles");
        commands.add(commonDir);
        commands.add("--html");
        commands.add(commonDir);
        commands.add("--name");
        commands.add(this.orgName);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process proc = processBuilder.start();
            proc.waitFor();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(e.toString());
        }
    }

    private void removeUnnecessaryFilesFromUnzip(String path) {

        Path filePath = Paths.get(path);
        File folder = new File(filePath.resolve(this.orgName).resolve(this.packageName).toString());

        File[] listOfFiles = folder.listFiles();

        boolean filesRemoved = false;

        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    String fileName = listOfFile.toString();
                    File file = new File(fileName);
                    if (listOfFile.toString().contains("___init")) {
                        filesRemoved = file.delete();
                    } else if (listOfFile.toString().contains("Frame")
                            && listOfFile.toString().contains("module")) {
                        filesRemoved = file.delete();
                    } else if (listOfFile.toString().contains("Frame")
                            && listOfFile.toString().contains(this.orgName)) {
                        filesRemoved = file.delete();
                    }
                }
            }
        }

        if (filesRemoved) {
            this.out.print("");
        }
    }

    private void copyFolder(File src, File dest) {
        if (src.isDirectory()) {
            boolean directoryCreated = true;
            //if directory not exists, create it
            if (!dest.exists()) {
                directoryCreated = dest.mkdir();
            }
            if (directoryCreated) {
                //list all the directory contents
                String[] files = src.list();
                if (files != null) {
                    for (String file : files) {
                        //construct the src and dest file structure
                        File srcFile = new File(src, file);
                        File destFile = new File(dest, file);
                        //recursive copy
                        copyFolder(srcFile, destFile);
                    }
                }
            }

        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            } catch (Exception ignored) {
            }
        }
    }

    private boolean generateDirectories() {
        boolean coverageDirectoryCreated;
        boolean extractedDirectoryCreated;
        coverageDirectoryCreated = generateDirectoryForGivenPath(this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY)
                .resolve(this.moduleJarName));
        extractedDirectoryCreated = generateDirectoryForGivenPath(this.targetPath.resolve(TARGET_COVERAGE_DIRECTORY)
                .resolve(this.moduleJarName).resolve(CoverageConstants.EXTRACTED_DIRECTORY_NAME));
        return coverageDirectoryCreated && extractedDirectoryCreated;
    }

    // got the code from https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java/3634879
    private boolean generateDirectoryForGivenPath(Path path) {
        return new File(path.toString()).mkdirs();
    }

    // got the code from https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
    private boolean deleteDirectory(File file) {
        boolean deleted;
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDirectory(f);
                }
            }
        }
        deleted = file.delete();
        return deleted;
    }
}
