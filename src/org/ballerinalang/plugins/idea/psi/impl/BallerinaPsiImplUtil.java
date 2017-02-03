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
//package org.ballerinalang.plugins.idea.psi.impl;
//
//import com.intellij.lang.ASTNode;
//import org.ballerinalang.plugins.idea.psi.BallerinaFunctionDefinition;
//import org.ballerinalang.plugins.idea.psi.BallerinaPackageDeclaration;
//import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
//
//public class BallerinaPsiImplUtil {
//
//    public static String getRealPackageName(BallerinaPackageDeclaration element) {
//        ASTNode keyNode = element.getNode().findChildByType(BallerinaTypes.PACKAGE_NAME);
//        if (keyNode != null) {
//            // IMPORTANT: Convert embedded escaped spaces to simple spaces
//            String text = keyNode.getText().replaceAll("\\\\ ", " ");
//            int index = text.lastIndexOf(".");
//            return text.substring(index == -1 ? 0 : index + 1);
//        } else {
//            return null;
//        }
//    }
//
//    public static String getFunctionName(BallerinaFunctionDefinition element) {
//        ASTNode keyNode = element.getNode().getTreeNext();
//        if (keyNode != null) {
//            // IMPORTANT: Convert embedded escaped spaces to simple spaces
//            return keyNode.getText().replaceAll("\\\\ ", " ");
//        } else {
//            return null;
//        }
//    }
//}
