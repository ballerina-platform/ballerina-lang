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
import org.apache.commons.io.FilenameUtils;
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.io.IOCase.INSENSITIVE;

/**
 * Workspace implementation for local file system.
 */
public class LocalFSWorkspace implements Workspace {

    private static final Logger logger = LoggerFactory.getLogger(LocalFSWorkspace.class);
    private static final String FOLDER_TYPE = "folder";
    private static final String FILE_TYPE = "file";
    private static final String NODE_ID = "id";
    private static final String NODE_TYPE = "type";
    private static final String NODE_LABEL = "label";
    private static final String NODE_CHILDREN = "children";
    private static final String NODE_ERROR = "error";
    private static final String FILE_CONTENT = "fileContent";
    private static final String FILE_NAME = "fileName";
    private static final String FILE_PATH = "filePath";
    private static final String FILE_FULL_PATH = "fileFullPath";
    private static final String EXTENSION = "extension";
    private static final String EXISTS = "exists";

    @Override
    public JsonArray listRoots(List<String> extensionList) throws IOException {
        final Iterable<Path> rootDirs = FileSystems.getDefault().getRootDirectories();
        List<Path> rootDirsList = new ArrayList<>();
        rootDirs.forEach(rootDirsList::add);
        return getJsonArrayForDirs(rootDirsList, extensionList);
    }

    @Override
    public JsonArray getJsonForRoots(List<Path> rootPaths, List<String> extensionList) throws IOException {
        return getJsonArrayForDirs(rootPaths, extensionList);
    }

    @Override
    public JsonObject exists(String path) throws IOException {
        Path ioPath = Paths.get(path);
        JsonObject result = new JsonObject();
        boolean exists = Files.exists(ioPath);
        result.addProperty(FILE_TYPE, path);
        result.addProperty(EXISTS, exists);
        return result;
    }

    @Override
    public JsonObject read(String path) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(path));
        JsonObject fileObject = new JsonObject();
        fileObject.addProperty(FILE_FULL_PATH, path);
        fileObject.addProperty(FILE_NAME, FilenameUtils.getBaseName(path));
        fileObject.addProperty(FILE_PATH, FilenameUtils.getFullPath(path));
        fileObject.addProperty(EXTENSION, FilenameUtils.getExtension(path));
        fileObject.addProperty(FILE_CONTENT, new String(fileContent, Charset.defaultCharset()));
        return fileObject;
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
    public void write(String path, String content) throws IOException {
        Path ioPath = Paths.get(path);
        Files.write(ioPath, content.getBytes(Charset.defaultCharset()));
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
                JsonObject jsnObj = getJsonObjForFile(next, extensions);
                if (Files.isRegularFile(next)) {
                    Path fileName = next.getFileName();
                    SuffixFileFilter fileFilter = new SuffixFileFilter(extensions, INSENSITIVE);
                    if (null != fileName && fileFilter.accept(next.toFile())) {
                        jsnObj.addProperty(FILE_FULL_PATH, next.toString());
                        jsnObj.addProperty(FILE_NAME, FilenameUtils.getBaseName(next.toString()));
                        jsnObj.addProperty(FILE_PATH, FilenameUtils.getFullPath(next.toString()));
                        jsnObj.addProperty(EXTENSION, FilenameUtils.getExtension(next.toString()));
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

    private JsonArray getJsonArrayForDirs(List<Path> rootDirs, List<String> extensionList) {
        JsonArray rootArray = new JsonArray();
        for (Path root : rootDirs) {
            JsonObject rootObj = getJsonObjForFile(root, extensionList);
            if (Files.isDirectory(root)) {
                rootArray.add(rootObj);
            }
        }
        return rootArray;
    }

    private JsonObject getJsonObjForFile(Path root, List<String> extensions) {
        JsonObject rootObj = new JsonObject();
        Path fileName = root.getFileName();
        if (fileName != null) {
            rootObj.addProperty(NODE_LABEL, fileName.toString());
        } else {
            rootObj.addProperty(NODE_LABEL, root.toString());
        }
        rootObj.addProperty(NODE_ID, root.toAbsolutePath().toString());
        if (Files.isDirectory(root)) {
            rootObj.addProperty(NODE_TYPE, FOLDER_TYPE);
            try {
                File rootFile = root.toFile();
                boolean hasChildren = Boolean.FALSE;
                if (rootFile != null) {
                    // check if this folder contains sub-directories
                    File[] subDirs = rootFile.listFiles(File::isDirectory);
                    if (subDirs != null && subDirs.length > 0) {
                        hasChildren = Boolean.TRUE;
                    } else {
                        // if there are no sub-directories, check whether this folder
                        // contains files with given extension list
                        String[] list = extensions != null && !extensions.isEmpty()
                                ? rootFile.list(new SuffixFileFilter(extensions, INSENSITIVE))
                                : rootFile.list();
                        if (list != null && list.length > 0) {
                            hasChildren = true;
                        }
                    }
                }
                rootObj.addProperty(NODE_CHILDREN, hasChildren);
            } catch (Exception e) {
                logger.debug("Error while fetching children of " + root.toString(), e);
                rootObj.addProperty(NODE_ERROR, e.toString());
            }
        } else if (Files.isRegularFile(root)) {
            rootObj.addProperty(NODE_TYPE, FILE_TYPE);
            rootObj.addProperty(NODE_CHILDREN, Boolean.FALSE);
        }
        return rootObj;
    }

    private boolean isWindowsSystemFile(Path filePath) throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            DosFileAttributes dosAttribs = Files.readAttributes(filePath, DosFileAttributes.class);
            return dosAttribs.isSystem();
        }
        return false;
    }
}
