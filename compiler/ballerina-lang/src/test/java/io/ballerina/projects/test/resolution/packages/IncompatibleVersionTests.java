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
                // 1. User specifies an incompatible version in Ballerina.toml
                {"suite-incompatible_versions", "case-0001", true},
                {"suite-incompatible_versions", "case-0001", false},

        };
    }
}
