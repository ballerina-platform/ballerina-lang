/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IndexableFileSet;
import io.ballerina.plugins.idea.BallerinaModuleType;
import io.ballerina.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Parent class related to code insight tests.
 */
public abstract class BallerinaCodeInsightFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {

    private static String MOCK_SDK_VERSION = "0.981.0";

    protected static String getTestDataPath(String path) {
        return "src/test/resources/testData/" + path;
    }

    @NotNull
    private static DefaultLightProjectDescriptor createMockProjectDescriptor() {
        return new DefaultLightProjectDescriptor() {

            @NotNull
            @Override
            public Sdk getSdk() {
                return createMockSdk(MOCK_SDK_VERSION);
            }

            @NotNull
            @Override
            public ModuleType getModuleType() {
                return BallerinaModuleType.getInstance();
            }
        };
    }

    @NotNull
    private static DefaultLightProjectDescriptor createMockProjectDescriptorWithoutSourceRoot() {
        return new DefaultLightProjectDescriptor() {

            @NotNull
            @Override
            public ModuleType getModuleType() {
                return BallerinaModuleType.getInstance();
            }

            @Nullable
            @Override
            protected VirtualFile createSourceRoot(@NotNull Module module, String srcPath) {
                VirtualFile dummyRoot = VirtualFileManager.getInstance().findFileByUrl("temp:///");
                assert dummyRoot != null;
                dummyRoot.refresh(false, false);

                VirtualFile srcRoot;
                try {
                    srcRoot = dummyRoot;
                    cleanSourceRoot(srcRoot);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                IndexableFileSet indexableFileSet = new IndexableFileSet() {
                    @Override
                    public boolean isInSet(@NotNull VirtualFile file) {
                        return file.getFileSystem() == srcRoot.getFileSystem() && module.getProject().isOpen();
                    }

                    @Override
                    public void iterateIndexableFilesIn(@NotNull VirtualFile file, @NotNull ContentIterator iterator) {
                        VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor() {
                            @Override
                            public boolean visitFile(@NotNull VirtualFile file) {
                                iterator.processFile(file);
                                return true;
                            }
                        });
                    }
                };
                FileBasedIndex.getInstance().registerIndexableSet(indexableFileSet, null);
                Disposer.register(module.getProject(), () -> FileBasedIndex.getInstance().removeIndexableSet(indexableFileSet));

                return srcRoot;
            }

            private void cleanSourceRoot(@NotNull VirtualFile contentRoot) throws IOException {
                TempFileSystem tempFs = (TempFileSystem)contentRoot.getFileSystem();
                for (VirtualFile child : contentRoot.getChildren()) {
                    if (!tempFs.exists(child)) {
                        tempFs.createChildFile(this, contentRoot, child.getName());
                    }
                    child.delete(this);
                }
            }
        };
    }

    protected void setUpProjectSdk() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            Sdk sdk = getProjectDescriptor().getSdk();
            ProjectJdkTable.getInstance().addJdk(sdk);
            ProjectRootManager.getInstance(myFixture.getProject()).setProjectSdk(sdk);
        });
    }

    @NotNull
    private static Sdk createMockSdk(@NotNull String version) {
        String homePath = new File(getTestDataPath("mockSdk-") + version + "/").getAbsolutePath();
        BallerinaSdkType sdkType = BallerinaSdkType.getInstance();
        ProjectJdkImpl sdk = new ProjectJdkImpl("Ballerina " + version, sdkType, homePath, version);
        sdkType.setupSdkPaths(sdk);
        sdk.setVersionString(version);
        return sdk;
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return isSdkAware() ? createMockProjectDescriptor() : createMockProjectDescriptorWithoutSourceRoot();
    }

    protected boolean isSdkAware() {
        return annotatedWith(BallerinaSDKAware.class);
    }
}
