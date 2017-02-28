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

package org.ballerinalang.plugins.idea.run.configuration;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BallerinaConsoleFilter implements Filter {

    private static final Pattern BALLERINA_FILE_PATTERN = Pattern.compile("(\\w+\\.bal):(\\d+):(\\d+)?");
    private Project myProject;

    public BallerinaConsoleFilter(Project project) {
        this.myProject = project;
    }

    @Nullable
    @Override
    public Result applyFilter(String line, int entireLength) {
        Matcher fileMatcher = BALLERINA_FILE_PATTERN.matcher(line);
        List<ResultItem> resultItems = ContainerUtil.newArrayList();
        while (fileMatcher.find()) {
            VirtualFile file = findSingleFile(fileMatcher.group(1));
            if (file != null) {
                if (fileMatcher.group(3) != null) {
                    resultItems.add(createResult(line, entireLength, fileMatcher.start(1), fileMatcher.end(3),
                            fileMatcher.group(2), fileMatcher.group(3), file));
                } else {
                    resultItems.add(createResult(line, entireLength, fileMatcher.start(1), fileMatcher.end(2),
                            fileMatcher.group(2), "0", file));
                }
            }
        }
        return !resultItems.isEmpty() ? new Result(resultItems) : null;
    }

    @Nullable
    private VirtualFile findSingleFile(@NotNull String fileName) {
        if (PathUtil.isValidFileName(fileName)) {
            Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(myProject, fileName,
                    GlobalSearchScope.allScope(myProject));
            if (files.size() == 1) {
                return ContainerUtil.getFirstItem(files);
            }
        }
        return null;
    }

    @NotNull
    private Result createResult(@NotNull String line, int entireLength, int startOffset, int endOffset, String
            lineNumber, String columnNumber, @NotNull VirtualFile virtualFile) {
        HyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(myProject, virtualFile,
                Integer.parseInt(lineNumber) - 1, Integer.parseInt(columnNumber));
        int lineStart = entireLength - line.length();
        return new Result(lineStart + startOffset, lineStart + endOffset, hyperlinkInfo);
    }
}
