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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.stubs.BallerinaAnnotationDefinitionStub;
import io.ballerina.plugins.idea.stubs.index.BallerinaAnnotationIndex;
import org.ballerinalang.plugins.idea.psi.BallerinaAnnotationDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaNamedElement;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaAnnotationDefinitionImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents annotation definition stub element.
 */
public class BallerinaAnnotationDefinitionStubElementType extends
        BallerinaNamedStubElementType<BallerinaAnnotationDefinitionStub, BallerinaAnnotationDefinition> {

    private static final ArrayList<StubIndexKey<String, ? extends BallerinaNamedElement>> EXTRA_KEYS =
            ContainerUtil.newArrayList(BallerinaAnnotationIndex.KEY);

    public BallerinaAnnotationDefinitionStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public BallerinaAnnotationDefinition createPsi(@NotNull BallerinaAnnotationDefinitionStub stub) {
        return new BallerinaAnnotationDefinitionImpl(stub, this);
    }

    @NotNull
    @Override
    public BallerinaAnnotationDefinitionStub createStub(@NotNull BallerinaAnnotationDefinition psi, StubElement
            parentStub) {
        return new BallerinaAnnotationDefinitionStub(parentStub, this, psi.getName(), psi.isPublic());
    }

    @Override
    public void indexStub(@NotNull BallerinaAnnotationDefinitionStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (shouldIndex() && StringUtil.isNotEmpty(name)) {
            sink.occurrence(BallerinaAnnotationIndex.KEY, name);
        }
    }

    @Override
    public void serialize(@NotNull BallerinaAnnotationDefinitionStub stub, @NotNull StubOutputStream dataStream)
            throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeBoolean(stub.isPublic());
    }

    @NotNull
    @Override
    public BallerinaAnnotationDefinitionStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub)
            throws IOException {
        return new BallerinaAnnotationDefinitionStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
    }

    @NotNull
    @Override
    protected Collection<StubIndexKey<String, ? extends BallerinaNamedElement>> getExtraIndexKeys() {
        return EXTRA_KEYS;
    }
}
