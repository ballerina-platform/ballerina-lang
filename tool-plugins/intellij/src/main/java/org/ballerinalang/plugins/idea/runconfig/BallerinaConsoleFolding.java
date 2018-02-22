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

package org.ballerinalang.plugins.idea.runconfig;

import com.intellij.execution.ConsoleFolding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides Ballerina run command folding in console support.
 */
public class BallerinaConsoleFolding extends ConsoleFolding {

    private static final String REGEX = "ballerina(.bat)? (run|build|test) .+";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public boolean shouldFoldLine(@NotNull String line) {
        Matcher matcher = PATTERN.matcher(line);
        return matcher.find();
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull List<String> lines) {
        return "<...>";
    }
}
