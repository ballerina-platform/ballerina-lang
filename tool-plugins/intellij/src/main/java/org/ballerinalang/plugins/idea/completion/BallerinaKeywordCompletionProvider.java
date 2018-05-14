package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.ballerinalang.plugins.idea.psi.BallerinaCompositeElement;
import org.ballerinalang.plugins.idea.psi.BallerinaDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaExpression;
import org.ballerinalang.plugins.idea.psi.BallerinaFiniteType;
import org.ballerinalang.plugins.idea.psi.BallerinaForeachStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaGlobalVariableDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaResourceDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaSimpleVariableReference;
import org.ballerinalang.plugins.idea.psi.BallerinaStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaTransactionStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.BallerinaUserDefineTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaVariableDefinitionStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaVariableReferenceList;
import org.ballerinalang.plugins.idea.psi.BallerinaWhileStatement;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Keyword completion provider.
 */
public class BallerinaKeywordCompletionProvider extends CompletionProvider<CompletionParameters> {

    BallerinaKeywordCompletionProvider() {

    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        PsiElement parent = position.getParent();

        BallerinaVariableReferenceList referenceList = PsiTreeUtil.getParentOfType(position,
                BallerinaVariableReferenceList.class);
        BallerinaForeachStatement foreachStatement = PsiTreeUtil.getParentOfType(referenceList,
                BallerinaForeachStatement.class, true);
        if (foreachStatement != null) {
            result.stopHere();
            return;
        }


        BallerinaTransactionStatement transactionStatement = PsiTreeUtil.getParentOfType(position,
                BallerinaTransactionStatement.class);
        if (transactionStatement != null) {
            BallerinaCompletionUtils.addTransactionKeywordsAsLookups(result);
        }

        BallerinaCompositeElement loopTypes = PsiTreeUtil.getParentOfType(position,
                BallerinaWhileStatement.class, BallerinaForeachStatement.class);
        if (loopTypes != null) {
            BallerinaCompletionUtils.addLoopKeywordsAsLookups(result);
        }

        // Todo - Add bind
        //        if (parent instanceof BallerinaServiceEndpointAttachments) {
        //            if (parent.getFirstChild().equals(position)) {
        //                BallerinaCompletionUtils.addBindAsLookup(result);
        //                return;
        //            }
        //        }

        BallerinaExpression expression = PsiTreeUtil.getParentOfType(position, BallerinaExpression.class);
        if (expression != null) {
            BallerinaSimpleVariableReference simpleVariableReference = PsiTreeUtil.getParentOfType(position,
                    BallerinaSimpleVariableReference.class);
            if (simpleVariableReference != null) {
                PsiElement tempParent = parent;
                while (tempParent != null) {
                    PsiElement superParent = tempParent.getParent();
                    if (!superParent.getFirstChild().equals(tempParent)) {
                        break;
                    }
                    tempParent = superParent;
                }

                // Todo - 'but' keyword in matching
                if (tempParent != null && tempParent.equals(expression)) {
                    PsiElement superParent = tempParent.getParent();
                    if (BallerinaPsiImplUtil.isObjectInitializer(superParent)) {
                        PsiElement type =
                                BallerinaPsiImplUtil.getType((BallerinaVariableDefinitionStatement) superParent);
                        if (type != null) {
                            BallerinaCompletionUtils.addNewAsLookup(result, (BallerinaTypeDefinition) type.getParent());
                            return;
                        }
                    }
                    BallerinaCompletionUtils.addExpressionKeywordsAsLookups(result);
                    return;
                }
            }
        }

        BallerinaStatement statement = PsiTreeUtil.getParentOfType(position, BallerinaStatement.class);
        if (statement != null) {
            BallerinaSimpleVariableReference simpleVariableReference = PsiTreeUtil.getParentOfType(position,
                    BallerinaSimpleVariableReference.class);
            if (simpleVariableReference != null) {
                PsiElement tempParent = parent;
                while (tempParent != null && !tempParent.equals(statement)) {
                    PsiElement superParent = tempParent.getParent();
                    if (!superParent.getFirstChild().equals(tempParent)) {
                        break;
                    }
                    tempParent = superParent;
                }
                if (tempParent != null && tempParent.equals(statement)) {
                    PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(position);
                    if (!(prevVisibleLeaf instanceof LeafPsiElement
                            && (((LeafPsiElement) prevVisibleLeaf).getElementType() == BallerinaTypes.RARROW
                            || ((LeafPsiElement) prevVisibleLeaf).getElementType() ==
                            BallerinaTypes.DECIMAL_INTEGER_LITERAL
                            || ((LeafPsiElement) prevVisibleLeaf).getElementType() == BallerinaTypes.COLON))) {
                        BallerinaCompletionUtils.addValueTypesAsLookups(result);
                        BallerinaCompletionUtils.addReferenceTypesAsLookups(result);
                        BallerinaCompletionUtils.addVarAsLookup(result);
                        BallerinaCompletionUtils.addReturnAsLookup(result);
                        BallerinaCompletionUtils.addLockAsLookup(result);
                        BallerinaCompletionUtils.addCommonKeywords(result);
                        return;
                    }
                }
            }
        }

        BallerinaGlobalVariableDefinition globalVariableDefinition = PsiTreeUtil.getParentOfType(position,
                BallerinaGlobalVariableDefinition.class);
        if (globalVariableDefinition != null) {

            // Todo - Consider situations where import statement is added after.
            BallerinaUserDefineTypeName userDefineTypeName = PsiTreeUtil.getParentOfType(position,
                    BallerinaUserDefineTypeName.class);
            if (userDefineTypeName != null) {
                PsiElement tempParent = parent;
                while (tempParent != null) {
                    PsiElement superParent = tempParent.getParent();
                    if (!superParent.getFirstChild().equals(tempParent)) {
                        break;
                    }
                    tempParent = superParent;
                    if (superParent.equals(globalVariableDefinition)) {
                        break;
                    }
                }

                if (tempParent != null && tempParent.equals(globalVariableDefinition)) {
                    PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(position);
                    if (!(prevVisibleLeaf instanceof LeafPsiElement
                            && (((LeafPsiElement) prevVisibleLeaf).getElementType() ==
                            BallerinaTypes.DECIMAL_INTEGER_LITERAL))) {
                        BallerinaCompletionUtils.addValueTypesAsLookups(result);
                        BallerinaCompletionUtils.addReferenceTypesAsLookups(result);
                        BallerinaCompletionUtils.addTopLevelDefinitionsAsLookups(result);
                    }
                }
            }

            BallerinaDefinition prevDefinition = PsiTreeUtil.getPrevSiblingOfType(globalVariableDefinition.getParent(),
                    BallerinaDefinition.class);

            if (prevDefinition == null) {
                BallerinaCompletionUtils.addPublicAsLookup(result);
                BallerinaCompletionUtils.addImportAsLookup(result);
                return;
            }
        }

        BallerinaFiniteType ballerinaFiniteType = PsiTreeUtil.getParentOfType(position, BallerinaFiniteType.class);
        if (ballerinaFiniteType != null) {
            BallerinaUserDefineTypeName userDefineTypeName = PsiTreeUtil.getParentOfType(position,
                    BallerinaUserDefineTypeName.class);
            if (userDefineTypeName != null) {
                PsiElement tempParent = parent;
                while (tempParent != null) {
                    PsiElement superParent = tempParent.getParent();
                    if (!superParent.getFirstChild().equals(tempParent)) {
                        break;
                    }
                    tempParent = superParent;
                    if (superParent.equals(ballerinaFiniteType)) {
                        break;
                    }
                }

                if (tempParent != null && tempParent.equals(ballerinaFiniteType)) {
                    BallerinaCompletionUtils.addEObjectAsLookup(result);
                    result.stopHere();
                    return;
                }
            }
        }

        if (parent instanceof BallerinaResourceDefinition) {
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(position);
            if (prevVisibleLeaf instanceof LeafPsiElement) {
                if (((LeafPsiElement) prevVisibleLeaf).getElementType() == BallerinaTypes.LEFT_PARENTHESIS) {
                    BallerinaCompletionUtils.addEndpointAsLookup(result);
                    return;
                }
            }
        }
    }
}
