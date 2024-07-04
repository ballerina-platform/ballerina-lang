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

package org.ballerinalang.test.documentation;

import io.ballerina.projects.Project;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Test cases related to error types in docs.
 *
 * @since 2.0
 */
public class ErrorsTest {
    private Module testModule;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "errors_project";
        Project project = BCompileUtil.loadProject(sourceRoot);
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        List<Module> modulesList = BallerinaDocGenerator.getDocsGenModel(moduleDocMap, project.currentPackage()
                .packageOrg().toString(), project.currentPackage().packageVersion().toString());
        testModule = modulesList.get(0);
    }

    @Test(description = "Test error type")
    public void testErrorAsAType() {
        Assert.assertEquals(testModule.errors.size(), 10, "Ten errors expected");
        Assert.assertEquals(testModule.errors.get(0).name, "AccountError");
        Assert.assertEquals(testModule.errors.get(1).name, "AccountNotFoundError");
        Assert.assertEquals(testModule.errors.get(2).name, "CacheError");
        Assert.assertEquals(testModule.errors.get(3).name, "CancelledError");
        Assert.assertEquals(testModule.errors.get(4).name, "Error");
        Assert.assertEquals(testModule.errors.get(5).name, "GrpcError");
        Assert.assertEquals(testModule.errors.get(6).name, "InvalidAccountIdError");
        Assert.assertEquals(testModule.errors.get(7).name, "LinkToGrpcError");
        Assert.assertEquals(testModule.errors.get(8).name, "TotalCacheError");
        Assert.assertEquals(testModule.errors.get(9).name, "UnKnownError");

        Assert.assertEquals(testModule.errors.get(4).detailType.name, "CacheError",
                "The error detailtype of Error should be CacheError. But Found:" +
                        testModule.errors.get(4).detailType.name);
        Assert.assertEquals(testModule.errors.get(4).detailType.category, "errors",
                "The detailtype category of Error should be errors. But Found:" + testModule.errors.get(4)
                        .detailType.category);

        Assert.assertEquals(testModule.errors.get(7).detailType.name, "GrpcError",
                "The error detailtype of LinkToGrpcError should be GrpcError. But Found:" +
                        testModule.errors.get(7).detailType.name);
        Assert.assertEquals(testModule.errors.get(7).detailType.category, "errors",
                "The detailtype category of LinkToGrpcError should be errors. But Found:"
                        + testModule.errors.get(7).detailType.category);

        Assert.assertEquals(testModule.errors.get(5).detailType.memberTypes.get(0).name, "CancelledError",
                "The name of first membertype, of error detailtype, in GrpcError should be CancelledError. " +
                        "But Found:" + testModule.errors.get(5).detailType.memberTypes.get(0).name);
        Assert.assertEquals(testModule.errors.get(5).detailType.memberTypes.get(0).category, "errors",
                "The category of first membertype, of error detailtype, in GrpcError should be errors. " +
                        "But Found:" + testModule.errors.get(5).detailType.memberTypes.get(0).category);

        Assert.assertEquals(testModule.errors.get(5).detailType.memberTypes.get(1).name, "UnKnownError",
                "The name of second membertype, of error detailtype, in GrpcError should be CancelledError. " +
                        "But Found:" + testModule.errors.get(5).detailType.memberTypes.get(1).name);
        Assert.assertEquals(testModule.errors.get(5).detailType.memberTypes.get(1).category, "errors",
                "The category of second membertype, of error detailtype, in GrpcError should be errors. " +
                        "But Found:" + testModule.errors.get(5).detailType.memberTypes.get(1).category);

        Assert.assertTrue(testModule.errors.get(8).isDistinct);
        Assert.assertEquals(testModule.errors.get(8).detailType.name, "CacheError");
        Assert.assertEquals(testModule.errors.get(8).detailType.category, "errors");
        Assert.assertEquals(testModule.errors.get(8).detailType.orgName, "test_org");
        Assert.assertEquals(testModule.errors.get(8).detailType.moduleName, "doc_errors_project");
        Assert.assertEquals(testModule.errors.get(8).detailType.version, "1.0.0");

        Assert.assertTrue(testModule.errors.get(1).isDistinct);
        Assert.assertEquals(testModule.errors.get(1).detailType.name, "AccountError");
        Assert.assertEquals(testModule.errors.get(1).detailType.category, "errors");
        Assert.assertEquals(testModule.errors.get(1).detailType.orgName, "test_org");
        Assert.assertEquals(testModule.errors.get(1).detailType.moduleName, "doc_errors_project");
        Assert.assertEquals(testModule.errors.get(1).detailType.version, "1.0.0");

        Assert.assertTrue(testModule.errors.get(0).isDistinct);
        Assert.assertEquals(testModule.errors.get(0).detailType.name, "AccountErrorData");
        Assert.assertEquals(testModule.errors.get(0).detailType.category, "records");
        Assert.assertEquals(testModule.errors.get(0).detailType.orgName, "test_org");
        Assert.assertEquals(testModule.errors.get(0).detailType.moduleName, "doc_errors_project");
        Assert.assertEquals(testModule.errors.get(0).detailType.version, "1.0.0");
    }

    @Test(description = "Test type")
    public void testType() {
        Assert.assertEquals(testModule.unionTypes.size(), 1, "One union type expected");

        Assert.assertEquals(testModule.simpleNameReferenceTypes.get(0).name, "LinktoYError", "The type should be " +
                "LinktoYError. But Found:" + testModule.simpleNameReferenceTypes.get(0).name);
        Assert.assertEquals(testModule.unionTypes.get(0).name, "YErrorType", "The type should be " +
                "YErrorType. But Found:" + testModule.unionTypes.get(0).name);

        Assert.assertEquals(testModule.simpleNameReferenceTypes.get(0).memberTypes.get(0).name, "YErrorType",
                "The name of first membertype, of LinktoYError should be YErrorType. But Found:" +
                        testModule.simpleNameReferenceTypes.get(0).memberTypes.get(0).name);
        Assert.assertEquals(testModule.simpleNameReferenceTypes.get(0).memberTypes.get(0).category, "types",
                "The category of first membertype, of LinktoYError should be types. " +
                        "But Found:" + testModule.simpleNameReferenceTypes.get(0).memberTypes.get(0).category);

        Assert.assertEquals(testModule.unionTypes.get(0).memberTypes.get(0).name, "error",
                "The name of first membertype, of error detailtype, in YErrorType should be error. But Found:" +
                        testModule.unionTypes.get(0).memberTypes.get(0).name);
        Assert.assertEquals(testModule.unionTypes.get(0).memberTypes.get(0).category, "builtin",
                "The category of first membertype, of error detailtype,in YErrorType should be builtin. " +
                        "But Found:" + testModule.unionTypes.get(0).memberTypes.get(0).category);
    }

}
