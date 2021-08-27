package io.ballerina.projects.test.resolution.packages;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExistingProjectTests extends AbstractPackageResolutionTest {
    @Test(dataProvider = "resolutionTestCaseProvider")
    public void testcaseExistingProjWithNoChanges(String testSuite, String testCase, boolean sticky) {
        runTestCase(testSuite, testCase, sticky);
    }

    @DataProvider(name = "resolutionTestCaseProvider")
    public static Object[][] testCaseProvider() {
        return new Object[][]{
                // 1. a new patch and minor version of a transitive has been released to central
                {"suite-existing_project", "case-0001", true},
                {"suite-existing_project", "case-0001", false},
                // 2. adding new import which is already there as a transitive in the graph with an old version
//                {"suite-existing_project", "case-0002", true},
//                {"suite-existing_project", "case-0002", false},
                // 3. remove existing import which is already there as a transitive in the graph
//                {"suite-existing_project", "case-0003", true},
//                {"suite-existing_project", "case-0003", false}
                // 4. package contains a built-in dependency with an older version
                {"suite-existing_project", "case-0004", true},
                {"suite-existing_project", "case-0004", false},
                // 5. package contains a built-in dependency with an older version
                {"suite-existing_project", "case-0005", true},
                {"suite-existing_project", "case-0005", false},
                // 6. package contains dependencies with pre-release versions
                // 7. package contains ballerina/observe
                // 8. use a module added in the new version of a dependency
        };
    }
}
