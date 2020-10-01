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

package org.ballerinalang.ballerina.openapi.convertor.service;

/**
 * This class will hold all constants related to openapi ballerina conversion.
 */
public class ConverterConstants {
    public static final String YAML_EXTENSION = ".yaml";
    public static final String OPENAPI_SUFFIX = "-openapi";

    // annotation attribute keys
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_CODE = "code";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_HEADER_TYPE = "headerType";
    public static final String ATTR_IN = "inInfo";
    public static final String ATTR_REQUIRED = "required";
    public static final String ATTR_SUMMARY = "summary";
    public static final String ATTR_PARAM = "parameters";
    public static final String ATTR_URL = "url";
    public static final String ATTR_REQUEST = "Request";
    public static final String ATTR_TERMS = "termsOfService";
    public static final String ATTR_TITLE = "title";
    public static final String ATTR_SERVICE_VERSION = "serviceVersion";
    public static final String ATTR_HEADERS = "headers";
    public static final String ATTR_TAGS = "tags";
    public static final String ATTR_EXT_DOC = "externalDoc";
    public static final String ATTR_TYPE = "paramType";

    // annotation keys
    public static final String ANNON_RES_INFO = "ResourceInfo";
    public static final String ANNON_MULTI_RES_INFO = "MultiResourceInfo";
}
