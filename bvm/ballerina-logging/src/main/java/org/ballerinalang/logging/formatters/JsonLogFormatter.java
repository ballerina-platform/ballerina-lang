/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.logging.formatters;

import com.google.gson.Gson;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * JSON log formatter for formatting HTTP trace log files.
 *
 * @since 0.970.0
 */
public class JsonLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return new Gson().toJson(record) + System.getProperty("line.separator");
    }
}
