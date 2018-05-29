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

package org.ballerinalang.plugins.idea.stubs;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.tree.IStubFileElementType;
import io.ballerina.plugins.idea.BallerinaFileElementType;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stub for file.
 */
public class BallerinaFileStub extends PsiFileStubImpl<BallerinaFile> {

    public BallerinaFileStub(@Nullable BallerinaFile file) {
        super(file);
    }

    @NotNull
    @Override
    public IStubFileElementType getType() {
        return BallerinaFileElementType.INSTANCE;
    }

//    @Nullable
//    public StubElement<BallerinaPackageDeclaration> getPackageClauseStub() {
//        return findChildStubByType(BallerinaPackageDeclarationStubElementType.INSTANCE);
//    }

//    @Nullable
//    public String getPackageName() {
//        StubElement<BallerinaPackageDeclaration> stub = getPackageClauseStub();
//        return stub instanceof BallerinaPackageDeclarationStub ? ((BallerinaPackageDeclarationStub) stub).getName() :
//                null;
//    }
}
