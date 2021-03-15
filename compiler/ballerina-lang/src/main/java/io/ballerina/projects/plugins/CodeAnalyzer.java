/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.plugins;

/**
 * Represents a collection of code analysis tasks that can analyze syntax nodes, symbols and compilation
 * and report diagnostics.
 *
 * @since 2.0.0
 */
public abstract class CodeAnalyzer {
    /**
     * Initializes the code analyzer with the analysis context.
     * <p>
     * This method is invoked for each run of the compilation for implementers
     * to add required {@code AnalysisTask}s. It is recommended to make the code analyzer and
     * other {@code AnalysisTask}s stateless as this method can be invoked by multiple threads.
     * If it is unable to make {@code AnalysisTask}s stateless, add new instances of
     * analysis tasks in the init method.
     *
     * @param analysisContext the context to which {@code AnalysisTask}s can be added
     */
    public abstract void init(CodeAnalysisContext analysisContext);

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
}
