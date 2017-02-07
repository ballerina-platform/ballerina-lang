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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.CollectionPattern;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.PsiFilePattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.util.ProcessingContext;
import org.antlr.jetbrains.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.jetbrains.adaptor.lexer.TokenIElementType;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.grammar.BallerinaLexer;
import org.ballerinalang.plugins.idea.grammar.BallerinaParser;
import org.ballerinalang.plugins.idea.psi.AnnotationNameNode;
import org.ballerinalang.plugins.idea.psi.AnnotationNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.FunctionBodyNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;
import static com.intellij.patterns.StandardPatterns.collection;
import static com.intellij.patterns.StandardPatterns.not;
import static com.intellij.patterns.StandardPatterns.or;

public class KeywordCompletionContributor extends CompletionContributor implements DumbAware {

    public static final int KEYWORD_PRIORITY = 20;

    static List<TokenIElementType> tokenIElementTypes =
            PSIElementTypeFactory.getTokenIElementTypes(BallerinaLanguage.INSTANCE);

    public KeywordCompletionContributor() {

        //        extend(CompletionType.BASIC,
        //                psiElement(tokenIElementTypes.get(BallerinaLexer.Identifier)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("test0"));
        //                    }
        //                }
        //        );

        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(PsiErrorElement.class).withParent(goFileWithoutPackage()))
        //                        .isFirstAcceptedChild(psiElement()),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("test1"));
        //                    }
        //                }
        //        );

//        extend(CompletionType.BASIC,
//                psiElement(),
//                new CompletionProvider<CompletionParameters>() {
//                    public void addCompletions(@NotNull CompletionParameters parameters,
//                                               ProcessingContext context,
//                                               @NotNull CompletionResultSet resultSet) {
//                        resultSet.addElement(LookupElementBuilder.create(""));
//                    }
//                }
//        );
//
//        extend(CompletionType.BASIC,
//                psiElement().inside(false, psiElement(FunctionDefinitionNode.class)),
//                new CompletionProvider<CompletionParameters>() {
//                    public void addCompletions(@NotNull CompletionParameters parameters,
//                                               ProcessingContext context,
//                                               @NotNull CompletionResultSet resultSet) {
//                        resultSet.addElement(LookupElementBuilder.create("test1"));
//                    }
//                }
//        );
//
//        extend(CompletionType.BASIC,
//                psiElement().withParent(psiElement(PsiErrorElement.class)),
//                new CompletionProvider<CompletionParameters>() {
//                    public void addCompletions(@NotNull CompletionParameters parameters,
//                                               ProcessingContext context,
//                                               @NotNull CompletionResultSet resultSet) {
//                        resultSet.addElement(LookupElementBuilder.create("test2"));
//                    }
//                }
//        );
//
//        extend(CompletionType.BASIC,
//                psiElement().withSuperParent(2, psiElement(FunctionBodyNode.class)),
//                new CompletionProvider<CompletionParameters>() {
//                    public void addCompletions(@NotNull CompletionParameters parameters,
//                                               ProcessingContext context,
//                                               @NotNull CompletionResultSet resultSet) {
//                        resultSet.addElement(LookupElementBuilder.create("test2"));
//                    }
//                }
//        );
//
//        extend(CompletionType.BASIC,
//                psiElement().withSuperParent(3, psiElement(FunctionBodyNode.class)),
//                new CompletionProvider<CompletionParameters>() {
//                    public void addCompletions(@NotNull CompletionParameters parameters,
//                                               ProcessingContext context,
//                                               @NotNull CompletionResultSet resultSet) {
//                        resultSet.addElement(LookupElementBuilder.create("test3"));
//                    }
//                }
//        );
//
//        extend(CompletionType.BASIC,
//                psiElement().withSuperParent(4, psiElement(FunctionBodyNode.class)),
//                new CompletionProvider<CompletionParameters>() {
//                    public void addCompletions(@NotNull CompletionParameters parameters,
//                                               ProcessingContext context,
//                                               @NotNull CompletionResultSet resultSet) {
//                        resultSet.addElement(LookupElementBuilder.create("test4"));
//                    }
//                }
//        );





        //        extend(CompletionType.BASIC,
        //                psiElement().afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
        //                        psiElement(StatementNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("test2"));
        //                    }
        //                }
        //        );

        //        extend(CompletionType.BASIC,
        //                psiElement().inside(psiElement(FunctionBodyNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource1"));
        //                        resultSet.addElement(LookupElementBuilder.create("@GET1"));
        //                        resultSet.addElement(LookupElementBuilder.create("@POST1"));
        //                    }
        //                }
        //        );

