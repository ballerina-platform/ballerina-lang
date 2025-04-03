/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.packages;

/**
 * Ballerina package service constants.
 *
 * @since 2.0.0
 */
public final class PackageServiceConstants {

    static final String CAPABILITY_NAME = "ballerinaPackage";
    static final String MAIN_FUNCTION = "main";
    static final String NAME = "name";
    static final String FILE_PATH = "filePath";
    static final String START_LINE = "startLine";
    static final String START_COLUMN = "startColumn";
    static final String END_LINE = "endLine";
    static final String END_COLUMN = "endColumn";
    static final String FUNCTIONS = "functions";
    static final String SERVICES = "services";
    static final String MODULES = "modules";
    static final String RESOURCES = "resources";
    static final String RECORDS = "records";
    static final String OBJECTS = "objects";
    static final String TYPES = "types";
    static final String CONSTANTS = "constants";
    static final String ENUMS = "enums";
    static final String CLASSES = "classes";
    static final String LISTENERS = "listeners";
    static final String MODULE_LEVEL_VARIABLE = "moduleVariables";
    static final String CONFIGURABLE_VARIABLES = "configurableVariables";
    static final String AUTOMATIONS = "automations";
    static final String NATURAL_FUNCTIONS = "naturalFunctions";

    private PackageServiceConstants() {
    }
}
