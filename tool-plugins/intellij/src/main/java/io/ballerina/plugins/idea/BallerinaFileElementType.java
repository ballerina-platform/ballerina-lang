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

package io.ballerina.plugins.idea;

import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.stubs.BallerinaFileStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Represent a BallerinaFileElementType.
 */
public class BallerinaFileElementType extends IStubFileElementType<BallerinaFileStub> {

    public static final IStubFileElementType INSTANCE = new BallerinaFileElementType();
    // Note - Change the version in the corresponding index class if a stub is changed.
    public static final int VERSION = 5;

    private BallerinaFileElementType() {
        super("BALLERINA_FILE", BallerinaLanguage.INSTANCE);
    }

    @Override
    public int getStubVersion() {
        return VERSION;
    }

    @NotNull
    @Override
    public StubBuilder getBuilder() {
        return new DefaultStubBuilder() {
            @NotNull
            @Override
            protected StubElement createStubForFile(@NotNull PsiFile file) {
                if (file instanceof BallerinaFile) {
                    return new BallerinaFileStub((BallerinaFile) file);
                }
                return super.createStubForFile(file);
            }
        };
    }

    @Override
    public void indexStub(@NotNull BallerinaFileStub stub, @NotNull IndexSink sink) {
        super.indexStub(stub, sink);
    }

    @Override
    public void serialize(@NotNull BallerinaFileStub stub, @NotNull StubOutputStream dataStream) throws IOException {

    }

    @NotNull
    @Override
    public BallerinaFileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws
            IOException {
        return new BallerinaFileStub(null);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "ballerina.FILE";
    }
}
