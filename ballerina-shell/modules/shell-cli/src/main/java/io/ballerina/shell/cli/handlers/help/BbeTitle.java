/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.handlers.help;

import java.util.Arrays;

/**
 * Helper class to Read BBE Records.
 */
public class BbeTitle {

    private String title;
    private String column;
    private String category;
    private BbeRecord[] samples;

    public BbeTitle(String title, String column, String category, BbeRecord[] samples) {
        this.title = title;
        this.column = column;
        this.category = category;
        this.samples = Arrays.copyOf(samples, samples.length);
    }

    public String getTitle() {
        return title;
    }

    public String getColumn() {
        return column;
    }

    public String getCategory() {
        return category;
    }

    public BbeRecord[] getSamples() {
        return Arrays.copyOf(samples, samples.length);
    }
}
