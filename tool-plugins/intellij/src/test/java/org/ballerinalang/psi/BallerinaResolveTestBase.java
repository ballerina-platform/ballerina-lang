/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.psi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Parent class for resolve tests.
 */
public abstract class BallerinaResolveTestBase extends BallerinaCodeInsightFixtureTestCase {

    @NotNull
    private static final String REF_MARK = "/*ref*/";
    @NotNull
    private static final String NO_REF_MARK = "/*no ref*/";
    @NotNull
    private static final String DEF_MARK = "/*def*/";

    @Nullable
    private PsiReference myReference;
    @Nullable
    private PsiElement myDefinition;
    private boolean myShouldBeResolved = true;

    @Override
    protected String getBasePath() {
        return "psi/resolve";
    }

    protected void doFileTest() {
        PsiFile file = myFixture.configureByFile(getTestName(false) + ".bal");

        String fileContent = loadText(file.getVirtualFile());

        int referenceIndex = getReferenceIndex(fileContent);
        int noReferenceIndex = getNoReferenceIndex(fileContent);
        int definitionIndex = getDefinitionIndex(fileContent);
        if (definitionIndex != -1 && definitionIndex < referenceIndex) {
            referenceIndex -= DEF_MARK.length();
        } else if (referenceIndex != -1 && definitionIndex > referenceIndex) {
            definitionIndex -= REF_MARK.length();
        }

        fileContent = fileContent.replace(REF_MARK, "");
        fileContent = fileContent.replace(NO_REF_MARK, "");
        fileContent = fileContent.replace(DEF_MARK, "");

        // Delete the current file. Otherwise it will cause false positives and tests will fail.
        ApplicationManager.getApplication().runWriteAction(file::delete);

        PsiFile newFile = myFixture.configureByText(BallerinaFileType.INSTANCE, fileContent);

        processPsiFile(newFile, referenceIndex, noReferenceIndex, definitionIndex);

        doResolveTest();
    }

    protected void doFileTest(@NotNull String textInDefinition, String... filePath) throws IOException {
        if (filePath == null || filePath.length == 0) {
            filePath = new String[1];
            filePath[0] = "test.bal";
        }
        int definitionIndex = getDefinitionIndex(textInDefinition);
        textInDefinition = textInDefinition.replace(DEF_MARK, "");

        PsiFile definitionFile = myFixture.addFileToProject(filePath[0], textInDefinition);
        PsiFile referenceFile = myFixture.configureByFile(getTestName(false) + ".bal");
        String referenceFileContent = loadText(referenceFile.getVirtualFile());
        deleteFile(referenceFile);

        int referenceIndex = getReferenceIndex(referenceFileContent);
        int noReferenceIndex = getNoReferenceIndex(referenceFileContent);
        referenceFileContent = referenceFileContent.replace(REF_MARK, "");
        referenceFileContent = referenceFileContent.replace(NO_REF_MARK, "");

        referenceFile = myFixture.configureByText(BallerinaFileType.INSTANCE, referenceFileContent);

        processPsiFile(referenceFile, definitionFile, referenceIndex, noReferenceIndex, definitionIndex);

        doResolveTest();
    }

    private static void deleteFile(@NotNull PsiFile referenceFile) throws IOException {
        ApplicationManager.getApplication().runWriteAction(
                new ThrowableComputable<Void, IOException>() {
                    @Override
                    public Void compute() throws IOException {
                        referenceFile.getVirtualFile().delete(this);
                        return null;
                    }

                }
        );
    }

    private void doResolveTest() {
        if (myReference == null) {
            fail("no reference defined in test case");
        }
        if (myShouldBeResolved && !allowNullDefinition() && myDefinition == null) {
            fail("no definition defined in test case");
        }

        PsiElement resolve = myReference.resolve();
        if (myShouldBeResolved) {
            assertNotNull("cannot resolve reference " + myReference.getCanonicalText(), resolve);
            if (myDefinition != null) {
                PsiElement def = PsiTreeUtil.getParentOfType(myDefinition, resolve.getClass(), false);
                assertSame("element resolved in non-expected element from " + getFileName(resolve) + ":\n" +
                        resolve.getText(), def, resolve);
            }
        } else if (resolve != null) {
            fail("element is resolved but it wasn't should. resolved to element from " + getFileName(resolve) + ":\n"
                    + resolve.getText());
        }
    }

    @NotNull
    private static String getFileName(@NotNull PsiElement resolve) {
        return resolve instanceof PsiFile ? ((PsiFile) resolve).getName() : resolve.getContainingFile().getName();
    }

    protected boolean allowNullDefinition() {
        return false;
    }

    private void processPsiFile(PsiFile file, int refIndex, int noRefIndex, int defIndex) {
        String fileName = file.getName();
        if (refIndex != -1) {
            myReference = findReference(file, refIndex);
        }
        if (noRefIndex != -1) {
            myReference = findReference(file, noRefIndex);
            myShouldBeResolved = false;
        }

        if (defIndex != -1) {
            if (myDefinition != null) {
                fail("only one definition should be allowed in a resolve test case, see file: " + fileName);
            }

            myDefinition = file.findElementAt(defIndex);
            if (myDefinition == null) {
                fail("no definition was found at mark in file: " + fileName + ", offset: " + defIndex);
            }
        }
    }

    private void processPsiFile(PsiFile referenceFile, PsiFile definitionFile, int refIndex, int noRefIndex,
                                int defIndex) {
        String fileName = referenceFile.getName();
        if (refIndex != -1) {
            myReference = findReference(referenceFile, refIndex);
        }
        if (noRefIndex != -1) {
            myReference = findReference(referenceFile, noRefIndex);
            myShouldBeResolved = false;
        }

        if (defIndex != -1) {
            if (myDefinition != null) {
                fail("only one definition should be allowed in a resolve test case, see file: " + fileName);
            }

            myDefinition = definitionFile.findElementAt(defIndex);
            if (myDefinition == null) {
                fail("no definition was found at mark in file: " + fileName + ", offset: " + defIndex);
            }
        }
    }

    private int getReferenceIndex(@NotNull String fileContent) {
        return fileContent.indexOf(REF_MARK);
    }

    private int getNoReferenceIndex(@NotNull String fileContent) {
        return fileContent.indexOf(NO_REF_MARK);
    }

    private int getDefinitionIndex(@NotNull String fileContent) {
        return fileContent.indexOf(DEF_MARK);
    }

    @NotNull
    protected static String loadText(@NotNull VirtualFile file) {
        try {
            return StringUtil.convertLineSeparators(VfsUtilCore.loadText(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private PsiReference findReference(@NotNull PsiFile file, int offset) {
        if (myReference != null) {
            fail("only one reference should be declared in a test case, see file: " + file.getName());
        }
        PsiReference result = file.findReferenceAt(offset);
        if (result == null) {
            fail("no reference was found at mark in file: " + file.getName() + ", offset: " + offset);
        }
        return result;
    }
}
