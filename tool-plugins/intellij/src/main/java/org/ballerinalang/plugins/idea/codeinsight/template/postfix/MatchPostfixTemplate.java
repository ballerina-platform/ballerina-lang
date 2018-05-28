package org.ballerinalang.plugins.idea.codeinsight.template.postfix;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.VariableNode;
import com.intellij.codeInsight.template.macro.IterableComponentTypeMacro;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
import com.intellij.codeInsight.template.postfix.templates.StringBasedPostfixTemplate;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaIdentifier;
import org.ballerinalang.plugins.idea.psi.BallerinaNullableTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaPackageReference;
import org.ballerinalang.plugins.idea.psi.BallerinaSimpleTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaUnionTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaUserDefineTypeName;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.ballerinalang.plugins.idea.codeinsight.template.postfix.BallerinaPostfixUtils.IS_ITERABLE_OR_ARRAY;
import static org.ballerinalang.plugins.idea.codeinsight.template.postfix.BallerinaPostfixUtils.getType;
import static org.ballerinalang.plugins.idea.codeinsight.template.postfix.BallerinaPostfixUtils.selectorTopmost;

/**
 * Match postfix template.
 */
public class MatchPostfixTemplate extends StringBasedPostfixTemplate {

    public MatchPostfixTemplate(String name) {
        super(name, "match expr { }", selectorTopmost(IS_ITERABLE_OR_ARRAY));
    }

    @Override
    public void setVariables(@NotNull Template template, @NotNull PsiElement element) {
        MacroCallNode node = new MacroCallNode(new IterableComponentTypeMacro());
        MacroCallNode name = new MacroCallNode(new SuggestVariableNameMacro());
        node.addParameter(new VariableNode("expr", null));
        // Get the element type.
        PsiElement type = getType(element);
        // If the type is null and the element is an identifier, we need to get the type using the identifier.
        if (type == null && element instanceof BallerinaIdentifier) {
            type = BallerinaPsiImplUtil.getType((BallerinaIdentifier) element);
        }
        if (type != null) {
            // If the element type is either union type or nullable type, we can expand the template.
            if (type instanceof BallerinaUnionTypeName) {
                // Get type list from the union type.
                BallerinaUnionTypeName ballerinaUnionTypeName = (BallerinaUnionTypeName) type;
                List<BallerinaTypeName> typeNameList = ballerinaUnionTypeName.getTypeNameList();
                // Add new variable for each type. This is used to enter the variable name for the type in the match
                // pattern.
                for (int i = 0; i < typeNameList.size(); i++) {
                    template.addVariable("name" + i, name, name, true);
                    template.addVariable("value" + i, name, name, true);
                }
            } else if (type instanceof BallerinaNullableTypeName) {
                // Get the sub-type name from the nullable type.
                BallerinaNullableTypeName ballerinaNullableTypeName = (BallerinaNullableTypeName) type;
                BallerinaTypeName typeName = ballerinaNullableTypeName.getTypeName();
                // If it is a simple type name, it means we have only one type. But we need to complete the match
                // pattern for null as well. So we add two variables.
                if (typeName instanceof BallerinaSimpleTypeName) {
                    // Add extra variable for nullable pattern.
                    for (int i = 0; i < 2; i++) {
                        template.addVariable("name" + i, name, name, true);
                        template.addVariable("value" + i, name, name, true);
                    }
                } else if (typeName instanceof BallerinaUnionTypeName) {
                    // Get type list from the union type.
                    List<BallerinaTypeName> typeNameList = ((BallerinaUnionTypeName) typeName).getTypeNameList();
                    int size = typeNameList.size();
                    // Add new variable for each type.
                    for (int i = 0; i < size; i++) {
                        template.addVariable("name" + i, name, name, true);
                        template.addVariable("value" + i, name, name, true);
                    }
                    // Add extra variable for nullable pattern.
                    template.addVariable("name" + size, name, name, true);
                    template.addVariable("value" + size, name, name, true);
                }
            }
        }
    }

