/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.plugins.idea.formatter.settings;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Ballerina code style settings provider.
 */
public class BallerinaLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    private static final String DEFAULT_CODE_SAMPLE =
            "import ballerina.io;\n" +
                    "\n" +
                    "function main(string... args) {\n" +
                    "\tio:println(\"Hello\");\n" +
                    "}";

    @NotNull
    @Override
    public Language getLanguage() {
        return BallerinaLanguage.INSTANCE;
    }

    @NotNull
    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return DEFAULT_CODE_SAMPLE;
    }

    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor();
    }

    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(getLanguage());
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
        indentOptions.INDENT_SIZE = 4;
        indentOptions.CONTINUATION_INDENT_SIZE = 4;
        indentOptions.TAB_SIZE = 4;
        indentOptions.USE_TAB_CHARACTER = false;

        defaultSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false;
        defaultSettings.LINE_COMMENT_AT_FIRST_COLUMN = false;
        return defaultSettings;
    }
}
