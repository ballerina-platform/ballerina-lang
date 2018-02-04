/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.fs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for the file system.
 */
public interface FileSystem {

    JsonArray listRoots(List<String> extensionList) throws IOException;

    JsonArray getJsonForRoots(List<Path> rootPaths, List<String> extensionList) throws IOException;

    JsonArray listFilesInPath(String path, List<String> extensions) throws IOException;

    JsonObject exists(String path) throws IOException;

    void move(String src, String dest) throws IOException;

    void copy(String src, String dest) throws IOException;

    void create(String path, String type, String content) throws IOException;

    void delete(String path) throws IOException;

    void write(String path, String content) throws IOException;

    JsonObject read(String path) throws IOException;

    String getUserHome();
}
