/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.natives.annotation.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * File Visitor class that will visits all built-in ballerina files and 
 * populate the built-in packages list.
 */
public class PackageFinder extends SimpleFileVisitor<Path>  {

    private Path basePath;
    private List<String> builtInPackages;

    /**
     * Create a package finder.
     * 
     * @param basePath Root directory to start traversing
     * @param builtInPackages List to populate the native packages
     */
    public PackageFinder(Path basePath, List<String> builtInPackages) {
        this.basePath = basePath;
        this.builtInPackages = builtInPackages;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String pkg = basePath.relativize(file.getParent()).toString().replace(File.separator, ".");
        builtInPackages.add(pkg);
        return FileVisitResult.CONTINUE;
    }
}
