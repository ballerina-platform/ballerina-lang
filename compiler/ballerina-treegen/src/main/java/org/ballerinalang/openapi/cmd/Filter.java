/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.cmd;

import java.util.ArrayList;
import java.util.List;

/**
 * This model use for storing the filter tags and operations details.
 */
public class Filter {
    private List<String> tags = new ArrayList<>();
    private List<String> operations = new ArrayList<>();

    public Filter(List<String> tags, List<String> operations) {
        this.tags = tags;
        this.operations = operations;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getOperations() {
        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }
}
