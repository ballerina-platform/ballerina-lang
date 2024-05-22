/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package bal_test_test

import (
	"encoding/xml"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
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
func rootDir() string {
	_, file, _, ok := runtime.Caller(1)
	if !ok {
		panic("Failed to retrieve caller information")
	}
	dir := filepath.Dir(file)
	return filepath.Join(dir, "..", "..", "..", "..")
}

func TestBuildMultiModuleProject(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources")
	tmpDir, err := setup(testResourcesDir)
	if err != nil {
		t.Fatalf("Failed to set up test resources: %v", err)
	}
	defer os.RemoveAll(tmpDir)

	projectPath := filepath.Join(tmpDir, "validMultiModuleProjectWithTests")
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}

	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	balPath := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	testCmd := exec.Command(balPath, "test")
	testCmd.Dir = projectPath
	output, err := testCmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute test command: %v", err)
	}

	DirName := "unix"
	if Os == "windows" {
		DirName = "windows"
	}
	expectedOutputPath := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_test_test", "test_outputs", DirName, "test_multiProject.txt")
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}

	actualOutput := removeExecutionTime(string(output))
	expectedOutputClean := removeExecutionTime(string(expectedOutput))

	if removeWhitespace(actualOutput) != removeWhitespace(expectedOutputClean) {
		t.Errorf("Actual log does not match expected log.\nExpected: %s\nActual: %s", expectedOutputClean, actualOutput)
	}

	os.RemoveAll(tmpDir)
}

func TestModule1SingleTest(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "project-based-tests", "module-execution-tests")
	tmpDir, err := setup(testResourcesDir)
	if err != nil {
		t.Fatalf("Failed to set up test resources: %v", err)
	}
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	args := []string{"--code-coverage", "--includes=*", "--tests", "moduleExecution.Module1:module1_test1"}
	output, err := runBallerinaTest(args)
	if err != nil {
		t.Fatalf("Failed to execute test command: %v", err)
	}
	firstString := "Compiling source"
	endString := "Generating Test Report"
	output = replaceVaryingString(firstString, endString, output)
	dirName := "unix"
	if runtime.GOOS == "windows" {
		dirName = "windows"
	}
	expectedOutputPath := filepath.Join(rootDir, "tests", "testerina-integration-test", "src", "test", "resources", "command-outputs", dirName, "ModuleExecutionTest-test_Module1_SingleTest.txt")
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}

	expectedOutputStr := replaceVaryingString(firstString, endString, string(expectedOutput))
	if removeWhitespace(output) != removeWhitespace(expectedOutputStr) {
		t.Errorf("Output does not match expected output:\nExpected: %s\nGot: %s", expectedOutputStr, output)
	}
}

func TestMultipleGroupExecution(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_test_test", "bal_test_group")
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	groups := "g2,g4"
	args := []string{"--groups", groups}
	output, err := runBallerinaTest(args)
	if err != nil {
		t.Fatalf("failed to execute test command: %v", err)
	}
	outputstr := removeExecutionTime(string(output))
	DirName := "unix"
	Os := runtime.GOOS
	if Os == "windows" {
		DirName = "windows"
	}
	expectedOutputFile := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_test_test", "test_outputs", DirName, "test_groups.txt")
	expectedOutput, err := os.ReadFile(expectedOutputFile)
	expectedOutputStr := removeExecutionTime(string(expectedOutput))
	if err != nil {
		t.Fatalf("failed to read expected output file: %v", err)
	}
	if removeWhitespace(outputstr) != removeWhitespace(expectedOutputStr) {
		t.Errorf("output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
}

func TestTestWithReport(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "validProjectWithTests")
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
	DirName := "unix"
	Os := runtime.GOOS
	if Os == "windows" {
		DirName = "windows"
	}
	expectedOutputPath := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_test_test", "test_outputs", DirName, "test_report.txt")
	expexted_output, err := os.ReadFile(expectedOutputPath)
	assertFileExists(t, reportDir, "test_results.json")
	startString1 := "Compiling source"
	endString1 := "Generating Test Report"
	outputMod1 := replaceVaryingString(startString1, endString1, string(output))
	outputMod1 = removeExecutionTime(outputMod1)
	expectedOutputMod1 := replaceVaryingString(startString1, endString1, string(expexted_output))
	expectedOutputMod1 = removeExecutionTime(expectedOutputMod1)
	if removeWhitespace(outputMod1) != removeWhitespace(expectedOutputMod1) {
		t.Errorf("expcted output does not match with the achual one:\nExpected: %s\nGot: %s", string(expectedOutputMod1), outputMod1)
	}

	startString2 := "test_results.json"
	endString2 := "report."
	outputMod2 := replaceVaryingString(startString2, endString2, string(output))
	outputMod2 = removeExecutionTime(outputMod2)
	expectedOutputMod2 := replaceVaryingString(startString2, endString2, string(expexted_output))
	expectedOutputMod2 = removeExecutionTime(expectedOutputMod2)
	if removeWhitespace(outputMod2) != removeWhitespace(expectedOutputMod2) {
		t.Errorf("expcted output does not match with the achual one:\nExpected: %s\nGot: %s", string(expectedOutputMod2), outputMod2)
	}

}

func TestMultipleModulePkgCoverageTest(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "tests", "testerina-integration-test", "src", "test", "resources", "project-based-tests")
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir, "test-report-tests")
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "test", "--code-coverage", "--coverage-format=xml")
	_, err = cmd.CombinedOutput()

	reportRoot := filepath.Join(projectPath, "target", "report", "foo")
	coverageXMLPath := filepath.Join(reportRoot, "coverage-report.xml")
	if _, err := os.Stat(reportRoot); os.IsNotExist(err) {
		t.Fatal("Error occurred while generating the coverage XML for package.")
	}
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

func removeExecutionTime(output string) string {
	re := regexp.MustCompile(`(?m)^\s*Test execution time : \d+\.\d+s\s*$`)
	return re.ReplaceAllString(output, "")
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}

func runBallerinaTest(args []string) (string, error) {
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	rootDir := rootDir()
	balPath := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(balPath, append([]string{"test"}, args...)...)
	output, err := cmd.CombinedOutput()
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
