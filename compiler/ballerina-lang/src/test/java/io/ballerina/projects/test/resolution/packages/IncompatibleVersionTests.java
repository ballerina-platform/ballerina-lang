package io.ballerina.projects.test.resolution.packages;

import io.ballerina.projects.environment.PackageLockingMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Contains version resolution tests for existing projects.
 *
 * @since 2.0.0
 */
public class IncompatibleVersionTests extends AbstractPackageResolutionTest {

    @Test(dataProvider = "resolutionTestCaseProvider")
    public void testcaseExistingProjWithNoChanges(String testSuite, String testCase, PackageLockingMode lockingMode) {
        runTestCase(testSuite, testCase, lockingMode);
    }

    @DataProvider(name = "resolutionTestCaseProvider")
    public static Object[][] testCaseProvider() {
        return new Object[][]{
                // 1. User specifies an incompatible version for a direct dependency in Ballerina.toml
                {"suite-incompatible_versions", "case-0001", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0001", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0001", PackageLockingMode.SOFT},
                // 2. User specifies an incompatible version for a dependency in Ballerina.toml - advanced
                {"suite-incompatible_versions", "case-0002", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0002", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0002", PackageLockingMode.SOFT},
                // 3. User adds a new dependency which has a dependency
                // that conflicts with an existing direct dependency
                {"suite-incompatible_versions", "case-0003", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0003", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0003", PackageLockingMode.SOFT},
                // 4. User specifies an incompatible version for an indirect dependency in Ballerina.toml
                {"suite-incompatible_versions", "case-0004", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0004", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0004", PackageLockingMode.SOFT},
                // 5. User specifies an incompatible version for an indirect dependency in Ballerina.toml - advanced
                {"suite-incompatible_versions", "case-0005", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0005", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0005", PackageLockingMode.SOFT},
                // 6. Two incompatible versions found as transitive dependencies
                {"suite-incompatible_versions", "case-0006", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0006", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0006", PackageLockingMode.SOFT},
                // 7. User specifies an incompatible version of a transitive dependency in Ballerina.toml
                {"suite-incompatible_versions", "case-0007", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0007", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0007", PackageLockingMode.SOFT},
                // 8. Parent transitive dep upgrade should replace child transitive deps without false conflict
                {"suite-incompatible_versions", "case-0008", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0008", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0008", PackageLockingMode.SOFT},
                // 9. Fresh project: parent transitive dep upgrade replaces child deps without false conflict
                {"suite-incompatible_versions", "case-0009", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0009", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0009", PackageLockingMode.SOFT},
                // 10. Parent upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0010", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0010", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0010", PackageLockingMode.SOFT},
                // 11. Fresh project: parent upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0011", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0011", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0011", PackageLockingMode.SOFT},
                // 12. Transitive dep upgrade replaces incompatible pre-1.0 child dep
                {"suite-incompatible_versions", "case-0012", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0012", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0012", PackageLockingMode.SOFT},
                // 13. Fresh project: transitive dep upgrade replaces incompatible pre-1.0 child dep
                {"suite-incompatible_versions", "case-0013", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0013", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0013", PackageLockingMode.SOFT},
                // 14. Patch upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0014", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0014", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0014", PackageLockingMode.SOFT},
                // 15. Fresh project: patch upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0015", PackageLockingMode.HARD},
                {"suite-incompatible_versions", "case-0015", PackageLockingMode.MEDIUM},
                {"suite-incompatible_versions", "case-0015", PackageLockingMode.SOFT},
        };
    }

    @Test(dataProvider = "resolutionTestCaseProviderOld")
    public void testcaseExistingProjWithNoChangesWIthOldStickyFlag(String testSuite, String testCase, boolean sticky) {
        runTestCase(testSuite, testCase, sticky);
    }

    @DataProvider(name = "resolutionTestCaseProviderOld")
    public static Object[][] testCaseProviderOld() {
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
                // 7. User specifies an incompatible version of a transitive dependency in Ballerina.toml
                {"suite-incompatible_versions", "case-0007", true},
                {"suite-incompatible_versions", "case-0007", false},
                // 8. Parent transitive dep upgrade should replace child transitive deps without false conflict
                {"suite-incompatible_versions", "case-0008", true},
                {"suite-incompatible_versions", "case-0008", false},
                // 9. Fresh project: parent transitive dep upgrade replaces child deps without false conflict
                {"suite-incompatible_versions", "case-0009", true},
                {"suite-incompatible_versions", "case-0009", false},
                // 10. Parent upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0010", true},
                {"suite-incompatible_versions", "case-0010", false},
                // 11. Fresh project: parent upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0011", true},
                {"suite-incompatible_versions", "case-0011", false},
                // 12. Transitive dep upgrade replaces incompatible pre-1.0 child dep
                {"suite-incompatible_versions", "case-0012", true},
                {"suite-incompatible_versions", "case-0012", false},
                // 13. Fresh project: transitive dep upgrade replaces incompatible pre-1.0 child dep
                {"suite-incompatible_versions", "case-0013", true},
                {"suite-incompatible_versions", "case-0013", false},
                // 14. Patch upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0014", true},
                {"suite-incompatible_versions", "case-0014", false},
                // 15. Fresh project: patch upgrade changes dep structure with incompatible transitive dep versions
                {"suite-incompatible_versions", "case-0015", true},
                {"suite-incompatible_versions", "case-0015", false},
        };
    }
}
