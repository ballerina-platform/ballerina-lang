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

package bal_run_test

import (
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

func TestRunValidBalProjectFromProjectDir(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources")
	tmpDir, err := setup(testResourcesDir)
	projectPath := filepath.Join(tmpDir, "validRunProject")
	tempFile := filepath.Join(projectPath, "temp.txt")
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "run", "--", tempFile)
	err = cmd.Run()
	if err != nil {
		t.Fatalf("Failed to run Ballerina project: %v", err)
	}
	if _, err := os.Stat(tempFile); os.IsNotExist(err) {
		t.Error("Expected temporary file 'temp.txt' does not exist")
	}
	err = os.Remove(tempFile)
	if err != nil {
		t.Fatalf("Failed to delete temporary file: %v", err)
	}
	fmt.Println("All tests Passed")
	os.RemoveAll(tmpDir)
}

func TestRunBalProjectWithConfigurables(t *testing.T) {
	rootDir := rootDir()
	Dir := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_run_test", "bal_run_sample")
	tmpDir, err := setup(Dir)
	err = os.Chdir(tmpDir)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "run", "--", "-Cint1=5", "-Cint2=8")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to run Ballerina project: %v", err)
	}
	DirName := "unix"
	if Os == "windows" {
		DirName = "windows"
	}
	expectedOutputPath := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_run_test", "run_output", DirName, "run_output.txt")
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}
	startString := "source"
	endString := "unused"
	outputMod := RemoveBetween(startString, endString, string(expectedOutput))
	expectedOutputMod := RemoveBetween(startString, endString, string(expectedOutput))
	if removeWhitespace(outputMod) != removeWhitespace(expectedOutputMod) {
		t.Errorf("output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
	os.RemoveAll(tmpDir)

	fmt.Println("All tests Passed")
}

func TestRunBalProject(t *testing.T) {
	rootDir := rootDir()
	Dir := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_run_test", "bal_run_hello")
	tmpDir, err := setup(Dir)
	err = os.Chdir(tmpDir)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}

	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "run")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to run Ballerina project: %v", err)
	}
	DirName := "unix"
	if Os == "windows" {
		DirName = "windows"
	}
	expectedOutputPath := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_run_test", "run_output", DirName, "run_hello_output.txt")
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	startString := "source"
	endString := "bal"
	outputMod := RemoveBetween(startString, endString, string(expectedOutput))
	expectedOutputMod := RemoveBetween(startString, endString, string(expectedOutput))
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}
	if removeWhitespace(outputMod) != removeWhitespace(expectedOutputMod) {
		t.Errorf("output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
	os.RemoveAll(tmpDir)

	fmt.Println("All tests Passed")
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}

func RemoveBetween(startStr, endStr, input string) string {
	startIndex := strings.Index(input, startStr)
	if startIndex == -1 {
		return input
	}

	endIndex := strings.Index(input[startIndex:], endStr)
	if endIndex == -1 {
		return input
	}
	endIndex += startIndex + len(endStr)

	return input[:startIndex] + input[endIndex:]
}
