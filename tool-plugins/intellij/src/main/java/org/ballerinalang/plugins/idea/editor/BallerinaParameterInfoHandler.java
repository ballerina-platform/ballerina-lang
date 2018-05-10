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

package org.ballerinalang.plugins.idea.editor;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandlerWithTabActionSupport;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaCallableUnitSignature;
import org.ballerinalang.plugins.idea.psi.BallerinaDefaultableParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaExpressionList;
import org.ballerinalang.plugins.idea.psi.BallerinaFormalParameterList;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionInvocation;
import org.ballerinalang.plugins.idea.psi.BallerinaInvocation;
import org.ballerinalang.plugins.idea.psi.BallerinaInvocationArg;
import org.ballerinalang.plugins.idea.psi.BallerinaInvocationArgList;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectCallableUnitSignature;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectDefaultableParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectInitializerParameterList;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectParameterList;
import org.ballerinalang.plugins.idea.psi.BallerinaParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaRestParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeInitExpr;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Provides parameter info support.
 */
public class BallerinaParameterInfoHandler implements ParameterInfoHandlerWithTabActionSupport {

    @NotNull
    @Override
    public PsiElement[] getActualParameters(@NotNull PsiElement o) {
        return new PsiElement[0];
    }

    @NotNull
    @Override
    public IElementType getActualParameterDelimiterType() {
        return BallerinaTypes.COMMA;
    }

    @NotNull
    @Override
    public IElementType getActualParametersRBraceType() {
        return BallerinaTypes.RIGHT_PARENTHESIS;
    }

    @NotNull
    @Override
    public Set<Class> getArgumentListAllowedParentClasses() {
        return ContainerUtil.newHashSet();
    }

    @NotNull
    @Override
    public Set<? extends Class> getArgListStopSearchClasses() {
        return ContainerUtil.newHashSet();
    }

