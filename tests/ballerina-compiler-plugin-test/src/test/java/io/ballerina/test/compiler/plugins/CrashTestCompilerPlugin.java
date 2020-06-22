/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.test.compiler.plugins;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A test implementation of the {@link org.ballerinalang.compiler.plugins.CompilerPlugin} interface.
 */
@SupportedAnnotationPackages(
        value = {"testOrg/functions"}
)
public class CrashTestCompilerPlugin extends AbstractCompilerPlugin {

    static Map<TestEvent.Kind, Set<TestEvent>> testEventMap = new HashMap<>();

    @Override
    public void init(DiagnosticLog diagnosticLog) {
    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.FUNC_ANN, functionNode.getName().getValue(), annotations.size());
        // emulate compiler plugin crash
        throw new IndexOutOfBoundsException();
    }

    private void addEvent(TestEvent.Kind kind, String nodeName, int noOfAnnotations) {
        addEvent(new TestEvent(kind, nodeName, noOfAnnotations));
    }

    private void addEvent(TestEvent event) {
        Set<TestEvent> eventSet = testEventMap.computeIfAbsent(event.kind, kind -> new HashSet<>());
        eventSet.add(event);
    }
}
