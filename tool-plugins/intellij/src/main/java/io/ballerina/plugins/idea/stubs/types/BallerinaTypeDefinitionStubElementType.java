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
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.impl.BallerinaTypeDefinitionImpl;
import io.ballerina.plugins.idea.stubs.BallerinaTypeDefinitionStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Represents type definition stub element.
 */
public class BallerinaTypeDefinitionStubElementType extends
        BallerinaNamedStubElementType<BallerinaTypeDefinitionStub, BallerinaTypeDefinition> {

//    private static final ArrayList<StubIndexKey<String, ? extends BallerinaNamedElement>> EXTRA_KEYS =
//            ContainerUtil.newArrayList(BallerinaFunctionIndex.KEY);

    public BallerinaTypeDefinitionStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public BallerinaTypeDefinition createPsi(@NotNull BallerinaTypeDefinitionStub stub) {
        return new BallerinaTypeDefinitionImpl(stub, this);
    }

    @NotNull
    @Override
    public BallerinaTypeDefinitionStub createStub(@NotNull BallerinaTypeDefinition psi, StubElement
            parentStub) {
        return new BallerinaTypeDefinitionStub(parentStub, this, psi.getName(), psi.isPublic());
    }

    @Override
    public void indexStub(@NotNull BallerinaTypeDefinitionStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (shouldIndex() && StringUtil.isNotEmpty(name)) {
//            sink.occurrence(BallerinaFunctionIndex.KEY, name);
        }
    }

    @Override
    public void serialize(@NotNull BallerinaTypeDefinitionStub stub, @NotNull StubOutputStream dataStream)
            throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeBoolean(stub.isPublic());
    }

    @NotNull
    @Override
    public BallerinaTypeDefinitionStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub)
            throws IOException {
        return new BallerinaTypeDefinitionStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
    }

//    @NotNull
//    @Override
//    protected Collection<StubIndexKey<String, ? extends BallerinaNamedElement>> getExtraIndexKeys() {
//        return EXTRA_KEYS;
//    }
}
