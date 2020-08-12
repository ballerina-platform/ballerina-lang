/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.formatter.core;

/**
 * A model for formatting options that could be passed onto the formatter.
 */
public class FormattingOptions {

    // Size of a tab in spaces.
    private int tabSize;

    // Prefer spaces over tabs.
    private boolean insertSpaces;

    FormattingOptions() {
        this.tabSize = 4;
        this.insertSpaces = true;
    }

    FormattingOptions(int tabSize, boolean insertSpaces) {
        this.tabSize = tabSize;
        this.insertSpaces = insertSpaces;
    }

    public int getTabSize() {
        return tabSize;
    }

    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    public boolean isInsertSpaces() {
        return insertSpaces;
    }

    public void setInsertSpaces(boolean insertSpaces) {
        this.insertSpaces = insertSpaces;
    }
}
