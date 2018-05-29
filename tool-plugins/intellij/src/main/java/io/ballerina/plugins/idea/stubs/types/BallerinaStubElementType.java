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

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubBase;
import io.ballerina.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.BallerinaCompositeElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


/**
 * Parent class of stub elements.
 *
 * @param <S> stub type
 * @param <T> definition type
 */
public abstract class BallerinaStubElementType<S extends StubBase<T>, T extends BallerinaCompositeElement>
        extends IStubElementType<S, T> {

    public BallerinaStubElementType(@NonNls @NotNull String debugName) {
        super(debugName, BallerinaLanguage.INSTANCE);
    }

    @Override
    @NotNull
    public String getExternalId() {
        return "ballerina." + super.toString();
    }

    @Override
    public void indexStub(@NotNull S stub, @NotNull IndexSink sink) {

    }
}
