/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;

import java.util.List;

/**
 * This class contains reusable functionalities across Item Contexts.
 */
public abstract class AbstractItemContext {

    /**
     * Check whether the token stream contains an annotation start (@).
     *
     * @param ctx Completion operation context
     * @return {@link Boolean}      Whether annotation context start or not
     */
    public boolean isAnnotationStart(LSContext ctx) {
        List<String> poppedTokens = CommonUtil.popNFromList(CommonUtil.getPoppedTokenStrings(ctx), 4);
        return poppedTokens.contains(UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY);
    }

    /**
     * Check whether the token stream corresponds to a action invocation or a function invocation.
     *
     * @param context Completion operation context
     * @return {@link Boolean}      Whether invocation or Field Access
     */
    protected boolean isInvocationOrInteractionOrFieldAccess(LSContext context) {
        List<String> poppedTokens = CommonUtil.popNFromList(CommonUtil.getPoppedTokenStrings(context), 2);
        return poppedTokens.contains(UtilSymbolKeys.DOT_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)
                || poppedTokens.contains(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.LEFT_ARROW_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.BANG_SYMBOL_KEY);
    }
}
