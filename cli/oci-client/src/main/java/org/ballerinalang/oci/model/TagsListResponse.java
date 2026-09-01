/*
 * Copyright (c) 2026, WSO2 LLC. (http://wso2.com).
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

package org.ballerinalang.oci.model;

import java.util.List;

/**
 * Response body of the OCI registry tags list endpoint ({@code GET /v2/{name}/tags/list}).
 *
 * @param tags the tag names present in the repository
 */
public record TagsListResponse(List<String> tags) {

    public TagsListResponse {
        tags = tags == null ? null : List.copyOf(tags);
    }
}