    @Override
    public String getTemplateString(@NotNull PsiElement element) {
        StringBuilder template = new StringBuilder("match $expr$ {\n");
        PsiElement type = getType(element);
        // If the type is null and the element is an identifier, we need to get the type using the identifier.
        if (type == null && element instanceof BallerinaIdentifier) {
            type = BallerinaPsiImplUtil.getType((BallerinaIdentifier) element);
        }
        if (type != null) {
            if (type instanceof BallerinaUnionTypeName) {
                BallerinaUnionTypeName ballerinaUnionTypeName = (BallerinaUnionTypeName) type;
                List<BallerinaTypeName> typeNameList = ballerinaUnionTypeName.getTypeNameList();
                for (int i = 0; i < typeNameList.size(); i++) {
                    BallerinaTypeName ballerinaTypeName = typeNameList.get(i);

                    // Get the suggested package for the type if any. Otherwise it will be an empty string.
                    String packageName = getPackageName(element, ballerinaTypeName);

                    template.append("    ").append(packageName).append(ballerinaTypeName.getText()).append(" $name")
                            .append(i).append("$ => {\n").append("$value").append(i).append("$").append("\n}");
                    if (i != typeNameList.size() - 1) {
                        template.append("\n");
                    }
                }
            } else if (type instanceof BallerinaNullableTypeName) {
                BallerinaNullableTypeName ballerinaNullableTypeName = (BallerinaNullableTypeName) type;
                BallerinaTypeName typeName = ballerinaNullableTypeName.getTypeName();
                // Todo - Consider package as well.
                if (typeName instanceof BallerinaSimpleTypeName) {

                    // Get the suggested package for the type if any. Otherwise it will be an empty string.
                    String packageName = getPackageName(element, typeName);

                    template.append("    ").append(packageName).append(typeName.getText()).append(" $name").append(0)
                            .append("$ => {\n").append("$value").append(0).append("$").append("\n}").append("\n")
                            .append("    ").append("()").append(" $name").append(1)
                            .append("$ => {\n").append("$value").append(1).append("$").append("\n}");
                } else if (typeName instanceof BallerinaUnionTypeName) {
                    BallerinaUnionTypeName ballerinaUnionTypeName = (BallerinaUnionTypeName) typeName;
                    List<BallerinaTypeName> typeNameList = ballerinaUnionTypeName.getTypeNameList();
                    int size = typeNameList.size();
                    for (int i = 0; i < size; i++) {
                        BallerinaTypeName ballerinaTypeName = typeNameList.get(i);
                        // Get the suggested package for the type if any. Otherwise it will be an empty string.
                        String packageName = getPackageName(element, ballerinaTypeName);
                        template.append("    ").append(packageName).append(ballerinaTypeName.getText())
                                .append(" $name").append(i).append("$ => {\n").append("$value").append(i)
                                .append("$").append("\n}").append("\n");
                    }
                    template.append("    ").append("()").append(" $name").append(size)
                            .append("$ => {\n").append("$value").append(size).append("$").append("\n}");
                }
            }
        } else {
            template.append("$END$");
        }
        return template.append("    \n}\n$END$").toString();
    }

    private String getPackageName(@NotNull PsiElement element, @NotNull PsiElement ballerinaTypeName) {
        // Check whether type already contains a package name. In that case we don't need to suggest a package name.
        BallerinaPackageReference ballerinaPackageReference = PsiTreeUtil.findChildOfType(ballerinaTypeName,
                BallerinaPackageReference.class);
        if (ballerinaPackageReference != null) {
            return "";
        }
        // Get the built-in type definitions. We don't need to suggest package name for these types.
        List<BallerinaTypeDefinition> ballerinaTypeDefinitions =
                BallerinaPsiImplUtil.suggestBuiltInTypes(ballerinaTypeName);
        for (BallerinaTypeDefinition ballerinaTypeDefinition : ballerinaTypeDefinitions) {
            PsiElement identifier = ballerinaTypeDefinition.getIdentifier();
            if (identifier == null) {
                continue;
            }
            if (identifier.getText().equals(ballerinaTypeName.getText())) {
                return "";
            }
        }

        // We only need to suggest package names for the user defined type names. Suggesting package names for other
        // type names like "string" is not correct.
        BallerinaUserDefineTypeName ballerinaUserDefineTypeName =
                PsiTreeUtil.findChildOfType(ballerinaTypeName, BallerinaUserDefineTypeName.class);
        if (ballerinaUserDefineTypeName != null) {
            // Suggest the package name for the current element.
            String packageNameForElement = BallerinaPsiImplUtil.suggestPackage(element);
            // Suggest the package name for the type. This
            String packageNameForType = BallerinaPsiImplUtil.suggestPackage(ballerinaTypeName);
            // If the suggested name for both elements are the same, that means both of them are in the same package.
            // In that case, we don't need to suggest a package.
            if (packageNameForElement != null && packageNameForType != null &&
                    !packageNameForElement.equals(packageNameForType)) {
                // Return the package name with ":" appended. This string will be directly added before the type.
                // That is why we append ":".
                return packageNameForType + ":";
            }
        }
        return "";
    }
}
