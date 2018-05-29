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
import io.ballerina.plugins.idea.stubs.BallerinaOrgNameStub;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.psi.impl.BallerinaOrgNameImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


/**
 * Represents organization name stub element.
 */
public class BallerinaOrgNameStubElementType extends
        BallerinaNamedStubElementType<BallerinaOrgNameStub, BallerinaOrgName> {

    public BallerinaOrgNameStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public BallerinaOrgName createPsi(@NotNull BallerinaOrgNameStub stub) {
        return new BallerinaOrgNameImpl(stub, this);
    }

    @NotNull
    @Override
    public BallerinaOrgNameStub createStub(@NotNull BallerinaOrgName psi, StubElement parentStub) {
        return new BallerinaOrgNameStub(parentStub, this, psi.getName(), psi.isPublic());
    }

    @Override
    public void serialize(@NotNull BallerinaOrgNameStub stub, @NotNull StubOutputStream dataStream)
            throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeBoolean(stub.isPublic());
    }

    @NotNull
    @Override
    public BallerinaOrgNameStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub)
            throws IOException {
        return new BallerinaOrgNameStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
    }
}
