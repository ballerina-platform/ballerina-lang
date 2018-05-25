package org.ballerinalang.plugins.idea.codeinsight.template.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelector;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaExpression;
import org.ballerinalang.plugins.idea.psi.BallerinaExpressionStmt;
import org.ballerinalang.plugins.idea.psi.BallerinaIdentifier;
import org.ballerinalang.plugins.idea.psi.BallerinaNullableTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaUnionTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaVariableReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.openapi.util.Conditions.and;

/**
 * Util methods for postfix completion.
 */
public class BallerinaPostfixUtils {

    public static PostfixTemplateExpressionSelector selectorTopmost(Condition<PsiElement> additionalFilter) {

        return new PostfixTemplateExpressionSelectorBase(additionalFilter) {
            @Override
            protected List<PsiElement> getNonFilteredExpressions(@NotNull PsiElement context,
                                                                 @NotNull Document document, int offset) {
                return ContainerUtil.createMaybeSingletonList(getTopmostExpression(context));
            }

            @Override
            protected Condition<PsiElement> getFilters(int offset) {
                return and(super.getFilters(offset), getPsiErrorFilter());
            }

            @NotNull
            @Override
            public Function<PsiElement, String> getRenderer() {
                return BallerinaPostfixUtils.getRenderer();
            }
        };
    }

    @NotNull
    public static Function<PsiElement, String> getRenderer() {
        return element -> new RenderFunction().fun((BallerinaExpression) element);
    }

    public static final Condition<PsiElement> IS_ITERABLE_OR_ARRAY = element -> {
        // The topmost expression can be an identifier in some cases. See the comment in getTopmostExpression method.
        if (element instanceof BallerinaIdentifier) {
            return true;
        }
        PsiElement type = getType(element);
        return type instanceof BallerinaUnionTypeName || type instanceof BallerinaNullableTypeName;
    };

    @Nullable
    public static PsiElement getType(PsiElement context) {
        return CachedValuesManager.getCachedValue(context, () -> {
            if (!(context instanceof BallerinaVariableReferenceExpression)) {
                return CachedValueProvider.Result.create(null, context);
            }
            return CachedValueProvider.Result.create(
                    ((BallerinaVariableReferenceExpression) context).getVariableReference().getType(), context);
        });
    }

    @Nullable
    public static PsiElement getTopmostExpression(PsiElement context) {
        BallerinaExpressionStmt statement = PsiTreeUtil.getNonStrictParentOfType(context,
                BallerinaExpressionStmt.class);
        if (statement != null) {
            return statement.getExpression();
        }
        // Sometimes while the typing, the code can be interpreted as a variable definition statement if there are
        // content after the caret. In that case, we return the context as the topmost expression.
        BallerinaTypeName ballerinaTypeName = PsiTreeUtil.getNonStrictParentOfType(context, BallerinaTypeName.class);
        if (ballerinaTypeName != null) {
            return context;
        }
        return null;
    }

    /**
     * Render helper class.
     */
    public static class RenderFunction implements Function<BallerinaExpression, String> {
        @Override
        public String fun(BallerinaExpression psiExpression) {
            return render(psiExpression);
        }
    }

    public static String render(BallerinaExpression expression) {
        return render(expression, 100);
    }

    public static String render(BallerinaExpression expression, int maxLength) {
        return expression.getText();
    }
}
