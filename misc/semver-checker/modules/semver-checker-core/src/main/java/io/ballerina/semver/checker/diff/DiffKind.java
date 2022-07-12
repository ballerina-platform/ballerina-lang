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

/**
 * Represents diff kinds that can exist within Ballerina packages.
 *
 * @since 2201.2.0
 */
public enum DiffKind {
    PACKAGE("package"),
    MODULE("module"),

    FUNCTION("function"),
    SERVICE("service"),
    SERVICE_ANNOTATION("service annotation"),
    SERVICE_LISTENER_EXPR("service listener expression"),
    SERVICE_FIELD("service field"),
    REMOTE_FUNCTION("remote function"),
    RESOURCE_FUNCTION("resource function"),

    MODULE_CONST("module constant"),
    MODULE_CONST_ANNOTATION("module constant annotation"),
    MODULE_CONST_INIT("module constant initializer"),

    MODULE_VAR("module variable"),
    MODULE_VAR_ANNOTATION("module variable annotation"),
    MODULE_VAR_INIT("module variable initializer"),
    DOCUMENTATION("documentation"),
    UNKNOWN("unknown"),

    CLASS("class"),
    OBJECT_FIELD("object field"),
    OBJECT_FIELD_EXPR("object field expression");
    public final String name;

    DiffKind(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
