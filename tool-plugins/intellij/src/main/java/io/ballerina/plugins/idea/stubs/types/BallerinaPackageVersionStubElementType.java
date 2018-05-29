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

package io.ballerina.plugins.idea.stubs.types;

import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import io.ballerina.plugins.idea.stubs.BallerinaPackageVersionStub;
import io.ballerina.plugins.idea.psi.BallerinaPackageVersion;
import io.ballerina.plugins.idea.psi.impl.BallerinaPackageVersionImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


/**
 * Represents package version stub element.
 */
public class BallerinaPackageVersionStubElementType extends
        BallerinaNamedStubElementType<BallerinaPackageVersionStub, BallerinaPackageVersion> {

    public BallerinaPackageVersionStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public BallerinaPackageVersion createPsi(@NotNull BallerinaPackageVersionStub stub) {
        return new BallerinaPackageVersionImpl(stub, this);
    }

    @NotNull
    @Override
    public BallerinaPackageVersionStub createStub(@NotNull BallerinaPackageVersion psi, StubElement parentStub) {
        return new BallerinaPackageVersionStub(parentStub, this, psi.getName(), psi.isPublic());
    }

    @Override
    public void serialize(@NotNull BallerinaPackageVersionStub stub, @NotNull StubOutputStream dataStream)
            throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeBoolean(stub.isPublic());
    }

    @NotNull
    @Override
    public BallerinaPackageVersionStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub)
            throws IOException {
        return new BallerinaPackageVersionStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
    }
}
