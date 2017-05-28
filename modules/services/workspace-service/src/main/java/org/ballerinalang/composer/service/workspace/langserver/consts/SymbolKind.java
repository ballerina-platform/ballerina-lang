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

package org.ballerinalang.composer.service.workspace.langserver.consts;

/**
 * Symbol kind Constants
 */
public class SymbolKind {
    public static final int FILE = 1;

    public static final int PACKAGE_DEF = 2;

    public static final int IMPORT_DEF = 3;

    public static final int SERVICE_DEF = 4;

    public static final int FUNCTION_DEF = 5;

    public static final int CONNECTOR_DEF = 6;

    public static final int RESOURCE_DEF = 7;

    public static final int ACTION_DEF = 8;

    public static final int VARIABLE_DEF = 9;

    public static final int BUILTIN_TYPE = 10;
}

