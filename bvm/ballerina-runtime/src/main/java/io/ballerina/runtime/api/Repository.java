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

import java.util.List;

/**
 * External API to be used by the remote-management module to provide Ballerina runtime artifacts.
 *
 * @since 2201.9.0
 */
public interface Repository {

    /**
     * Get the list of runtime artifacts.
     * @return List of artifacts that contains information about the active services and listeners.
     */
    public List<Artifact> getArtifacts();

    /**
     * Get the current Ballerina node.
     * @return Ballerina node.
     */
    public Node getNode();
}
