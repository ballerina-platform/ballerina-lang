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
package org.ballerinalang.composer.service.workspace.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.SystemUtils;
import org.ballerinalang.composer.service.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Workspace implementation for local file system.
 */
public class LocalFSWorkspace implements Workspace {

    private static final Logger logger = LoggerFactory.getLogger(LocalFSWorkspace.class);
    private static final String FOLDER_TYPE = "folder";
    private static final String CONTENT = "content";
    private static final String BAL_EXT = ".bal";

    @Override
    public JsonArray listRoots() throws IOException {
        final Iterable<Path> rootDirs = FileSystems.getDefault().getRootDirectories();
        List<Path> rootDirsList = new ArrayList<>();
        rootDirs.forEach(rootDirsList::add);
        return getJsonArrayForDirs(rootDirsList);
    }

    private JsonArray getJsonArrayForDirs(List<Path> rootDirs) {
        JsonArray rootArray = new JsonArray();
        for (Path root : rootDirs) {
            JsonObject rootObj = getJsonObjForFile(root, false);
            try {
                if (Files.isDirectory(root) && Files.list(root).count() > 0) {
                    JsonArray children = listFilesInPath(root.toFile().getAbsolutePath(), Arrays.asList(BAL_EXT));
                    rootObj.add("children", children);
                }
            } catch (IOException e) {
                logger.debug("Error while traversing children of " + e.toString(), e);
                rootObj.addProperty("error", e.toString());
            }
            if (Files.isDirectory(root)) {
                rootArray.add(rootObj);
            }
        }
        return rootArray;
    }

    @Override
    public JsonArray getRoots(List<Path> rootPaths) throws IOException {
        return getJsonArrayForDirs(rootPaths);
    }

    @Override
    public JsonArray listDirectoriesInPath(String path) throws IOException {
        Path ioPath = Paths.get(path);
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(ioPath).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            if (Files.isDirectory(next) && !Files.isHidden(next)) {
                JsonObject jsnObj = getJsonObjForFile(next, true);
                dirs.add(jsnObj);
            }
        }
        return dirs;
    }

    @Override
    public void write(String path, String content) throws IOException {
        Path ioPath = Paths.get(path);
        Files.write(ioPath, content.getBytes(Charset.defaultCharset()));
    }

    @Override
    public JsonObject read(String path) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(path));
        JsonObject content = new JsonObject();
        content.addProperty(CONTENT, new String(fileContent, Charset.defaultCharset()));
        return content;
    }

    @Override
    public void delete(String path, String type) throws IOException {
        Path ioPath = Paths.get(path);
        if (FOLDER_TYPE.equals(type)) {
            Files.walk(ioPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
                 .forEach(File::delete);
        } else {
            Files.delete(ioPath);
        }
    }

    @Override
    public void create(String path, String type) throws IOException {
        Path ioPath = Paths.get(path);
        if (FOLDER_TYPE.equals(type)) {
            Files.createDirectories(ioPath);
        } else {
            Files.createFile(ioPath);
        }
    }

    @Override
    public void log(String loggerID, String timestamp, String level, String url, String message, String layout)
            throws IOException {
        Logger frontEndLog = LoggerFactory.getLogger(loggerID);
        String logMessage = "client-timestamp: " + timestamp + ", page: " + url + ", message: " + message;
        switch (level) {
            case "TRACE":
                frontEndLog.trace(logMessage);
                break;
            case "INFO":
                frontEndLog.info(logMessage);
                break;
            case "WARN":
                frontEndLog.warn(logMessage);
                break;
            case "ERROR":
            case "FATAL":
                frontEndLog.error(logMessage);
                break;
            default:
                frontEndLog.debug(logMessage);
        }

    }

    private JsonObject getJsonObjForFile(Path root, boolean checkChildren) {
        JsonObject rootObj = new JsonObject();
        if (null != root.getFileName()) {
            Path fileName = root.getFileName();
            if (null != fileName) {
                rootObj.addProperty("text", fileName.toString());
            }
        } else {
            rootObj.addProperty("text", root.toString());
        }
        rootObj.addProperty("id", root.toAbsolutePath().toString());
        if (Files.isDirectory(root) && checkChildren) {
            rootObj.addProperty("type", "folder");
            try {
                if (Files.list(root).count() > 0) {
                    rootObj.addProperty("children", Boolean.TRUE);
                } else {
                    rootObj.addProperty("children", Boolean.FALSE);
                }
            } catch (IOException e) {
                logger.debug("Error while fetching children of " + root.toString(), e);
                rootObj.addProperty("error", e.toString());
            }
        } else if (Files.isRegularFile(root) && checkChildren) {
            rootObj.addProperty("type", "file");
            rootObj.addProperty("children", Boolean.FALSE);
        }
        return rootObj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonArray listFilesInPath(String path, List<String> extensions) throws IOException {
        Path ioPath = Paths.get(path);
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(ioPath).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            if ((Files.isDirectory(next) || Files.isRegularFile(next)) && !Files.isHidden(next) &&
                !isWindowsSystemFile(next)) {
                JsonObject jsnObj = getJsonObjForFile(next, true);
                if (Files.isRegularFile(next)) {
                    Path fileName = next.getFileName();
                    SuffixFileFilter fileFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);
                    if (null != fileName && fileFilter.accept(next.toFile())) {
                        dirs.add(jsnObj);
                    }
                } else {
                    dirs.add(jsnObj);
                }

            }
        }
        return dirs;
    }

    @Override
    public JsonObject exists(String path) throws IOException {
        Path ioPath = Paths.get(path);
        JsonObject result = new JsonObject();
        boolean exists = Files.exists(ioPath);
        result.addProperty("file", path);
        result.addProperty("exists", exists);
        return result;
    }

    private boolean isWindowsSystemFile(Path filePath) throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            DosFileAttributes dosAttribs = Files.readAttributes(filePath, DosFileAttributes.class);
            return dosAttribs.isSystem();
        }
        return false;
    }
}
