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

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;
import io.ballerina.plugins.idea.psi.BallerinaNamedElement;
import io.ballerina.plugins.idea.stubs.BallerinaNamedStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Named stub element parent class.
 *
 * @param <S> stub type
 * @param <T> definition type
 */
public abstract class BallerinaNamedStubElementType<S extends BallerinaNamedStub<T>, T extends BallerinaNamedElement>
        extends BallerinaStubElementType<S, T> {

    public BallerinaNamedStubElementType(@NonNls @NotNull String debugName) {
        super(debugName);
    }

    @Override
    public boolean shouldCreateStub(@NotNull ASTNode node) {
        if (!super.shouldCreateStub(node)) {
            return false;
        }
        PsiElement psi = node.getPsi();
        return psi instanceof BallerinaNamedElement && StringUtil.isNotEmpty(((BallerinaNamedElement) psi).getName());
    }

    //    @Override
    //    public void indexStub(@NotNull S stub, @NotNull IndexSink sink) {
    //        String name = stub.getName();
    //        if (shouldIndex() && StringUtil.isNotEmpty(name)) {
    //            String packageName = null;
    //            StubElement parent = stub.getParentStub();
    //            while (parent != null) {
    //                if (parent instanceof GoFileStub) {
    //                    packageName = ((GoFileStub) parent).getPackageName();
    //                    break;
    //                }
    //                parent = parent.getParentStub();
    //            }
    //
    //            String indexingName = StringUtil.isNotEmpty(packageName) ? packageName + "." + name : name;
    //            if (stub.isPublic()) {
    //                sink.occurrence(GoAllPublicNamesIndex.ALL_PUBLIC_NAMES, indexingName);
    //            } else {
    //                sink.occurrence(GoAllPrivateNamesIndex.ALL_PRIVATE_NAMES, indexingName);
    //            }
    //            for (StubIndexKey<String, ? extends GoNamedElement> key : getExtraIndexKeys()) {
    //                sink.occurrence(key, name);
    //            }
    //
    //
    //        }
    //    }

    protected boolean shouldIndex() {
        return true;
    }

    @NotNull
    protected Collection<StubIndexKey<String, ? extends BallerinaNamedElement>> getExtraIndexKeys() {
        return Collections.emptyList();
    }
}
