/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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

package io.ballerina.runtime.api;

import java.util.HashMap;
import java.util.Map;

public class Artifact {
    public final String name;
    public final ArtifactType type;
    public final Map<String, Object> details;

    public enum ArtifactType {
        SERVICE
    }

    public Artifact(String name, ArtifactType type) {
        this.name = name;
        this.type = type;
        this.details = new HashMap<>();
    }

    public void addDetail(String detailsKey, Object value) {
        this.details.put(detailsKey, value);
    }

    public Object getDetail(String detailKey) {
        return details.getOrDefault(detailKey, null);
    }
}