    @NotNull
    @Override
    public Class getArgumentListClass() {
        return BallerinaExpressionList.class;
    }

    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(Object p, ParameterInfoContext context) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    @Override
    public Object findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        return findElement(context);
    }

    @Nullable
    private Object findElement(@NotNull ParameterInfoContext context) {
        PsiElement element = context.getFile().findElementAt(context.getOffset());
        if (element == null) {
            return null;
        }
        return findElement(element);
    }

    @Nullable
    public static Object findElement(@NotNull PsiElement element) {
        // Return the element in the same file. Otherwise the parameter info will not be shown properly.
        BallerinaInvocationArgList ballerinaInvocationArgList = PsiTreeUtil.getParentOfType(element,
                BallerinaInvocationArgList.class);

        // Check for function invocation.
        BallerinaFunctionInvocation functionInvocation;
        if (element instanceof LeafPsiElement) {
            functionInvocation = PsiTreeUtil.getParentOfType(element, BallerinaFunctionInvocation.class);
        } else {
            functionInvocation = PsiTreeUtil.getParentOfType(ballerinaInvocationArgList,
                    BallerinaFunctionInvocation.class);
        }
        if (functionInvocation != null) {
            return functionInvocation;
        }

        // Check for type init expression.
        BallerinaTypeInitExpr ballerinaTypeInitExpr;
        if (element instanceof LeafPsiElement) {
            ballerinaTypeInitExpr = PsiTreeUtil.getParentOfType(element, BallerinaTypeInitExpr.class);
        } else {
            ballerinaTypeInitExpr = PsiTreeUtil.getParentOfType(ballerinaInvocationArgList,
                    BallerinaTypeInitExpr.class);
        }
        if (ballerinaTypeInitExpr != null) {
            return ballerinaTypeInitExpr;
        }

        // Check for invocations.
        BallerinaInvocation ballerinaInvocation;
        if (element instanceof LeafPsiElement) {
            ballerinaInvocation = PsiTreeUtil.getParentOfType(element, BallerinaInvocation.class);
        } else {
            ballerinaInvocation = PsiTreeUtil.getParentOfType(ballerinaInvocationArgList,
                    BallerinaInvocation.class);
        }
        if (ballerinaInvocation != null) {
            return ballerinaInvocation;
        }
        return null;
    }

    @Override
    public void showParameterInfo(@NotNull Object element, @NotNull CreateParameterInfoContext context) {
        if (element instanceof BallerinaFunctionInvocation) {
            BallerinaFunctionInvocation functionInvocation = (BallerinaFunctionInvocation) element;
            PsiElement signature =
                    BallerinaPsiImplUtil.getCallableUnitSignature(functionInvocation);
            if (signature != null) {
                if (signature instanceof BallerinaCallableUnitSignature) {
                    BallerinaFormalParameterList formalParameterList =
                            ((BallerinaCallableUnitSignature) signature).getFormalParameterList();
                    if (formalParameterList != null) {
                        // Note - We can set multiple object if we need to show overloaded function parameters.
                        context.setItemsToShow(new Object[]{formalParameterList});
                    } else {
                        // If no parameters are required, we set an empty string. Otherwise we wont be able to show
                        // "no param" message.
                        context.setItemsToShow(new Object[]{""});
                    }
                    context.showHint(functionInvocation, functionInvocation.getTextOffset(), this);
                } else if (signature instanceof BallerinaObjectCallableUnitSignature) {
                    BallerinaFormalParameterList formalParameterList =
                            ((BallerinaObjectCallableUnitSignature) signature).getFormalParameterList();
                    if (formalParameterList != null) {
                        // Note - We can set multiple object if we need to show overloaded function parameters.
                        context.setItemsToShow(new Object[]{formalParameterList});
                    } else {
                        // If no parameters are required, we set an empty string. Otherwise we wont be able to show
                        // "no param" message.
                        context.setItemsToShow(new Object[]{""});
                    }
                    context.showHint(functionInvocation, functionInvocation.getTextOffset(), this);
                }
            }
        } else if (element instanceof BallerinaTypeInitExpr) {
            BallerinaTypeInitExpr ballerinaTypeInitExpr = (BallerinaTypeInitExpr) element;
            BallerinaObjectInitializerParameterList objectInitializerParameterList = BallerinaPsiImplUtil
                    .getObjectInitializerParameterList(ballerinaTypeInitExpr);
            if (objectInitializerParameterList != null) {
                BallerinaObjectParameterList objectParameterList =
                        objectInitializerParameterList.getObjectParameterList();
                if (objectParameterList != null) {
                    context.setItemsToShow(new Object[]{objectParameterList});
                } else {
                    // If no parameters are required, we set an empty string. Otherwise we wont be able to show
                    // "no param" message.
                    context.setItemsToShow(new Object[]{""});
                }
                context.showHint(ballerinaTypeInitExpr, ballerinaTypeInitExpr.getTextOffset(), this);
            }
        } else if (element instanceof BallerinaInvocation) {
            BallerinaInvocation ballerinaTypeInitExpr = (BallerinaInvocation) element;
            PsiElement callableUnitSignature = BallerinaPsiImplUtil.getCallableUnitSignature(ballerinaTypeInitExpr);
            if (callableUnitSignature != null) {
                BallerinaFormalParameterList formalParameterList = null;
                if (callableUnitSignature instanceof BallerinaCallableUnitSignature) {
                    formalParameterList =
                            ((BallerinaCallableUnitSignature) callableUnitSignature).getFormalParameterList();
                } else if (callableUnitSignature instanceof BallerinaObjectCallableUnitSignature) {
                    formalParameterList =
                            ((BallerinaObjectCallableUnitSignature) callableUnitSignature).getFormalParameterList();
                }

                if (formalParameterList != null) {
                    // Note - We can set multiple object if we need to show overloaded function parameters.
                    context.setItemsToShow(new Object[]{formalParameterList});
                } else {
                    // If no parameters are required, we set an empty string. Otherwise we wont be able to show
                    // "no param" message.
                    context.setItemsToShow(new Object[]{""});
                }
                context.showHint(ballerinaTypeInitExpr, ballerinaTypeInitExpr.getTextOffset(), this);
            }
        }
    }

    @Nullable
    @Override
    public Object findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        return findElement(context);
    }

    @Override
    public void updateParameterInfo(@NotNull Object o, @NotNull UpdateParameterInfoContext context) {
        int index = getCurrentParameterIndex(o, context.getOffset());
        if (index != -1) {
            context.setCurrentParameter(index);
        }
    }

    public static int getCurrentParameterIndex(@NotNull Object o, int offset) {
        if (o instanceof BallerinaFunctionInvocation) {
            BallerinaFunctionInvocation functionInvocation = (BallerinaFunctionInvocation) o;
            BallerinaInvocationArgList invocationArgList = functionInvocation.getInvocationArgList();
            return getIndex(invocationArgList, offset);
        } else if (o instanceof BallerinaTypeInitExpr) {
            BallerinaTypeInitExpr ballerinaTypeInitExpr = (BallerinaTypeInitExpr) o;
            BallerinaInvocationArgList invocationArgList = ballerinaTypeInitExpr.getInvocationArgList();
            return getIndex(invocationArgList, offset);
        } else if (o instanceof BallerinaInvocation) {
            BallerinaInvocation ballerinaTypeInitExpr = (BallerinaInvocation) o;
            BallerinaInvocationArgList invocationArgList = ballerinaTypeInitExpr.getInvocationArgList();
            return getIndex(invocationArgList, offset);
        }
        return -1;
    }

    private static int getIndex(@Nullable BallerinaInvocationArgList invocationArgs, int offset) {
        if (invocationArgs == null) {
            return 0;
        }
        int index = 0;
        List<BallerinaInvocationArg> invocationArgList = invocationArgs.getInvocationArgList();
        for (BallerinaInvocationArg arg : invocationArgList) {
            TextRange textRange = arg.getTextRange();
            // If the caret is within the argument, we return the current arg index.
            if (textRange.getStartOffset() <= offset && offset <= textRange.getEndOffset()) {
                return index;
            }
            // If the caret is not within the arg, it might be between the leading "," or "(" and arg.
            // Eg - test ( | arg1, arg2);
            PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(arg);
            if (prevSibling instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                if (elementType == BallerinaTypes.COMMA || elementType == BallerinaTypes.LEFT_PARENTHESIS) {
                    // In here, the caret should be between the leading "," or "(" and the arg.
                    if (prevSibling.getTextOffset() < offset && offset < textRange.getStartOffset()) {
                        return index;
                    }
                }
            }
            // If the caret is not within the arg, it might be between the trailering "," or ")" and arg.
            // Eg - test (arg1,   |   arg2);
            PsiElement nextSibling = PsiTreeUtil.nextVisibleLeaf(arg);
            if (nextSibling instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) nextSibling).getElementType();
                if (elementType == BallerinaTypes.COMMA || elementType == BallerinaTypes.RIGHT_PARENTHESIS) {
                    // In here, the caret should be between the trailering "," or ")" and the arg.
                    if (nextSibling.getTextOffset() >= offset) {
                        return index;
                    }
                }
            }
            index++;
        }
        return invocationArgList.size();
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return "(,";
    }

    @Override
    public boolean tracksParameterIndex() {
        return true;
    }

    @Override
    public void updateUI(Object p, @NotNull ParameterInfoUIContext context) {
        updateObjectParameterPresentation(p, context);
    }

    public static String updateObjectParameterPresentation(Object p, @NotNull ParameterInfoUIContext context) {
        // This method contains the logic which we use to show the parameters in the popup.
        if (p instanceof BallerinaFormalParameterList) {
            // Get the parameter list. We highlight defaultable and rest parameters together since they can be mixed.
            BallerinaFormalParameterList formalParameterList = (BallerinaFormalParameterList) p;
            List<BallerinaParameter> parameterList = formalParameterList.getParameterList();
            // Get the parameter presentations.
            List<String> parameterPresentations = getParameterPresentations(formalParameterList);
            return updatePresentation(context, parameterPresentations, parameterList.size());
        } else if (p instanceof BallerinaObjectParameterList) {
            BallerinaObjectParameterList objectParameterList = (BallerinaObjectParameterList) p;
            List<BallerinaObjectParameter> parameterList = objectParameterList.getObjectParameterList();
            // Get the parameter presentations.
            List<String> parameterPresentations = getParameterPresentations(objectParameterList);
            return updatePresentation(context, parameterPresentations, parameterList.size());
        } else if (p instanceof String) {
            // Handle empty parameter scenario.
            if (((String) p).isEmpty()) {
                return context.setupUIComponentPresentation(CodeInsightBundle.message("parameter.info.no.parameters"),
                        0, 0, false, false, false, context.getDefaultParameterColor());
            }
        }
        return context.setupUIComponentPresentation(CodeInsightBundle.message("parameter.info.no.parameters"), 0, 0,
                false, false, false, context.getDefaultParameterColor());
    }

    public static String updatePresentation(@NotNull ParameterInfoUIContext context,
                                            @NotNull List<String> parameterPresentations, int parameterListSize) {
        // These will be used to identify which parameter is selected. This will be highlighted in the popup.
        int start = 0;
        int end = 0;
        StringBuilder builder = new StringBuilder();
        // If there are no parameter nodes, set "no parameters" message.
        if (parameterPresentations.isEmpty()) {
            builder.append(CodeInsightBundle.message("parameter.info.no.parameters"));
        } else {
            boolean paramFound = false;
            // Get the current parameter index.
            int selected = context.getCurrentParameterIndex();
            // Iterate through each parameter presentation.
            for (int i = 0; i < parameterPresentations.size(); i++) {
                // If i != 0, we need to add the "," between parameters.
                if (i != 0) {
                    builder.append(", ");
                }
                // In here, we need to check whether the current parameter is selected and it is not a
                // defualtable or rest parameter.
                // We also need to prevent updating the

                if ((i == selected && i < parameterListSize) || (!paramFound && i >= parameterListSize)) {
                    start = builder.length();
                    paramFound = true;
                }
                // Append the parameter.
                builder.append(parameterPresentations.get(i));
                // If the current parameter is the selected parameter, get the end index.
                if (i == selected && i < parameterListSize) {
                    end = builder.length();
                }
            }
        }
        // If the end is 0, that means the caret is now at defaultable or rest parameters. So we get the total
        // length as the end.
        if (end == 0) {
            end = builder.length();
        }
        // Call setupUIComponentPresentation with necessary arguments.
        return context.setupUIComponentPresentation(builder.toString(), start, end, false, false, false,
                context.getDefaultParameterColor());
    }

    /**
     * Creates a list of parameter presentations.
     *
     * @param node BallerinaFormalParameterList which contains the parameters
     * @return list of parameter presentations
     */
    public static List<String> getParameterPresentations(@Nullable BallerinaFormalParameterList node) {
        List<String> params = new LinkedList<>();
        if (node == null) {
            return params;
        }
        // Add parameters.
        List<BallerinaParameter> parameterList = node.getParameterList();
        for (BallerinaParameter parameter : parameterList) {
            params.add(formatParameter(parameter.getText()));
        }
        // Add defaultable parameters.
        List<BallerinaDefaultableParameter> defaultableParameterList = node.getDefaultableParameterList();
        for (BallerinaDefaultableParameter parameter : defaultableParameterList) {
            params.add(formatParameter(parameter.getText()));
        }
        // Add test parameter.
        BallerinaRestParameter restParameter = node.getRestParameter();
        if (restParameter != null) {
            params.add(formatParameter(restParameter.getText()));
        }
        return params;
    }

    public static List<String> getParameterPresentations(@Nullable BallerinaObjectParameterList node) {
        List<String> params = new LinkedList<>();
        if (node == null) {
            return params;
        }
        // Add parameters.
        List<BallerinaObjectParameter> parameterList = node.getObjectParameterList();
        for (BallerinaObjectParameter parameter : parameterList) {
            params.add(formatParameter(parameter.getText()));
        }
        // Add defaultable parameters.
        List<BallerinaObjectDefaultableParameter> defaultableParameterList = node.getObjectDefaultableParameterList();
        for (BallerinaObjectDefaultableParameter parameter : defaultableParameterList) {
            params.add(formatParameter(parameter.getText()));
        }
        // Add test parameter.
        BallerinaRestParameter restParameter = node.getRestParameter();
        if (restParameter != null) {
            params.add(formatParameter(restParameter.getText()));
        }
        return params;
    }

    /**
     * Removes excess spaces in the provided string. This is used to format parameters and types.
     *
     * @param text text to be formatted
     * @return formatted string.
     */
    public static String formatParameter(String text) {
        return text.trim().replaceAll("\\s+", " ").replaceAll("( )?\\[ ]", "[]");
    }
}
