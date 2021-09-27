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
package io.ballerina.projects.test.resolution.packages.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds details about the actual and expected dependency graph comparison.
 *
 * @since 2.0.0
 */
public class GraphComparisonResult {
    private final boolean identicalGraphs;
    private final List<String> diagnostics = new ArrayList<>();

    public static final GraphComparisonResult IDENTICAL_GRAPH_COMP_RESULT =
            new GraphComparisonResult(true);

    public GraphComparisonResult(boolean identicalGraphs) {
        this.identicalGraphs = identicalGraphs;
    }

    public boolean isIdenticalGraphs() {
        return identicalGraphs;
    }

    public Collection<String> diagnostics() {
        return diagnostics;
    }

    public void addDiagnostic(String diagnostic) {
        diagnostics.add(diagnostic);
    }
}
