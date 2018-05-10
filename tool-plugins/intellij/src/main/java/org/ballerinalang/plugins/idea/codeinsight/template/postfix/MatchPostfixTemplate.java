package org.ballerinalang.plugins.idea.codeinsight.template.postfix;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.VariableNode;
import com.intellij.codeInsight.template.macro.IterableComponentTypeMacro;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
import com.intellij.codeInsight.template.postfix.templates.StringBasedPostfixTemplate;
import com.intellij.psi.PsiElement;
import org.ballerinalang.plugins.idea.psi.BallerinaNullableTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaSimpleTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaUnionTypeName;
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
        //        template.addVariable("type", type, type, false);
        PsiElement type = getType(element);
        if (type != null) {
            if (type instanceof BallerinaUnionTypeName) {
                BallerinaUnionTypeName ballerinaUnionTypeName = (BallerinaUnionTypeName) type;
                List<BallerinaTypeName> typeNameList = ballerinaUnionTypeName.getTypeNameList();
                for (int i = 0; i < typeNameList.size(); i++) {
                    template.addVariable("name" + i, name, name, true);
                    template.addVariable("value" + i, name, name, true);
                }
            } else if (type instanceof BallerinaNullableTypeName) {
                BallerinaNullableTypeName ballerinaNullableTypeName = (BallerinaNullableTypeName) type;
                BallerinaTypeName typeName = ballerinaNullableTypeName.getTypeName();

                if (typeName instanceof BallerinaSimpleTypeName) {
                    // Add extra variable for nullable pattern.
                    for (int i = 0; i < 2; i++) {
                        template.addVariable("name" + i, name, name, true);
                        template.addVariable("value" + i, name, name, true);
                    }
                } else if (typeName instanceof BallerinaUnionTypeName) {
                    List<BallerinaTypeName> typeNameList = ((BallerinaUnionTypeName) typeName).getTypeNameList();
                    int size = typeNameList.size();
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
        if (type != null) {
            if (type instanceof BallerinaUnionTypeName) {
                // Todo - Consider package as well.
                BallerinaUnionTypeName ballerinaUnionTypeName = (BallerinaUnionTypeName) type;
                List<BallerinaTypeName> typeNameList = ballerinaUnionTypeName.getTypeNameList();
                for (int i = 0; i < typeNameList.size(); i++) {
                    template.append("    ").append(typeNameList.get(i).getText()).append(" $name").append(i)
                            .append("$ => {\n").append("$value").append(i).append("$").append("\n}");
                    if (i != typeNameList.size() - 1) {
                        template.append("\n");
                    }
                }
            } else if (type instanceof BallerinaNullableTypeName) {
                BallerinaNullableTypeName ballerinaNullableTypeName = (BallerinaNullableTypeName) type;
                BallerinaTypeName typeName = ballerinaNullableTypeName.getTypeName();
                // Todo - Consider package as well.
                if (typeName instanceof BallerinaSimpleTypeName) {
                    template.append("    ").append(typeName.getText()).append(" $name").append(0)
                            .append("$ => {\n").append("$value").append(0).append("$").append("\n}").append("\n")
                            .append("    ").append("()").append(" $name").append(1)
                            .append("$ => {\n").append("$value").append(1).append("$").append("\n}");
                } else if (typeName instanceof BallerinaUnionTypeName) {
                    BallerinaUnionTypeName ballerinaUnionTypeName = (BallerinaUnionTypeName) typeName;
                    List<BallerinaTypeName> typeNameList = ballerinaUnionTypeName.getTypeNameList();
                    int size = typeNameList.size();
                    for (int i = 0; i < size; i++) {
                        template.append("    ").append(typeNameList.get(i).getText()).append(" $name").append(i)
                                .append("$ => {\n").append("$value").append(i).append("$").append("\n}").append("\n");
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
}
