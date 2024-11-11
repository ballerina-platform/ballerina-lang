/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.diagramutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Utility class for syntax tree gen tests.
 */
public final class TestUtil {

    public static final Path RES_DIR = Path.of("src/test/resources/").toAbsolutePath();
    public static final Path BUILD_DIR = Path.of("build/").toAbsolutePath();

    private TestUtil() {
    }

    public static Path createTempFile(Path filePath) throws IOException {
        Path tempFilePath = BUILD_DIR.resolve("tmp")
                .resolve(UUID.randomUUID() + ".bal");
        Files.copy(filePath, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath;
    }

    public static Path createTempProject(Path projectPath) throws IOException {
        Path tempFilePath = BUILD_DIR.resolve("tmp")
                .resolve(UUID.randomUUID() + "");
        Path directoryPath = Files.createDirectory(tempFilePath);
        Files.copy(projectPath, directoryPath, StandardCopyOption.REPLACE_EXISTING);
        FileVisitor fileVisitor = new FileVisitor(projectPath, directoryPath);
        Files.walkFileTree(projectPath, fileVisitor);
        return directoryPath;
    }
}
