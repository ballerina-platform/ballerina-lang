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

package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.repository.CompilerInput;

import java.util.List;

/**
 * List of resolved sources and the RepoHierarchy used to resolve it.
 * Had to wrap in a class since you can't return multiple items form a java method.
 */
public class Resolution {
    public static final Resolution NOT_FOUND = new Resolution(null, null);
    public final RepoHierarchy resolvedBy;
    public final List<CompilerInput> inputs;

    Resolution(RepoHierarchy resolvedBy, List<CompilerInput> inputs) {
        this.resolvedBy = resolvedBy;
        this.inputs = inputs;
    }
}
