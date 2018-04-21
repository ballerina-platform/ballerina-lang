/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.code.generator;

import java.io.File;

/**
 * Constants for ballerina code generator.
 */
public class GeneratorConstants {

    /**
     * Enum to select the code generation mode.
     *
     */
    public enum GenType {
        CLIENT("client"), OPENAPI("oas3.0"), SWAGGER("swagger");
        private String name;
        GenType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static final String CLIENT_TEMPLATE_NAME = "client";
    public static final String OPEN_API_TEMPLATE_NAME = "skeleton";
    public static final String SWAGGER_TEMPLATE_NAME = "skeleton";

    public static final String TEMPLATES_SUFFIX = ".mustache";
    public static final String TEMPLATES_DIR_PATH_KEY = "templates.dir.path";
    public static final String DEFAULT_TEMPLATE_DIR = File.separator + "templates";
    public static final String DEFAULT_CLIENT_DIR = DEFAULT_TEMPLATE_DIR + File.separator + "client";
    public static final String DEFAULT_OPEN_API_DIR = DEFAULT_TEMPLATE_DIR + File.separator + "oas3";
    public static final String DEFAULT_SWAGGER_DIR = DEFAULT_TEMPLATE_DIR + File.separator + "swagger2";

    public static final String RES_CONFIG_ANNOTATION = "ResourceConfig";
    public static final String HTTP_PKG_ALIAS = "http";
    public static final String SWAGGER_PKG_ALIAS = "swagger";

    public static final String ATTR_METHODS = "methods";
    public static final String ATTR_CONSUMES = "consumes";
    public static final String ATTR_PATH = "path";
    public static final String ATTR_HTTP_PORT = "port";
    public static final String ATTR_HTTPS_PORT = "httpsPort";
    public static final String ATTR_HOST = "host";
    public static final String ATTR_BASE_PATH = "basePath";
    public static final String ATTR_HTTP = "http://";
    public static final String ATTR_HTTPS = "https://";
    public static final String ATTR_DEF_HOST = "localhost";
    public static final String ATTR_DEF_PORT = "80";
}
