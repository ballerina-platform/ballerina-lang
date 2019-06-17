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
 */

package io.ballerina.plugins.idea.sdk;

import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.libraries.LibraryPresentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import icons.BallerinaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.swing.Icon;

/**
 * Provides the presentation for Ballerina SDK.
 */
public class BallerinaSdkLibraryPresentationProvider extends LibraryPresentationProvider<DummyLibraryProperties> {

    private static final LibraryKind KIND = LibraryKind.create("Ballerina");

    public BallerinaSdkLibraryPresentationProvider() {
        super(KIND);
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return BallerinaIcons.ICON;
    }

    @Override
    @Nullable
    public DummyLibraryProperties detect(@NotNull List<VirtualFile> classesRoots) {
        for (VirtualFile root : classesRoots) {
            if (BallerinaSmallIDEsSdkService.isBallerinaSdkLibRoot(root)) {
                return DummyLibraryProperties.INSTANCE;
            }
        }
        return null;
    }
}
