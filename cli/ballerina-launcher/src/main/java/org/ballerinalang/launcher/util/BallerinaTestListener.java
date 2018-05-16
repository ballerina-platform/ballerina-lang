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

package org.ballerinalang.launcher.util;

import org.ballerinalang.stdlib.utils.GenerateBalo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * A listener for loading ballerina standard libs prior to running the tests.
 *
 * @since 0.975.0
 */
public class BallerinaTestListener implements ISuiteListener {

    private static final String BALLERINA_HOME = "ballerina.home";
    private static Logger log = LoggerFactory.getLogger(BallerinaTestListener.class);
    private static Path stdlibs = Paths.get("target", "libs");
    private static String ballerinaHome;

    @Override
    public void onStart(ISuite iSuite) {
        copyStandardLibs();
    }

    @Override
    public void onFinish(ISuite iSuite) {
        cleanupStandardLibs();
    }

    private void copyStandardLibs() {
        try {
            cleanupStandardLibs();
            ballerinaHome = System.getProperty(BALLERINA_HOME);
            Path stdlibModule =
                    Paths.get(GenerateBalo.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path stdlibCompileZip = stdlibModule.getParent().resolve("stdlib-compile-0.970.1-SNAPSHOT.zip");

            if (!stdlibs.toFile().exists()) {
                Files.createDirectory(stdlibs);
            }

            Path zip = stdlibs.resolve("stdlib-compile-0.970.1-SNAPSHOT.zip");
            Files.copy(stdlibCompileZip, zip);
            System.setProperty(BALLERINA_HOME, stdlibs.getParent().toAbsolutePath().toString());
            log.info(BALLERINA_HOME + " is set to: " + System.getProperty(BALLERINA_HOME));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("error occured while loading standard libs: " + e.getMessage(), e);
        }
    }

    private void cleanupStandardLibs() {
        if (ballerinaHome != null) {
            System.setProperty(BALLERINA_HOME, ballerinaHome);
        }
        deleteFile(stdlibs);
    }

    private void deleteFile(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignore) {
        }
    }
}
