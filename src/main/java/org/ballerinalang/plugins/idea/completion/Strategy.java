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

/**
 * Interface which is used to pass methods to {@link BallerinaCompletionUtils#checkPrevNodeAndHandle} method. These
 * passed methods will contain the logic which is executed to add lookup elements.
 *
 * @param <T> A {@link com.intellij.codeInsight.completion.CompletionParameters} object
 * @param <U> A {@link com.intellij.codeInsight.completion.CompletionResultSet} object
 * @param <V> A {@link com.intellij.psi.PsiElement} object which represents the element which we are editing
 */
@FunctionalInterface
public interface Strategy<T, U, V> {

    void execute(T t, U u, V v);
}
