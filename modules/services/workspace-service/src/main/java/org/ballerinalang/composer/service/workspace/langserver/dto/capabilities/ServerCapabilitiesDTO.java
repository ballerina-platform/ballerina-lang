/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.langserver.dto.capabilities;

/**
 * DTO for server capabilities
 */
public class ServerCapabilitiesDTO {

    private boolean hoverProvider;

    private boolean renameProvider;

    public boolean isHoverProvider() {
        return hoverProvider;
    }

    public void setHoverProvider(boolean hoverProvider) {
        this.hoverProvider = hoverProvider;
    }

    public boolean isRenameProvider() {
        return renameProvider;
    }

    public void setRenameProvider(boolean renameProvider) {
        this.renameProvider = renameProvider;
    }
}
