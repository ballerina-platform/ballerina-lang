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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Document class to hold the file path used in the LS.
 */
public class LSDocument {
    private String uri;
    private String sourceRoot;

    public LSDocument() {
    }

    public LSDocument(String uri) {
        this.uri = uri;
    }

    public LSDocument(String uri, String sourceRoot) {
        this.sourceRoot = sourceRoot;
        this.uri = uri;
    }

    /**
     * Get the path of the given URI.
     *
     * @return {@link Path} get the path
     * @throws MalformedURLException can throw malformed url exception
     * @throws URISyntaxException    can throw URI syntax exception
     */
    public Path getPath() throws MalformedURLException, URISyntaxException {
        return Paths.get(new URL(uri).toURI());
    }

    /**
     * Get source root path.
     *
     * @return {@link Path} source root path
     */
    public Path getSourceRootPath() {
        return Paths.get(this.sourceRoot);
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
    public String getSourceRoot() {
        return this.sourceRoot;
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
    public void setSourceRoot(String sourceRoot) {
        this.sourceRoot = sourceRoot;
    }
}
