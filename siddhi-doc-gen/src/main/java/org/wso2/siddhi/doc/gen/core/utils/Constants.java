/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.doc.gen.core.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Constants used by the doc generator
 */
public class Constants {
    public static final String DOCS_DIRECTORY = "docs";
    public static final String CLASSES_DIRECTORY = "classes";
    public static final String TEMPLATES_DIRECTORY = "templates";
    public static final String API_SUB_DIRECTORY = "api";

    public static final String FREEMARKER_TEMPLATE_FILE_EXTENSION = ".ftl";
    public static final String CLASS_FILE_EXTENSION = ".class";
    public static final String MARKDOWN_FILE_EXTENSION = ".md";
    public static final String YAML_FILE_EXTENSION = ".yml";

    public static final String MARKDOWN_DOCUMENTATION_TEMPLATE = "documentation";
    public static final String MARKDOWN_EXTENSIONS_INDEX_TEMPLATE = "extensions";
    public static final String MARKDOWN_HOME_PAGE_TEMPLATE = "index";

    public static final String GITHUB_GPL_EXTENSION_REPOSITORY_PREFIX = "siddhi-gpl-";
    public static final String GITHUB_APACHE_EXTENSION_REPOSITORY_PREFIX = "siddhi-";
    public static final String GITHUB_OWNER_WSO2_EXTENSIONS = "wso2-extensions";

    public static final String MKDOCS_CONFIG_PAGES_KEY = "pages";
    public static final String MKDOCS_CONFIG_PAGES_API_KEY = "API Docs";
    public static final String MKDOCS_FILE_SEPARATOR = "/";

    public static final String README_FILE_NAME = "README";
    public static final String MKDOCS_CONFIG_FILE_NAME = "mkdocs";

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String CORE_NAMESPACE = "core";
}
