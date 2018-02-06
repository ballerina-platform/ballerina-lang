/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.formatter.settings;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.jetbrains.annotations.NotNull;

public class BallerinaCodeStyleConfigurable extends CodeStyleAbstractConfigurable {

    public BallerinaCodeStyleConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
        super(settings, cloneSettings, "Ballerina");
    }

    @NotNull
    @Override
    protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
        return new BallerinaCodeStyleMainPanel(getCurrentSettings(), settings);
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    private static class BallerinaCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {

        private BallerinaCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(BallerinaLanguage.INSTANCE, currentSettings, settings);
        }

        @Override
        protected void addSpacesTab(CodeStyleSettings settings) {
        }

        @Override
        protected void addBlankLinesTab(CodeStyleSettings settings) {
        }

        @Override
        protected void addWrappingAndBracesTab(CodeStyleSettings settings) {
        }
    }
}
