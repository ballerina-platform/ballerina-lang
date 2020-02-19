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

package org.ballerinalang.packerina.task;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.test.launcher.entity.Test;
import org.ballerinalang.test.launcher.entity.TestSuite;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task for listing groups defined in tests.
 *
 * @since 1.1.2
 */
public class ListTestGroupsTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
       List<BLangPackage> moduleBirMap = buildContext.getModules();
        for (BLangPackage bLangPackage : moduleBirMap) {
            PackageID packageID = bLangPackage.packageID;
            TestSuite suite = TesterinaRegistry.getInstance().getTestSuites().get(packageID.toString());
            listGroups(suite, buildContext.out());
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
            outStream.println("There are no groups available!");
        } else {
            outStream.println("Following groups are available : ");
            outStream.println(groupList);
        }
    }

    /**
     * Returns a distinct list of groups in test functions.
     *
     * @param testSuite testSuite
     * @return a list of groups
     */
    private List<String> getGroupList(TestSuite testSuite) {
        List<String> groupList = new ArrayList<>();
        for (Test test : testSuite.getTests()) {
            if (test.getGroups().size() > 0) {
                groupList.addAll(test.getGroups());
            }
        }
        return groupList.stream().distinct().collect(Collectors.toList());
    }
}
