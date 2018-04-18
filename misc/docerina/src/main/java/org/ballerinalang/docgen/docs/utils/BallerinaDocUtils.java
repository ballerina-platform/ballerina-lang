/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs.utils;

import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Util methods used for doc generation.
 */
public class BallerinaDocUtils {

    private static final boolean debugEnabled = "true".equals(System.getProperty(
            BallerinaDocConstants.ENABLE_DEBUG_LOGS));
    private static final PrintStream out = System.out;

    /**
     * Convert a given md to a html.
     *
     * @param mdContent content
     * @return html representation
     */
    public static String mdToHtml(String mdContent) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(mdContent);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    public static void packageToZipFile(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            //Ignore
                        }
                    });
        }
    }

    /**
     * Find a given resource and copy its content recursively to a given target path.
     * @param resource name of the resource
     * @param targetPath target directory path as a string.
     * @throws IOException Failure to load the resources.
     */
    public static void copyResources(String resource, String targetPath) throws IOException {
        URI uri = null;
        try {
            // load the resource from the Class path
            uri = BallerinaDocUtils.class.getClassLoader().getResource(resource).toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to load resources: " + e.getMessage(), e);
        }

        Path source;
        try {
            source = Paths.get(uri);
        } catch (FileSystemNotFoundException e) {
            // this means the resource is in a jar file, hence we have to create a new File system for the jar
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            FileSystems.newFileSystem(uri, env);
            source = Paths.get(uri);
        }
        Path target = Paths.get(targetPath, resource);
        Files.walkFileTree(source, new RecursiveFileVisitor(source, target));
    }
    
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Visits a folder recursively and copy folders and files to a target directory.
     */
    static class RecursiveFileVisitor extends SimpleFileVisitor<Path> {
        Path source;
        Path target;

        public RecursiveFileVisitor(Path aSource, Path aTarget) {
            this.source = aSource;
            this.target = aTarget;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetdir = target.resolve(source.relativize(dir).toString());
            try {
                Files.copy(dir, targetdir);
            } catch (FileAlreadyExistsException e) {
                if (!Files.isDirectory(targetdir)) {
                    throw e;
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("File copied: " + file.toString());
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
