/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.task;

import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;
import org.ballerinalang.test.runtime.entity.Test;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.testerina.core.TestProcessor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Task for listing groups defined in tests.
 *
 * @since 2.0.0
 */
public class ListTestGroupsTask implements Task {

    private final PrintStream out;
    private boolean displayWarning;

    public ListTestGroupsTask(PrintStream out, boolean displayWarning) {
        this.out = out;
        this.displayWarning = displayWarning;
    }

    @Override
    public void execute(Project project) {
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            TestProcessor testProcessor = new TestProcessor();
            Optional<TestSuite> suite = testProcessor.testSuite(module);
            if (displayWarning) {
                out.println("\nWarning: Other flags are skipped when list-groups flag is provided.\n");
            }
            if (!project.currentPackage().packageOrg().anonymous()) {
                out.println();
                out.println("\t" + module.moduleName().toString());
            }
            listGroups(suite.orElse(null), this.out);
        }
    }

    /**
     * lists the groups available in tests.
     *
     * @param testSuite testSuite
     */
    private void listGroups(TestSuite testSuite, PrintStream outStream) {
        List<String> groupList = getGroupList(testSuite);
        if (groupList.size() == 0) {
            outStream.println("\tThere are no groups available!");
        } else {
            outStream.println("\tFollowing groups are available : ");
            outStream.println("\t" + groupList);
        }
    }

    /**
     * Returns a distinct list of groups in test functions.
     *
     * @param testSuite testSuite
     * @return a list of groups
     */
    private List<String> getGroupList(TestSuite testSuite) {
        if (testSuite == null) {
            return Collections.emptyList();
        }
        List<String> groupList = new ArrayList<>();
        for (Test test : testSuite.getTests()) {
            if (test.getGroups().size() > 0) {
                groupList.addAll(test.getGroups());
            }
        }
        return groupList.stream().distinct().collect(Collectors.toList());
    }
}
