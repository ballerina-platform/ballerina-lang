/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.util;

import java.util.concurrent.TimeUnit;

/**
 * Constants used in test cases.
 */
public class TestConstant {
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String CHARSET_NAME = "UTF-8";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
    public static final String HTTP_METHOD_HEAD = "HEAD";
    public static final String ENCODING_GZIP = "gzip";
    public static final String DEFLATE = "deflate";
    public static final String IDENTITY = "identity";
    public static final String ENABLE_JBALLERINA_TESTS = "enableJBallerinaTests";
    public static final String MODULE_INIT_CLASS_NAME = "$_init";

    //HTTP2 related Constants
    public static final long HTTP2_RESPONSE_TIME_OUT = 10;
    public static final TimeUnit HTTP2_RESPONSE_TIME_UNIT = TimeUnit.SECONDS;
}
