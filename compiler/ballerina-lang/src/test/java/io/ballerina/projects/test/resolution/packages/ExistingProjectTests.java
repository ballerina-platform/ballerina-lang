package io.ballerina.projects.test.resolution.packages;

import io.ballerina.projects.environment.UpdatePolicy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Contains version resolution tests for existing projects.
 *
 * @since 2.0.0
 */
public class ExistingProjectTests extends AbstractPackageResolutionTest {
    @Test(dataProvider = "resolutionTestCaseProvider")
    public void testcaseExistingProjWithNoChanges(String testSuite, String testCase,
                                                  UpdatePolicy mode, boolean lessThan24HrsAfterBuild, String errMsg) {
        runTestCase(testSuite, testCase, mode, lessThan24HrsAfterBuild, errMsg);
    }

    private static Object[] testCase(String testCase, UpdatePolicy policy) {
        return new Object[]{"suite-existing_project", testCase, policy, false, null};
    }

    private static Object[] testCase(String testCase, UpdatePolicy policy, String errMsg) {
        return new Object[]{"suite-existing_project", testCase, policy, false, errMsg};
    }

    @DataProvider(name = "resolutionTestCaseProvider")
    public static Object[][] testCaseProvider() {
        return new Object[][]{
                // 1. A new patch and minor version of a transitive has been released to central
                testCase("case-0001", UpdatePolicy.SOFT),
                testCase("case-0001", UpdatePolicy.MEDIUM),
                testCase("case-0001", UpdatePolicy.HARD),
                testCase("case-0001", UpdatePolicy.LOCKED),
                // 2. Adding of a new import which is already there as a transitive in the graph with an old version
                testCase("case-0002", UpdatePolicy.SOFT),
                testCase("case-0002", UpdatePolicy.MEDIUM),
                testCase("case-0002", UpdatePolicy.HARD),
                testCase("case-0002", UpdatePolicy.LOCKED, "Invalid state"),
                // 3. Remove existing import which is also a transitive dependency from another import
                testCase("case-0003", UpdatePolicy.SOFT),
                testCase("case-0003", UpdatePolicy.MEDIUM),
                testCase("case-0003", UpdatePolicy.HARD),
                testCase("case-0003", UpdatePolicy.LOCKED),
                // 4. Package contains a built-in transitive dependency with a non-zero version
                testCase("case-0004", UpdatePolicy.SOFT),
                testCase("case-0004", UpdatePolicy.MEDIUM),
                testCase("case-0004", UpdatePolicy.HARD),
                testCase("case-0004", UpdatePolicy.LOCKED),
                // 5. Package contains a built-in transitive dependency which has its dependencies changed
                //    in the current dist
                testCase("case-0005", UpdatePolicy.SOFT),
                testCase("case-0005", UpdatePolicy.MEDIUM),
                testCase("case-0005", UpdatePolicy.HARD),
                testCase("case-0005", UpdatePolicy.LOCKED, "Cannot have new dependencies with the LOCKED update policy"),
//                // 6. Remove existing import which also is a dependency of a newer patch version of another import
//                {"suite-existing_project", "case-0006", true},
//                {"suite-existing_project", "case-0006", false},
//                // 7. Package contains hierarchical imports
//                {"suite-existing_project", "case-0007", true},
//                {"suite-existing_project", "case-0007", false},
//                // 8. Add a new hierarchical import which has a possible package name in the Dependencies.toml
//                {"suite-existing_project", "case-0008", true},
//                {"suite-existing_project", "case-0008", false},
//                // 9. Package uses a module available in the newer minor version of an existing dependency
//                {"suite-existing_project", "case-0009", true},
//                {"suite-existing_project", "case-0009", false},
//                // 10. package contains dependencies with pre-release versions
//                {"suite-existing_project", "case-0010", true},
//                {"suite-existing_project", "case-0010", false},
//                // 11. package contains dependencies with pre-release versions specified from local repo
//                {"suite-existing_project", "case-0011", true},
//                {"suite-existing_project", "case-0011", false},
//                // 12. package contains dependencies which only has pre-release versions published
//                {"suite-existing_project", "case-0012", true},
//                {"suite-existing_project", "case-0012", false},
//                // 13. package contains dependency which specified in the Ballerina toml file thats not local
//                {"suite-existing_project", "case-0013", true},
//                {"suite-existing_project", "case-0013", false},
//                // 14. package contains 2 dependencies one of which is in Ballerina toml file thats not local
//                {"suite-existing_project", "case-0014", true},
//                {"suite-existing_project", "case-0014", false},
//                // 15. package updates transitive dependency from the Ballerina toml file that is not local
//                {"suite-existing_project", "case-0015", true},
//                {"suite-existing_project", "case-0015", false},
//                // 16. package name is hierarchical, there are new versions in the central,
//                // and the older version is specified in Ballerina.toml and Dependencies.toml
//                {"suite-existing_project", "case-0016", true},
//                {"suite-existing_project", "case-0016", false},
//                {"suite-existing_project", "case-0017", true},
//                {"suite-existing_project", "case-0017", false}
        };
    }
}
