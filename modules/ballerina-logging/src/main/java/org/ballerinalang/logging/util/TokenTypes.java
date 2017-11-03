/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.logging.util;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by pubudu on 11/1/17.
 */
public class TokenTypes {

    public static final int LOG_FIELD = 1;
    public static final int LOG_RECORD_PART = 2;

    public static final String FMT_TIMESTAMP = "{{timestamp}}";
    public static final String FMT_LEVEL = "{{level}}";
    public static final String FMT_LOGGER = "{{logger}}";
    public static final String FMT_PACKAGE = "{{package}}";
    public static final String FMT_CLASS = "{{class}}";
    public static final String FMT_UNIT = "{{unit}}";
    public static final String FMT_FILE = "{{file}}";
    public static final String FMT_LINE = "{{line}}";
    public static final String FMT_WORKER = "{{worker}}";
    public static final String FMT_MESSAGE = "{{msg}}";
    public static final String FMT_ERROR = "{{err}}";

    public static final Set<String> VALID_LOG_FIELDS = Collections.unmodifiableSet(Stream.of(
            TokenTypes.FMT_TIMESTAMP,
            TokenTypes.FMT_LEVEL,
            TokenTypes.FMT_LOGGER,
            TokenTypes.FMT_PACKAGE,
            TokenTypes.FMT_UNIT,
            TokenTypes.FMT_FILE,
            TokenTypes.FMT_LINE,
            TokenTypes.FMT_WORKER,
            TokenTypes.FMT_MESSAGE,
            TokenTypes.FMT_ERROR
    ).collect(Collectors.toSet()));
}
