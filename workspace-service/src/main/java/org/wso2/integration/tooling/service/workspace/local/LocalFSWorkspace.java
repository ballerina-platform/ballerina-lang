/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.integration.tooling.service.workspace.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.integration.tooling.service.workspace.Workspace;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Workspace implementation for local file system.
 */
public class LocalFSWorkspace implements Workspace {

    private static final Logger logger = LoggerFactory.getLogger(LocalFSWorkspace.class);

    @Override
    public JsonArray listRoots() throws IOException  {
        final Iterable<Path> rootDirs = FileSystems.getDefault().getRootDirectories();
        JsonArray rootArray = new JsonArray();
        for (Path root: rootDirs){
            JsonObject rootObj = getJsonObjForFile(root, false);
            try {
                if(Files.isDirectory(root) && Files.list(root).count() > 0){
                    JsonArray children = new JsonArray();
                    Iterator<Path> rootItr = Files.list(root).iterator();
                    while (rootItr.hasNext()){
                        Path next = rootItr.next();
                        JsonObject childObj = getJsonObjForFile(next, true);
                        if(Files.isDirectory(next)){
                            children.add(childObj);
                        }
                    }
                    rootObj.add("children", children);
                }
            } catch (IOException e) {
                logger.debug("Error while traversing children of " + e.toString(), e);
                rootObj.addProperty("error", e.toString());
            }
            if(Files.isDirectory(root)){
                rootArray.add(rootObj);
            }
        }
        return rootArray;
    }

    @Override
    public JsonArray listDirectoriesInPath(String path) throws IOException {
        Path ioPath = Paths.get(path);
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(ioPath).iterator();
        while (iterator.hasNext()){
            Path next = iterator.next();
            if(Files.isDirectory(next)){
                JsonObject jsnObj = getJsonObjForFile(next, true);
                dirs.add(jsnObj);
            }
        }
        return dirs;
    }

    @Override
    public void write(String path, String content) throws IOException {
        Path ioPath = Paths.get(path);
        Files.write(ioPath, content.getBytes());
    }

    private JsonObject getJsonObjForFile(Path root, boolean checkChildren) {
        JsonObject rootObj = new JsonObject();
        rootObj.addProperty("text", root.getFileName() != null ? root.getFileName().toString() : root.toString());
        rootObj.addProperty("id", root.toAbsolutePath().toString());
        if(Files.isDirectory(root) && checkChildren){
            try {
                if(Files.list(root).count() > 0){
                    rootObj.addProperty("children", Boolean.TRUE);
                }else{
                    rootObj.addProperty("children", Boolean.FALSE);
                }
            } catch (IOException e) {
                logger.debug("Error while fetching children of " + root.toString(), e);
                rootObj.addProperty("error", e.toString());
            }
        }
        return rootObj;
    }
}