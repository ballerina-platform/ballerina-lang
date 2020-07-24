/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */
package org.ballerinalang.datamapper.utils;

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
    public static final String ENABLE_OLD_PARSER_FOR_TESTS = "enableOldParserForTests";
    public static final String MODULE_INIT_CLASS_NAME = "___init";

    //HTTP2 related Constants
    public static final long HTTP2_RESPONSE_TIME_OUT = 10;
    public static final TimeUnit HTTP2_RESPONSE_TIME_UNIT = TimeUnit.SECONDS;
}
