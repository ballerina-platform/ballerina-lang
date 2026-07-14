/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com).
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

package io.ballerina.projects.internal.model;

import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.projects.internal.SettingsBuilder.MAVEN;

/**
 * Represents the repository object.
 *
 * @since 2201.8.0
 */
public class Repository {

    public static final String MODE_HOSTED = "hosted";
    public static final String MODE_PROXY = "proxy";

    private final String id;
    private final String url;
    private final String username;
    private final String password;
    private final String type;
    private final Path path;
    private final String mode;

    private Repository(String id, String url, String username, String password, String remoteType, Path path,
                        String mode) {
        this.id = id;
        this.url = url;
        this.username = username;
        this.password = password;
        this.type = remoteType;
        this.path = path;
        this.mode = (mode == null || mode.isEmpty()) ? MODE_HOSTED : mode;
    }

    public static Repository from(String id, String url, String username, String password) {
        return new Repository(id, url, username, password, MAVEN, null, MODE_HOSTED);
    }

    public static Repository from(String id, String url, String username, String password, String type, Path path) {
        return new Repository(id, url, username, password, type, path, MODE_HOSTED);
    }

    public static Repository from(String id, String url, String username, String password, String type, Path path,
                                   String mode) {
        return new Repository(id, url, username, password, type, path, mode);
    }

    public static Repository from() {
        return new Repository("", "", "", "", MAVEN, null, MODE_HOSTED);
    }

    public String id() {
        return id;
    }

    public String url() {
        return url;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String type() {
        return type;
    }

    public Optional<Path> path() {
        return Optional.ofNullable(path);
    }

    public String mode() {
        return mode;
    }
}
