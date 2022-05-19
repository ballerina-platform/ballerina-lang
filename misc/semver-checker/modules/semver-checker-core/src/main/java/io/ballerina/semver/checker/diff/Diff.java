/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.diff;

import com.google.gson.JsonElement;

import java.util.List;

/**
 * Base definition for all the Ballerina source diff types. All the diff types are defined to be immutable (hence
 * thread-safe).
 *
 * @since 2201.2.0
 */
public interface Diff {

    DiffType getType();

    SemverImpact getVersionImpact();

    void computeVersionImpact();

    List<? extends Diff> getChildDiffs();

    List<? extends Diff> getChildDiffs(SemverImpact versionImpact);

    String getAsString();

    JsonElement getAsJson();
}
