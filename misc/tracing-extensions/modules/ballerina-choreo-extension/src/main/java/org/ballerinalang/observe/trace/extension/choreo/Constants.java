
/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

package org.ballerinalang.observe.trace.extension.choreo;

/**
 * This is the constants class that defines all the constants
 * that are used by the {@link OpenTracerExtension}.
 */
public class Constants {

    private Constants() {}

    public static final String EXTENSION_NAME = "choreo";

    public static final String REPORTER_HOST_NAME_CONFIG = "reporter.hostname";
    public static final String DEFAULT_REPORTER_HOSTNAME = "localhost";

    public static final String REPORTER_PORT_CONFIG = "reporter.port";
    public static final int DEFAULT_REPORTER_PORT = 9411;

    public static final String REPORTER_USE_SSL_CONFIG = "reporter.useSSL";
    public static final boolean DEFAULT_REPORTER_USE_SSL = true;
}