        //        extend(CompletionType.BASIC,
        //                psiElement().inside(psiElement(ServiceBodyNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource1"));
        //                        resultSet.addElement(LookupElementBuilder.create("@GET1"));
        //                        resultSet.addElement(LookupElementBuilder.create("@POST1"));
        //                    }
        //                }
        //        );
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().inside(psiElement(ServiceBodyDeclarationNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource2"));
        //                    }
        //                }
        //        );
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(CompilationUnitNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource"));
        //                        resultSet.addElement(LookupElementBuilder.create("function"));
        //                    }
        //                }
        //        );
        //
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().afterSibling(psiElement(ImportDeclarationNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource2"));
        //                        resultSet.addElement(LookupElementBuilder.create("function2"));
        //                    }
        //                }
        //        );

        //        extend(CompletionType.BASIC,
        //                psiElement().afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
        //                        psiElement(ResourceDefinitionNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource2"));
        //                    }
        //                }
        //        );


        //        extend(CompletionType.BASIC,
        //                not(psiElement().inside(psiElement(FunctionBodyNode.class)))
        //                ,
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("package"));
        //                        resultSet.addElement(LookupElementBuilder.create("import"));
        //                        resultSet.addElement(LookupElementBuilder.create("resource"));
        //                        resultSet.addElement(LookupElementBuilder.create("service"));
        //                        resultSet.addElement(LookupElementBuilder.create("function"));
        //                    }
        //                }
        //        );


        //        extend(CompletionType.BASIC,
        //                psiElement().afterLeaf("@").withParent(psiElement(ResourceDefinitionNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("GET"));
        //                        resultSet.addElement(LookupElementBuilder.create("POST"));
        //                    }
        //                }
        //        );
        //
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(ServiceBodyNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource1"));
        //                        resultSet.addElement(LookupElementBuilder.create("@GET1"));
        //                        resultSet.addElement(LookupElementBuilder.create("@POST1"));
        //                    }
        //                }
        //        );
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(ServiceBodyDeclarationNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource2"));
        //                        resultSet.addElement(LookupElementBuilder.create("@GET2"));
        //                        resultSet.addElement(LookupElementBuilder.create("@POST2"));
        //                    }
        //                }
        //        );
        //
        //        //        extend(CompletionType.BASIC,
        //        //                psiElement().withParent(psiElement(ServiceBodyDeclarationNode.class)),
        //        //                new CompletionProvider<CompletionParameters>() {
        //        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //        //                                               ProcessingContext context,
        //        //                                               @NotNull CompletionResultSet resultSet) {
        //        //                        resultSet.addElement(LookupElementBuilder.create("resource"));
        //        //                    }
        //        //                }
        //        //        );
        //
        //
        //        //
        //        //        extend(CompletionType.BASIC,
        //        //                psiElement().withSuperParent(3,psiElement(ANTLRPsiNode.class)),
        //        //                new CompletionProvider<CompletionParameters>() {
        //        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //        //                                               ProcessingContext context,
        //        //                                               @NotNull CompletionResultSet resultSet) {
        //        //                        resultSet.addElement(LookupElementBuilder.create("res"));
        //        //                    }
        //        //                }
        //        //        );
        //
        //
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(AnnotationNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("@anno"));
        //                        resultSet.addElement(LookupElementBuilder.create("@POST"));
        //                        resultSet.addElement(LookupElementBuilder.create("anno"));
        //                    }
        //                }
        //        );
        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(AnnotationNameNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("@annoname"));
        //                        resultSet.addElement(LookupElementBuilder.create("@POST"));
        //                        resultSet.addElement(LookupElementBuilder.create("annoname"));
        //                    }
        //                }
        //        );
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().afterSibling(psiElement(AnnotationNameNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("resource3"));
        //                    }
        //                }
        //        );
        //
        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(PsiErrorElement.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("err"));
        //                    }
        //                }
        //        );


        //        extend(CompletionType.BASIC,
        //                psiElement().withParent(psiElement(ServiceBodyNode.class)),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("@serbody"));
        //                        resultSet.addElement(LookupElementBuilder.create("@POST"));
        //                        resultSet.addElement(LookupElementBuilder.create("serbody"));
        //                    }
        //                }
        //        );
    }

//        @Override
//        public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
//            return super.invokeAutoPopup(position, ':');
//        }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);
    }

    private static PsiElementPattern.Capture<PsiElement> packagePattern() {
        return
                //                psiElement(BallerinaParserDefinition.ID)
                psiElement()
                //                        .withParent(psiElement(PsiErrorElement.class))
                //                        .withParent(psiElement(CompilationUnitNode.class))
                //                .withParent(psiElement(ANTLRPsiNode.class))
                //                .withParent(psiElement(ANTLRPsiNode.class))
                //                .withParent(psiFile(BallerinaFile.class))
                //                .withLanguage(BallerinaLanguage.INSTANCE)

                ;
    }

    private static PsiFilePattern.Capture<BallerinaFile> goFileWithoutPackage() {


        CollectionPattern<PsiElement> collection = collection(PsiElement.class);
        ElementPattern<Collection<PsiElement>> emptyOrPackageIsNotFirst = or(collection.empty(),
                collection.first(not(psiElement(PackageDeclarationNode.class))));
        return psiFile(BallerinaFile.class).withChildren(collection.filter(not(psiElement()
                .whitespaceCommentEmptyOrError()), emptyOrPackageIsNotFirst));
    }
}
