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
package org.wso2.integration.tooling.service.workspace.cloud;

import com.google.gson.JsonArray;
import org.wso2.integration.tooling.service.workspace.Workspace;

import java.io.IOException;

/**
 * Workspace implementation for cloud file system.
 */
public class CloudWorkspace  implements Workspace {
    @Override
    public JsonArray listRoots() throws IOException {
        return null;
    }

    @Override
    public JsonArray listDirectoriesInPath(String path) {
        return null;
    }

    @Override
    public void write(String path, String content) throws IOException {

    }

    @Override
    public void log(String logger, String timestamp, String level, String URL, String message, String layout) throws IOException {

    }
    // TODO: This is just for future use.
}
