/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.encoding;

/**
 * Constants related to Ballerina encoding stdlib.
 *
 * @since 0.991.0
 */
public class Constants {

    // Name of the Ballerina encoding module, used to create struct instances.
    public static final String ENCODING_PACKAGE = "ballerina/encoding";

    // Error record for encoding module.
    public static final String ENCODING_ERROR = "EncodingError";

    // Message field within error record.
    public static final String MESSAGE = "message";

    // Error code for encoding error
    public static final String ENCODING_ERROR_CODE = "{ballerina/encoding}EncodingError";

}
