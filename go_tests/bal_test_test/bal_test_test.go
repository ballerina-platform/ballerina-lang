package main

import (
	"encoding/xml"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"strings"
	"testing"

	cp "github.com/otiai10/copy"
)

func setup(dir string) (string, error) {
	tmpDir, err := os.MkdirTemp("", "run-test-resources")
	if err != nil {
		return "", fmt.Errorf("failed to create temporary directory: %v", err)
	}

	err = cp.Copy(dir, tmpDir)
	return tmpDir, nil
}

func TestBuildMultiModuleProject(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir, "validMultiModuleProjectWithTests")
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	testCmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "test")
	testCmd.Dir = projectPath
	output, err := testCmd.CombinedOutput()
	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_test_test/test_outputs/test_multiProject.txt"
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("failed to execute test command: %v", err)
	}
	if removeWhitespace(string(output)) != removeWhitespace(string(expectedOutput)) {
		t.Errorf("Actual log does not match expected log.\nExpected: %s\nActual: %s", string(expectedOutput), string(output))
	}
	os.RemoveAll(tmpDir)
}

func TestModule1SingleTest(t *testing.T) {
	testResourcesDir := "/home/pabadhi/ballerina-lang/tests/testerina-integration-test/src/test/resources/project-based-tests/module-execution-tests"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	args := []string{"--code-coverage", "--includes=*", "--tests", "moduleExecution.Module1:module1_test1"}
	output, err := runBallerinaTest(args)
	if err != nil {
		t.Fatalf("failed to execute test command: %v", err)
	}

	firstString := "Compiling source"
	endString := "Generating Test Report"
	output = replaceVaryingString(firstString, endString, output)
	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/tests/testerina-integration-test/src/test/resources/command-outputs/unix/ModuleExecutionTest-test_Module1_SingleTest.txt"
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	expectedOutputStr := replaceVaryingString(firstString, endString, string(expectedOutput))
	if err != nil {
		t.Fatalf("failed to read expected output file: %v", err)
	}
	if removeWhitespace(string(output)) != removeWhitespace(string(expectedOutputStr)) {
		t.Errorf("output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
	os.RemoveAll(tmpDir)
}

func TestMultipleGroupExecution(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_test_test/bal_test_group"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir) //"tests")
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	//go_tests/bal_test_test/bal_test_group/tests/main_test.bal
	//balFile := "main_test.bal"
	groups := "g2,g4"
	//args := []string{"--groups", groups, balFile}
	args := []string{"--groups", groups}
	output, err := runBallerinaTest(args)
	if err != nil {
		t.Fatalf("failed to execute test command: %v", err)
	}
	expectedOutputFile := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_test_test/test_outputs/test_groups.txt"
	expectedOutput, err := os.ReadFile(expectedOutputFile)
	if err != nil {
		t.Fatalf("failed to read expected output file: %v", err)
	}

	if removeWhitespace(string(output)) != removeWhitespace(string(expectedOutput)) {
		t.Errorf("output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
}

func TestTestWithReport(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/validProjectWithTests"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	args := []string{"--test-report"}
	output, err := runBallerinaTest(args)
	if err != nil {
		t.Fatalf("failed to execute test command: %v", err)
	}
	reportDir := filepath.Join(projectPath, "target", "report")
	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_test_test/test_outputs/test_report.txt"
	expexted_output, err := os.ReadFile(expectedOutputPath)
	assertFileExists(t, reportDir, "test_results.json")
	startString1 := "Compiling source"
	endString1 := "Generating Test Report"
	outputMod1 := replaceVaryingString(startString1, endString1, string(output))
	expectedOutputMod1 := replaceVaryingString(startString1, endString1, string(expexted_output))
	if removeWhitespace(outputMod1) != removeWhitespace(expectedOutputMod1) {
		t.Errorf("expcted output does not match with the achual one:\nExpected: %s\nGot: %s", string(expectedOutputMod1), outputMod1)
	}

	startString2 := "test_results.json"
	endString2 := "report."
	outputMod2 := replaceVaryingString(startString2, endString2, string(output))
	expectedOutputMod2 := replaceVaryingString(startString2, endString2, string(expexted_output))
	if removeWhitespace(outputMod2) != removeWhitespace(expectedOutputMod2) {
		t.Errorf("expcted output does not match with the achual one:\nExpected: %s\nGot: %s", string(expectedOutputMod2), outputMod2)
	}

}

func TestMultipleModulePkgCoverageTest(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/tests/testerina-integration-test/src/test/resources/project-based-tests"
	///home/pabadhi/ballerina-lang/tests/testerina-integration-test/src/test/resources/project-based-tests/test-report-tests
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir, "test-report-tests")
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	// args := []string{"--code-coverage", "--coverage-format=xml"}
	// _, err = runBallerinaTest(args)
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "test", "--code-coverage", "--coverage-format=xml")
	_, err = cmd.CombinedOutput()

	// Check if XML report generated
	reportRoot := filepath.Join(projectPath, "target", "report", "foo")
	coverageXMLPath := filepath.Join(reportRoot, "coverage-report.xml")
	if _, err := os.Stat(reportRoot); os.IsNotExist(err) {
		t.Fatal("Error occurred while generating the coverage XML for package.")
	}
	///home/pabadhi/myGoProjects/ballerina-lang/tests/testerina-integration-test/src/test/resources/project-based-tests/test-report-tests/target/report/foo/coverage-report.xml
	expectedPackageNames := []string{
		"testerina_report/foo&0046math$test/0/constants",
		"testerina_report/foo$test/0/types",
		"testerina_report/foo&0046bar/0/creators",
		"testerina_report/foo&0046bar&0046tests/0",
		"testerina_report/foo&0046math$test/0",
		"testerina_report/foo/0/creators",
		"testerina_report/foo&0046bar&0046tests/0/creators",
		"testerina_report/foo&0046bar&0046tests/0/types",
		"testerina_report/foo&0046math/0/constants",
		"testerina_report/foo&0046bar&0046tests$test/0/constants",
		"testerina_report/foo&0046math/0/types",
		"testerina_report/foo&0046bar/0/annotations",
		"test-report-tests/modules/math",
		"testerina_report/foo&0046math$test/0/creators",
		"testerina_report/foo&0046math/0/creators",
		"testerina_report/foo&0046bar&0046tests$test/0/types",
		"test-report-tests",
		"testerina_report/foo/0/types",
		"testerina_report/foo&0046bar/0/types",
		"testerina_report/foo&0046bar&0046tests/0/constants",
		"testerina_report/foo$test/0",
		"testerina_report/foo/0",
		"testerina_report/foo&0046bar/0",
		"testerina_report/foo/0/annotations",
		"testerina_report/foo/0/constants",
		"testerina_report/foo&0046math$test/0/types",
		"testerina_report/foo&0046math/0/annotations",
		"testerina_report/foo&0046bar&0046tests$test/0/creators",
		"testerina_report/foo&0046bar&0046tests$test/0",
		"testerina_report/foo&0046bar&0046tests/0/annotations",
		"testerina_report/foo&0046bar&0046tests$test/0/annotations",
		"testerina_report/foo&0046math$test/0/annotations",
		"testerina_report/foo$test/0/annotations",
		"testerina_report/foo$test/0/constants",
		"testerina_report/foo&0046math/0",
		"testerina_report/foo&0046bar/0/constants",
		"test-report-tests/modules/bar",
		"testerina_report/foo$test/0/creators",
	}

	val, err := validatePackageNames(coverageXMLPath, expectedPackageNames)
	if !val {
		t.Fatalf("Package Name Validation for coverage XML failed for multi-module project %s", err)
	}
}

func replaceVaryingString(firstString, endString, output string) string {
	startIndex := strings.Index(output, firstString)
	endIndex := strings.Index(output, endString)

	if startIndex == -1 || endIndex == -1 {
		return output
	}

	return output[startIndex:endIndex]
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}

func runBallerinaTest(args []string) (string, error) {
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", append([]string{"test"}, args...)...)
	output, err := cmd.Output()
	if err != nil {
		return "", err
	}

	return string(output), nil
}

func assertFileExists(t *testing.T, dir, filename string) {
	filePath := filepath.Join(dir, filename)
	if _, err := os.Stat(filePath); os.IsNotExist(err) {
		t.Errorf("expected file %q does not exist", filePath)
	}
}

func validatePackageNames(coverageXMLPath string, expectedPackageNames []string) (bool, error) {
	file, err := os.Open(coverageXMLPath)
	if err != nil {
		return false, err
	}
	defer file.Close()

	data, err := os.ReadFile(coverageXMLPath)
	if err != nil {
		return false, err
	}

	var packages Packages
	err = xml.Unmarshal(data, &packages)
	if err != nil {
		return false, err
	}

	for _, packageName := range expectedPackageNames {
		found := false
		for _, pkg := range packages.Package {
			if pkg.Name == packageName {
				found = true
				break
			}
		}
		if !found {
			return false, nil
		}
	}

	return true, nil
}

type Packages struct {
	XMLName xml.Name  `xml:"report"`
	Package []Package `xml:"package"`
}

type Package struct {
	Name   string `xml:"name,attr"`
	Source []struct {
		File []struct {
			Line []struct {
				Nr string `xml:"nr,attr"`
			} `xml:"line"`
		} `xml:"sourcefile"`
	} `xml:"packages"`
}
