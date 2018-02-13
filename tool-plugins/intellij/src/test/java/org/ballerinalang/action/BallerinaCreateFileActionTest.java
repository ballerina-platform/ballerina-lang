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

package org.ballerinalang.action;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.jetbrains.annotations.NotNull;

public class BallerinaCreateFileActionTest extends BallerinaCodeInsightFixtureTestCase {

    public void testPackageNameInARootDirectory() {
        doTest("", "");
    }

    public void testPackageNameInSingleLevelPackage() throws Exception {
        doTest("org", "org");
    }

    public void testPackageNameInMultiLevelPackage1() throws Exception {
        doTest("org.test", "org.test");
    }

    public void testPackageNameInMultiLevelPackage2() throws Exception {
        doTest("org.test.abc", "org.test.abc");
    }

    private void doTest(String directoryPath, String expectedPackage) {
        String filePath = (directoryPath.isEmpty() ? "" : directoryPath + "/") + "a.bal";
        PsiFile file = myFixture.addFileToProject(filePath, "");
        myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
        doTest(myFixture.getFile().getContainingDirectory(), expectedPackage);
    }

    private static void doTest(@NotNull PsiDirectory dir, @NotNull String expectedPackage) {
        CustomFileTemplate template = new CustomFileTemplate("testTemplate", "bal");
        String templateText = "#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != \"\")\npackage ${PACKAGE_NAME};\n\n#end";
        template.setText(templateText);

        doTemplateTest(dir, expectedPackage, template);
    }

    private static void doTemplateTest(@NotNull PsiDirectory dir, @NotNull String expectedPackage,
                                       @NotNull CustomFileTemplate template) {
        PsiFile fileFromTemplate = CreateFileFromTemplateAction.createFileFromTemplate("test",
                                                                                       template, dir, null, true);
        BallerinaFile file = (BallerinaFile) fileFromTemplate;
        assertNotNull(file);
        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(fileFromTemplate,
                                                                                    PackageDeclarationNode.class);
        // If the file is created in a content root, the package name is empty. So there will be no package
        // declaration available. Otherwise there will be a package declaration node.
        if (!expectedPackage.isEmpty()) {
            assertNotNull(packageDeclarationNode);
            FullyQualifiedPackageNameNode fullyQualifiedPackageNameNode = PsiTreeUtil.getChildOfType(
                    packageDeclarationNode, FullyQualifiedPackageNameNode.class);
            assertNotNull(fullyQualifiedPackageNameNode);
            assertEquals(expectedPackage, fullyQualifiedPackageNameNode.getText());
        } else {
            assertNull(packageDeclarationNode);
        }
        WriteCommandAction.runWriteCommandAction(dir.getProject(), file::delete);
    }
}
