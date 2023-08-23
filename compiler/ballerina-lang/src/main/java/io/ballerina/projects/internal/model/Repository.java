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

/**
 * Represents the repository object.
 *
 * @since 2201.8.0
 */
public class Repository {
    private String id;
    private String url;
    private String username;
    private String password;

    private Repository(String id, String url, String username, String password) {
        this.id = id;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static Repository from(String id, String url, String username, String password) {
        return new Repository(id, url, username, password);
    }

    public static Repository from() {
        return new Repository("", "", "", "");
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
}
