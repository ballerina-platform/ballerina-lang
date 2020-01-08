/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler.common;

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.format.FormatterCustomErrorStrategy;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;

import java.lang.reflect.InvocationTargetException;

/**
 * Custom error strategy factory.
 */
public class CustomErrorStrategyFactory {
    /**
     * Get custom error strategy for given class.
     *
     * @param customErrorStrategyClass class to get the error strategy from
     * @param context                  language server context
     * @return {@link BallerinaParserErrorStrategy} custom strategy
     */
    public static BallerinaParserErrorStrategy getCustomErrorStrategy(Class customErrorStrategyClass,
                                                                      LSContext context) {
        //TODO re-visit this to remove reflections
        BallerinaParserErrorStrategy lsCustomErrorStrategy;
        try {
            Object errStrategy = customErrorStrategyClass.getConstructor(LSContext.class).newInstance(context);
            if (errStrategy instanceof FormatterCustomErrorStrategy) {
                lsCustomErrorStrategy = (FormatterCustomErrorStrategy) errStrategy;
            } else {
                lsCustomErrorStrategy = (LSCustomErrorStrategy) errStrategy;
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException
                | NoSuchMethodException e) {
            lsCustomErrorStrategy = new LSCustomErrorStrategy(context);
        }
        return lsCustomErrorStrategy;
    }
}
