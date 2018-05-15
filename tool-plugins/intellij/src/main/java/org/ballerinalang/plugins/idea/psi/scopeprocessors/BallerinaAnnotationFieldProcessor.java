package org.ballerinalang.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.inserthandlers.ColonInsertHandler;
import org.ballerinalang.plugins.idea.psi.BallerinaAnnotationAttachment;
import org.ballerinalang.plugins.idea.psi.BallerinaAnnotationDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaExpression;
import org.ballerinalang.plugins.idea.psi.BallerinaFieldDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaNameReference;
import org.ballerinalang.plugins.idea.psi.BallerinaRecordKey;
import org.ballerinalang.plugins.idea.psi.BallerinaRecordKeyValue;
import org.ballerinalang.plugins.idea.psi.BallerinaRecordLiteralExpression;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaUserDefineTypeName;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Responsible for resolving and completing annotation fields.
 */
public class BallerinaAnnotationFieldProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaAnnotationFieldProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                             boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaAnnotationAttachment;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        if (accept(element)) {
            BallerinaRecordLiteralExpression recordLiteralExpression = PsiTreeUtil.getParentOfType(myElement,
                    BallerinaRecordLiteralExpression.class);
            PsiElement ownerName;
            BallerinaTypeDefinition typeDefinition;
            if (recordLiteralExpression != null) {
                BallerinaRecordKeyValue recordKeyValue = PsiTreeUtil.getParentOfType(recordLiteralExpression,
                        BallerinaRecordKeyValue.class);
                if (recordKeyValue == null) {
                    return true;
                } else {
                    BallerinaRecordKey recordKey = recordKeyValue.getRecordKey();
                    BallerinaExpression expression = recordKey.getExpression();
                    if (expression == null) {
                        return true;
                    }
                    PsiReference reference = expression.findReferenceAt(expression.getTextLength());
                    if (reference == null) {
                        return true;
                    }
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement == null || !(resolvedElement.getParent() instanceof BallerinaFieldDefinition)) {
                        return true;
                    }
                    BallerinaFieldDefinition fieldDefinition = (BallerinaFieldDefinition) resolvedElement.getParent();
                    PsiElement type = BallerinaPsiImplUtil.getTypeNameFromField(fieldDefinition);
                    if (type == null) {
                        return true;
                    }
                    if (type.getParent() instanceof BallerinaTypeDefinition) {
                        ownerName = type;
                        typeDefinition = (BallerinaTypeDefinition) type.getParent();
                    } else {
                        PsiReference typeReference = type.findReferenceAt(type.getTextLength());
                        if (typeReference == null) {
                            return true;
                        }
                        ownerName = typeReference.resolve();
                        if (ownerName == null || !(ownerName.getParent() instanceof BallerinaTypeDefinition)) {
                            return true;
                        }
                        typeDefinition = (BallerinaTypeDefinition) ownerName.getParent();
                    }
                }
            } else {
                BallerinaNameReference nameReference = ((BallerinaAnnotationAttachment) element).getNameReference();
                PsiReference reference = nameReference.getIdentifier().getReference();
                if (reference == null) {
                    return true;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null || !(resolvedElement.getParent() instanceof
                        BallerinaAnnotationDefinition)) {
                    return true;
                }
                BallerinaUserDefineTypeName userDefineTypeName = PsiTreeUtil.getNextSiblingOfType(resolvedElement,
                        BallerinaUserDefineTypeName.class);
                if (userDefineTypeName == null) {
                    return true;
                }
                PsiElement identifier = userDefineTypeName.getNameReference().getIdentifier();
                reference = identifier.getReference();
                if (reference == null) {
                    return true;
                }
                ownerName = reference.resolve();
                if (ownerName == null || !(ownerName.getParent() instanceof BallerinaTypeDefinition)) {
                    return true;
                }
                typeDefinition = (BallerinaTypeDefinition) ownerName.getParent();
            }

            Collection<BallerinaFieldDefinition> fieldDefinitions = PsiTreeUtil.findChildrenOfType(typeDefinition,
                    BallerinaFieldDefinition.class);
            for (BallerinaFieldDefinition fieldDefinition : fieldDefinitions) {
                PsiElement definitionIdentifier = fieldDefinition.getIdentifier();
                String typeName = "Type";
                PsiElement typeNameFromField = BallerinaPsiImplUtil.getTypeNameFromField(fieldDefinition);
                if (typeNameFromField != null) {
                    typeName = typeNameFromField.getText();
                }
                if (myResult != null) {
                    // Todo - Conside oncommit, onabort, etc and set the insert handler
                    // Note - Child is passed here instead of identifier because it is is top level
                    // definition.
                    myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(definitionIdentifier,
                            ownerName, typeName, null, ColonInsertHandler.INSTANCE_WITH_AUTO_POPUP, true));
                } else if (myElement.getText().equals(definitionIdentifier.getText())) {
                    add(definitionIdentifier);
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCompletion() {
        return myIsCompletion;
    }

    @Override
    protected boolean crossOff(@NotNull PsiElement e) {
        return false;
    }
}
