package io.ballerina.projects.test.resolution.packages;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Contains version resolution tests for existing projects.
 *
 * @since 2.0.0
 */
public class IncompatibleVersionTests extends AbstractPackageResolutionTest {
    @Test(dataProvider = "resolutionTestCaseProvider")
    public void testcaseExistingProjWithNoChanges(String testSuite, String testCase, boolean sticky) {
        runTestCase(testSuite, testCase, sticky);
    }

    @DataProvider(name = "resolutionTestCaseProvider")
    public static Object[][] testCaseProvider() {
        return new Object[][]{
                // 1. User specifies an incompatible version for a direct dependency in Ballerina.toml
                {"suite-incompatible_versions", "case-0001", true},
                {"suite-incompatible_versions", "case-0001", false},
                // 2. User specifies an incompatible version for a dependency in Ballerina.toml - advanced
                {"suite-incompatible_versions", "case-0002", true},
                {"suite-incompatible_versions", "case-0002", false},
                // 3. User adds a new dependency which has a dependency
                // that conflicts with an existing direct dependency
                {"suite-incompatible_versions", "case-0003", true},
                {"suite-incompatible_versions", "case-0003", false},
                // 4. User specifies an incompatible version for an indirect dependency in Ballerina.toml
                {"suite-incompatible_versions", "case-0004", true},
                {"suite-incompatible_versions", "case-0004", false},
                // 5. User specifies an incompatible version for an indirect dependency in Ballerina.toml - advanced
                {"suite-incompatible_versions", "case-0005", true},
                {"suite-incompatible_versions", "case-0005", false},
                // 6. Two incompatible versions found as transitive dependencies
                {"suite-incompatible_versions", "case-0006", true},
                {"suite-incompatible_versions", "case-0006", false},
        };
    }
}
