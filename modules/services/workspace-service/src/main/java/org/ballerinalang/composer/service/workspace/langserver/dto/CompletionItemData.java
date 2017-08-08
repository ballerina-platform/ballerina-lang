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

package org.ballerinalang.composer.service.workspace.langserver.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent Completion Item Data
 */
public class CompletionItemData {

    private Map<String, Object> data;

    public CompletionItemData() {
        data = new HashMap<String, Object>();
    }

    public void addData(String key, Object value) {
        data.put(key, value);
    }
}
