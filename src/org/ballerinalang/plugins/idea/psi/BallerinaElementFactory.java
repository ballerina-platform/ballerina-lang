///*
// *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.ballerinalang.plugins.idea.psi;
//
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiFileFactory;
//import org.ballerinalang.plugins.idea.BallerinaFileType;
//
//public class BallerinaElementFactory {
//
//    private static String sampleFunction = "\nfunction test(){return 0;}";
//
//    public static BallerinaPackageDeclaration createPackageDeclaration(Project project, String packageName) {
//        final BallerinaFile file = createFile(project, "package " + packageName + ";" + sampleFunction);
//        return (BallerinaPackageDeclaration) file.getFirstChild();
//    }
//
//    private static BallerinaFile createFile(Project project, String text) {
//        String name = "dummy.bal";
//        return (BallerinaFile) PsiFileFactory.getInstance(project)
//                .createFileFromText(name, BallerinaFileType.INSTANCE, text);
//    }
//}
