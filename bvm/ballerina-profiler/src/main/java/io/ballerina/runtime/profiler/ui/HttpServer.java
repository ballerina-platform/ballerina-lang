/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.profiler.ui;

import io.ballerina.runtime.profiler.util.Constants;
import io.ballerina.runtime.profiler.util.ProfilerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import static io.ballerina.runtime.profiler.util.Constants.BALLERINA_HOME;
import static io.ballerina.runtime.profiler.util.Constants.HTML_PROFILER_REPORT;
import static io.ballerina.runtime.profiler.util.Constants.OUT_STREAM;
import static io.ballerina.runtime.profiler.util.Constants.PERFORMANCE_JSON;
import static io.ballerina.runtime.profiler.util.Constants.WORKING_DIRECTORY;

/**
 * This class contains the HTTP server of the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class HttpServer {

    public void initializeHTMLExport() throws IOException {
        String profilerOutputDir = System.getProperty(WORKING_DIRECTORY);
        OUT_STREAM.printf("      Output: " + Constants.ANSI_YELLOW + "%s" + File.separator + HTML_PROFILER_REPORT +
                Constants.ANSI_RESET + "%n", profilerOutputDir);
        Path resourcePath = Paths.get(System.getenv(BALLERINA_HOME)).resolve("resources")
                .resolve("profiler");

        try {
            copyFolder(resourcePath, Path.of(profilerOutputDir));
        } catch (IOException e) {
            throw new ProfilerException("Error occurred while copying the resources", e);
        }

        String content = FileUtils.readFileAsString(PERFORMANCE_JSON);
        FrontEnd frontEnd = new FrontEnd();
        String htmlData = frontEnd.getSiteData(content);
        try (FileWriter writer = new FileWriter(HTML_PROFILER_REPORT, StandardCharsets.UTF_8)) {
            writer.write(htmlData);
        } catch (IOException e) {
            OUT_STREAM.printf("%s%n", e);
        }
    }

    private static void copyFolder(Path source, Path target) throws IOException {
        EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(source, options, Integer.MAX_VALUE, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // exclude the template html file.
                if (file.toString().endsWith(".html")) {
                    return FileVisitResult.CONTINUE;
                }
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
