/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.webview.diagram.preview.javafx;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

class ImageRefreshFix {
    private ImageRefreshFix() {
    }

    @NotNull
    static String setStamps(@NotNull String html) {
        final VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        final String pattern = "<img src=\"file:";

        StringBuilder sb = new StringBuilder();
        int processedOffset = 0;
        while (true) {
            final int nextI = html.indexOf(pattern, processedOffset);
            if (nextI == -1) {
                break;
            }

            final int nextJ = html.indexOf('"', nextI + pattern.length());
            if (nextJ == -1) {
                return html;
            }

            sb.append(html, processedOffset, nextI + pattern.length());

            final String url = html.substring(nextI + pattern.length(), nextJ);
            sb.append(processUrl(virtualFileManager, url));
            sb.append('"');
            processedOffset = nextJ + 1;
        }

        if (processedOffset < html.length()) {
            sb.append(html, processedOffset, html.length());
        }
        return sb.toString();
    }

    @NotNull
    private static String processUrl(@NotNull VirtualFileManager virtualFileManager, @NotNull String url) {
        final VirtualFile virtualFile = virtualFileManager.findFileByUrl("file://" + url);
        if (virtualFile == null) {
            return url;
        }
        return url + "?stamp=" + virtualFile.getModificationStamp();
    }
}
