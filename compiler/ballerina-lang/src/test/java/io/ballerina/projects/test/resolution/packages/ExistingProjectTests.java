package io.ballerina.projects.test.resolution.packages;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Contains version resolution tests for existing projects.
 *
 * @since 2.0.0
 */
public class ExistingProjectTests extends AbstractPackageResolutionTest {
    @Test(dataProvider = "resolutionTestCaseProvider")
    public void testcaseExistingProjWithNoChanges(String testSuite, String testCase, boolean sticky) {
        runTestCase(testSuite, testCase, sticky);
    }

    @DataProvider(name = "resolutionTestCaseProvider")
    public static Object[][] testCaseProvider() {
        return new Object[][]{
                // 1. A new patch and minor version of a transitive has been released to central
                {"suite-existing_project", "case-0001", true},
                {"suite-existing_project", "case-0001", false},
                // 2. Adding of a new import which is already there as a transitive in the graph with an old version
                {"suite-existing_project", "case-0002", true},
                {"suite-existing_project", "case-0002", false},
                // 3. Remove existing import which is also a transitive dependency from another import
                {"suite-existing_project", "case-0003", true},
                {"suite-existing_project", "case-0003", false},
                // 4. Package contains a built-in transitive dependency with a non-zero version
                {"suite-existing_project", "case-0004", true},
                {"suite-existing_project", "case-0004", false},
                // 5. Package contains a built-in transitive dependency which has its dependencies changed
                //    in the current dist
                {"suite-existing_project", "case-0005", true},
                {"suite-existing_project", "case-0005", false},
                // 6. Remove existing import which also is a dependency of a newer patch version of another import
                {"suite-existing_project", "case-0006", true},
                {"suite-existing_project", "case-0006", false},
                // 8. package contains dependencies with pre-release versions
                // 9. use a module added in the new version of a dependency

        };
    }
}
