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
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.util.PsiTreeUtil;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Collection;
//
//public class PackageUnitReference extends BallerinaElementReference {
//
//    public PackageUnitReference(@NotNull IdentifierPSINode element) {
//        super(element);
//    }
//
//    @Override
//    public boolean isDefinitionNode(PsiElement element) {
//        return element instanceof PackageUnitNode;
//    }
//
//    @NotNull
//    @Override
//    public Object[] getVariants() {
////        PackageUnitNode rules = PsiTreeUtil.getContextOfType(myElement, PackageUnitNode.class);
////        // find all rule defs (token, parser)
////        Collection<? extends RuleSpecNode> ruleSpecNodes =
////                PsiTreeUtil.findChildrenOfAnyType(rules,
////                        new Class[] {
////                                ParserRuleSpecNode.class,
////                                LexerRuleSpecNode.class}
////                );
////
////        return ruleSpecNodes.toArray();
//        return new Object[0];
//    }
//}
