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

package org.ballerinalang.plugins.idea.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.stubs.BallerinaFileStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

/**
 * Represents a ballerina file.
 */
public class BallerinaFile extends PsiFileBase {

    public static final BallerinaImportDeclaration[] EMPTY_ARRAY = new BallerinaImportDeclaration[0];
    public static final ArrayFactory<BallerinaImportDeclaration> ARRAY_FACTORY = count -> count == 0 ?
            EMPTY_ARRAY : new BallerinaImportDeclaration[count];

    public BallerinaFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, BallerinaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return BallerinaFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Ballerina File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

    @Nullable
    @Override
    public BallerinaFileStub getStub() {
        //noinspection unchecked
        return (BallerinaFileStub) super.getStub();
    }

    @NotNull
    private static <E extends PsiElement> List<E> getChildrenByType(@NotNull StubElement<? extends PsiElement> stub,
                                                                    IElementType elementType,
                                                                    ArrayFactory<E> f) {
        return Arrays.asList(stub.getChildrenByType(elementType, f));
    }

    @NotNull
    public List<BallerinaImportDeclaration> getCachedImports() {
        return CachedValuesManager.getCachedValue(this, () -> {
            StubElement<BallerinaFile> stub = getStub();
            List<BallerinaImportDeclaration> imports = stub != null ? getChildrenByType(stub,
                    BallerinaTypes.IMPORT_DECLARATION, ARRAY_FACTORY) : getImports();
            return CachedValueProvider.Result.create(imports, this);
        });
    }

    @NotNull
    private List<BallerinaImportDeclaration> getImports() {
        List<BallerinaImportDeclaration> allImportsInPackage = PsiTreeUtil.getChildrenOfTypeAsList(this,
                BallerinaImportDeclaration.class);
        // We need to get the original file since the file in the context is an in-memory file.
        PsiFile originalFile = getOriginalFile();
        PsiDirectory parent = originalFile.getParent();
        // If the parent is not available, we return the imports in the current file.
        if (parent == null) {
            return allImportsInPackage;
        }
        // Iterate through child files.
        PsiElement[] children = parent.getChildren();
        for (PsiElement child : children) {
            // If the child file is current file or it is not a Ballerina file, we continue with the next file.
            if (child.equals(originalFile) || !(child instanceof BallerinaFile)) {
                continue;
            }
            // Get all imports in the file and iterate through them.
            Collection<BallerinaImportDeclaration> importDeclarations = PsiTreeUtil.findChildrenOfType(child,
                    BallerinaImportDeclaration.class);
            for (BallerinaImportDeclaration importDeclaration : importDeclarations) {
                // Get the package name.
                BallerinaCompletePackageName completePackageName = importDeclaration.getCompletePackageName();
                if (completePackageName == null) {
                    continue;
                }
                // If the package name is already in the import name, we skip that import.
                String currentPackageName = completePackageName.getText();
                if (!isAlreadyImported(allImportsInPackage, currentPackageName)) {
                    allImportsInPackage.add(importDeclaration);
                }
            }
        }
        return allImportsInPackage;
    }

    private static boolean isAlreadyImported(@NotNull List<BallerinaImportDeclaration> allImportsInPackage,
                                             @NotNull String currentPackageName) {
        for (BallerinaImportDeclaration ballerinaImportDeclaration : allImportsInPackage) {
            BallerinaCompletePackageName completePackageName = ballerinaImportDeclaration.getCompletePackageName();
            if (completePackageName == null) {
                continue;
            }
            if (completePackageName.getText().equalsIgnoreCase(currentPackageName)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public MultiMap<String, BallerinaImportDeclaration> getImportMap() {
        return CachedValuesManager.getCachedValue(this, () -> {
            MultiMap<String, BallerinaImportDeclaration> map = MultiMap.createLinked();
            List<Object> dependencies = ContainerUtil.newArrayList(this);
            for (BallerinaImportDeclaration importDeclaration : getCachedImports()) {
                PsiElement shortPackageName = importDeclaration.getShortPackageName();
                if (shortPackageName != null) {
                    map.putValue(shortPackageName.getText(), importDeclaration);
                }
            }
            return CachedValueProvider.Result.create(map, ArrayUtil.toObjectArray(dependencies));
        });
    }

    public List<BallerinaDefinition> getDefinitions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaDefinition.class);
    }
}
