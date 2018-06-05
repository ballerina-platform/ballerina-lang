/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.utils;

import java.io.File;

/**
 * Constants that use in .bal file generation.
 */
public class BalGenConstants {
    public static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");
    
    public static final String FILE_SEPARATOR = File.separator;
    
    public static final String RESOURCE_SEPARATOR = "/";
    
    public static final int SERVICE_INDEX = 0;
    
    public static final String BLOCKING_STUB_KEY = "BLOCKING";
    
    public static final String NON_BLOCKING_STUB_KEY = "NON-BLOCKING";
    
    public static final String DEFAULT_SAMPLE_CONNECTOR_PORT = "9090";
    
    public static final String DEFAULT_SAMPLE_CONNECTOR_HOST = "localhost";
    
    public static final String DEFAULT_PACKAGE = "client";
    
    public static final String GRPC_NATIVE_PACKAGE = "ballerina.grpc";
    
    public static final String STUB_FILE_PREFIX = "_pb.bal";
    
    public static final String SAMPLE_FILE_PREFIX = "_sample_client.bal";
    
    public static final String EMPTY_STRING = "";
    
    public static final String EMPTY_DATA_TYPE = "Empty";
    
    public static final String PACKAGE_SEPARATOR = ".";
    
    public static final String PACKAGE_SEPARATOR_REGEX = "\\.";
    
    static final String TEMPLATES_SUFFIX = ".mustache";
    
    static final String TEMPLATES_DIR_PATH_KEY = "templates.dir.path";
    
    private static final String DEFAULT_TEMPLATE_DIR = RESOURCE_SEPARATOR + "templates";
    
    public static final String DEFAULT_SKELETON_DIR = DEFAULT_TEMPLATE_DIR + RESOURCE_SEPARATOR + "skeleton";
    
    public static final String DEFAULT_SAMPLE_DIR = DEFAULT_TEMPLATE_DIR + RESOURCE_SEPARATOR + "skeleton";
    
    public static final String SKELETON_TEMPLATE_NAME = "clientStub";
    
    public static final String SAMPLE_TEMPLATE_NAME = "sample";
}
