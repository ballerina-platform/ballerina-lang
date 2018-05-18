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

package org.ballerinalang.plugins.idea.stubs.types;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaGlobalEndpointDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaNamedElement;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaGlobalEndpointDefinitionImpl;
import org.ballerinalang.plugins.idea.stubs.BallerinaGlobalEndpointDefinitionStub;
import org.ballerinalang.plugins.idea.stubs.index.BallerinaGlobalEndpointIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents global endpoint stub element.
 */
public class BallerinaGlobalEndpointDefinitionStubElementType extends
        BallerinaNamedStubElementType<BallerinaGlobalEndpointDefinitionStub, BallerinaGlobalEndpointDefinition> {

    private static final ArrayList<StubIndexKey<String, ? extends BallerinaNamedElement>> EXTRA_KEYS =
            ContainerUtil.newArrayList(BallerinaGlobalEndpointIndex.KEY);

    public BallerinaGlobalEndpointDefinitionStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public BallerinaGlobalEndpointDefinition createPsi(@NotNull BallerinaGlobalEndpointDefinitionStub stub) {
        return new BallerinaGlobalEndpointDefinitionImpl(stub, this);
    }

    @NotNull
    @Override
    public BallerinaGlobalEndpointDefinitionStub createStub(@NotNull BallerinaGlobalEndpointDefinition psi, StubElement
            parentStub) {
        return new BallerinaGlobalEndpointDefinitionStub(parentStub, this, psi.getName(), psi.isPublic());
    }

    @Override
    public void indexStub(@NotNull BallerinaGlobalEndpointDefinitionStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (shouldIndex() && StringUtil.isNotEmpty(name)) {
            sink.occurrence(BallerinaGlobalEndpointIndex.KEY, name);
        }
    }

    @Override
    public void serialize(@NotNull BallerinaGlobalEndpointDefinitionStub stub, @NotNull StubOutputStream dataStream)
            throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeBoolean(stub.isPublic());
    }

    @NotNull
    @Override
    public BallerinaGlobalEndpointDefinitionStub deserialize(@NotNull StubInputStream dataStream,
                                                             StubElement parentStub) throws IOException {
        return new BallerinaGlobalEndpointDefinitionStub(parentStub, this, dataStream.readName(),
                dataStream.readBoolean());
    }

    @NotNull
    @Override
    protected Collection<StubIndexKey<String, ? extends BallerinaNamedElement>> getExtraIndexKeys() {
        return EXTRA_KEYS;
    }
}
