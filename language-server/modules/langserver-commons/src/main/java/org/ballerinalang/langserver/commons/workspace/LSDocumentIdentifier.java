/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.workspace;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

/**
 * Document class identifier interface to hold the file path used in the LS.
 */
public interface LSDocumentIdentifier {
    /**
     * Get the path of the given URI.
     *
     * @return {@link Path} get the path
     */
    Path getPath();

    /**
     * Get source root path.
     *
     * @return {@link Path} source root path
     */
    Path getProjectRootPath();

    /**
     * Get the URI of the given string URI.
     *
     * @return {@link URI} get the URI
     * @throws MalformedURLException can throw malformed url exception
     * @throws URISyntaxException    can throw URI syntax exception
     */
    URI getURI() throws MalformedURLException, URISyntaxException;

    /**
     * Get the uri.
     *
     * @return {@link String} get the string uri
     */
    String getURIString();

    /**
     * Get source root.
     *
     * @return {@link String} source root
     */
    String getProjectRoot();

    /**
     * Set URI.
     *
     * @param uri string URI
     */
    void setUri(String uri);

    /**
     * Set source root.
     *
     * @param sourceRoot source root
     */
    void setProjectRootRoot(String sourceRoot);

    /**
     * Get the project modules list.
     *
     * @return {@link List} list of project modules
     */
    List<String> getProjectModules();

    boolean isWithinProject();

    String getOwnerModule();

    Path getOwnerModulePath();
}
