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
package org.ballerinalang.langserver.compiler.common;

import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Document class to hold the file path used in the LS.
 */
public class LSDocument {
    private Path path;
    private String uri;
    private String projectRoot;
    private List<String> projectModules;
    private boolean withinProject;

    public LSDocument(String uri) {
        try {
            this.uri = uri;
            this.path = Paths.get(new URL(uri).toURI());
            this.projectRoot = LSCompilerUtil.getProjectRoot(this.path);
            if (this.projectRoot == null) {
                return;
            }
            try {
                this.withinProject = !Files.isSameFile(this.path.getParent(), Paths.get(projectRoot));
            } catch (IOException e) {
                withinProject = false;
            }
            if (withinProject) {
                // TODO: Fix project module retrieve logic
                this.projectModules = LSCompilerUtil.getCurrentProjectModules(Paths.get(projectRoot).getParent());
            }
        } catch (URISyntaxException | MalformedURLException e) {
            // Ignore
        }
    }

    public LSDocument(Path path, String projectRoot) {
        this.uri = path.toUri().toString();
        this.projectRoot = projectRoot;
        this.path = path;
    }

    /**
     * Get the path of the given URI.
     *
     * @return {@link Path} get the path
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Get source root path.
     *
     * @return {@link Path} source root path
     */
    public Path getProjectRootPath() {
        return Paths.get(this.projectRoot);
    }

    /**
     * Get the URI of the given string URI.
     *
     * @return {@link URI} get the URI
     * @throws MalformedURLException can throw malformed url exception
     * @throws URISyntaxException    can throw URI syntax exception
     */
    public URI getURI() throws MalformedURLException, URISyntaxException {
        return new URL(uri).toURI();
    }

    /**
     * Get the uri.
     *
     * @return {@link String} get the string uri
     */
    public String getURIString() {
        return this.uri;
    }

    /**
     * Get source root.
     *
     * @return {@link String} source root
     */
    public String getProjectRoot() {
        return this.projectRoot;
    }

    /**
     * Set URI.
     *
     * @param uri string URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Set source root.
     *
     * @param sourceRoot source root
     */
    public void setProjectRootRoot(String sourceRoot) {
        this.projectRoot = sourceRoot;
    }

    /**
     * Returns True when this source file has a ballerina project repository folder.
     *
     * @return True if this file has project repo, False otherwise
     */
    public boolean hasProjectRepo() {
        return RepoUtils.isBallerinaProject(Paths.get(projectRoot));
    }

    /**
     * Get the project modules list.
     * 
     * @return {@link List} list of project modules
     */
    public List<String> getProjectModules() {
        return projectModules;
    }

    public boolean isWithinProject() {
        return withinProject;
    }

    @Override
    public String toString() {
        return "{" + "projectRoot:" + this.projectRoot + ", uri:" + this.uri + "}";
    }
}
