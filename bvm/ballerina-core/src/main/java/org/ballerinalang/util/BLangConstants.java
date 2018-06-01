/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.util;

/**
 * This file contains a list of constant values used by Ballerina Compiler and the Bytecode interpreter.
 *
 * @since 0.90
 */
public class BLangConstants {

    public static final int MAGIC_NUMBER = 0xBA1DA4CE;
    public static final short VERSION_NUMBER = 17;

    public static final String MAIN_FUNCTION_NAME = "main";
    public static final String INIT_FUNCTION_SUFFIX = ".<init>";
    public static final String CONSTRUCTOR_FUNCTION_SUFFIX = "new";
    public static final String START_FUNCTION_SUFFIX = ".<start>";
    public static final String STOP_FUNCTION_SUFFIX = ".<stop>";

    public static final String BLANG_SRC_FILE_EXT = "bal";
    public static final String BLANG_SRC_FILE_SUFFIX = "." + BLANG_SRC_FILE_EXT;

    public static final String BLANG_EXEC_FILE_EXT = "balx";
    public static final String BLANG_EXEC_FILE_SUFFIX = "." + BLANG_EXEC_FILE_EXT;

    public static final String BLANG_COMPILED_PACKAGE_FILE_EXT = "balo";
    public static final String BLANG_COMPILED_PACKAGE_FILE_SUFFIX = "." + BLANG_COMPILED_PACKAGE_FILE_EXT;

    // int, float, string, boolean, blob, reference type
    public static final int NO_OF_VAR_TYPE_CATEGORIES = 6;
    public static final int INT_OFFSET = 0;
    public static final int FLOAT_OFFSET = 1;
    public static final int STRING_OFFSET = 2;
    public static final int BOOL_OFFSET = 3;
    public static final int BLOB_OFFSET = 4;
    public static final int REF_OFFSET = 5;

    public static final String USER_REPO_ENV_KEY = "BALLERINA_REPOSITORY";
    public static final String USER_REPO_DEFAULT_DIRNAME = ".ballerina";
    public static final String USER_REPO_ARTIFACTS_DIRNAME = "artifacts";
    public static final String USER_REPO_SRC_DIRNAME = "src";
    public static final String USER_REPO_OBJ_DIRNAME = "obj";
    public static final String USER_REPO_METADATA_DIRNAME = "metadata";

    public static final String BALLERINA_BUILTIN_PKG_PREFIX = "ballerina";

    public static final String USER_HOME = "user.home";
    
    // Zero value for string
    public static final String STRING_NULL_VALUE = null;
    
    // Empty value for string
    public static final String STRING_EMPTY_VALUE = "";

    // Empty value for blob
    public static final byte[] BLOB_EMPTY_VALUE = new byte[0];
}
