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

package bal_build_test

import (
	"fmt"
	"os"
	"os/exec"
	"os/user"
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
func TestBuildProjectByGivingTargertDir(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "validApplicationProject")
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	testTargetDir := filepath.Join(tmpDir, "test_target")
	err = os.Mkdir(testTargetDir, os.ModePerm)
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "build", "--target-dir", testTargetDir, projectPath)
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to build Ballerina project: %v\nOutput: %s", err, output)
	}
	expectedFiles := []string{
		filepath.Join(testTargetDir, "bin", "winery.jar"),
		filepath.Join(testTargetDir, "cache", "foo", "winery", "0.1.0", "java17", "foo-winery-0.1.0.jar"),
	}
	for _, file := range expectedFiles {
		if _, err := os.Stat(file); os.IsNotExist(err) {
			t.Errorf("Expected file does not exist: %s", file)
		}
	}

}

func TestBuildBalFile(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "valid-bal-file")
	tmpDir, err := setup(testResourcesDir)
	validBalFilePath := filepath.Join(tmpDir, "hello_world.bal")
	defer os.RemoveAll(tmpDir)
	err = os.Chdir(filepath.Dir(validBalFilePath))
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "build", validBalFilePath)
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to build Ballerina file: %v\n%s", err, output)
	}
	fmt.Println(validBalFilePath)
	DirName := "unix"
	if Os == "windows" {
		DirName = "windows"
	}
	expectedOutputPath := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_build_test", "bal_build_outputs", DirName, "build-hello-world-bal.txt")
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}
	if removeWhitespace(string(output)) != removeWhitespace(string(expectedOutput)) {
		t.Errorf("Build output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
	jarPath := filepath.Join("hello_world.jar")
	if _, err := os.Stat(jarPath); os.IsNotExist(err) {
		t.Errorf("Expected JAR file does not exist")
	}
	copiedJarFilePath := filepath.Join(tmpDir, "target", "hello_world-for-codegen-test.jar")
	err = cp.Copy(jarPath, copiedJarFilePath)
	if err != nil {
		t.Fatalf("Failed to copy JAR file: %v", err)
	}
	err = os.Remove(jarPath)
	if err != nil {
		t.Fatalf("Failed to delete JAR file: %v", err)
	}
	os.RemoveAll(tmpDir)
}

func TestBuildProjectWithDefaultBuildOptions(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "validProjectWithBuildOptions")
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "build")

	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to build Ballerina project: %v\nOutput: %s", err, output)
	}
	DirName := "unix"
	if Os == "windows" {
		DirName = "windows"
	}
	expectedOutputPath := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "command-outputs", DirName, "build-project-default-build-options.txt")
	expectedOutput, _ := os.ReadFile(expectedOutputPath)
	if removeWhitespace(string(output)) != removeWhitespace(string(expectedOutput)) {
		t.Errorf("Build output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
	if _, err := os.Stat(filepath.Join(projectPath, "target", "bin", "winery.jar")); os.IsNotExist(err) {
		t.Error("Expected JAR file 'winery.jar' does not exist")
	}
	if _, err := os.Stat(filepath.Join(projectPath, "target", "cache", "foo", "winery", "0.1.0", "java17", "foo-winery-0.1.0.jar")); os.IsNotExist(err) {
		t.Error("Expected JAR file 'foo-winery-0.1.0.jar' does not exist")
	}
	if _, err := os.Stat(filepath.Join(projectPath, "target", "cache", "foo", "winery", "0.1.0", "java17", "foo-winery-testable-0.1.0.jar")); !os.IsNotExist(err) {
		t.Error("Unexpected JAR file 'foo-winery-testable-0.1.0.jar' exists")
	}
	os.RemoveAll(tmpDir)
}

func TestBuildWithOffline(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-go-cli", "go_tests", "bal_build_test", "projectForTestOffline")
	tmpDir, err := setup(testResourcesDir)
	currentUser, err := user.Current()
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "build", "--offline")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Not get expected behaviour: %v", err)
	}
	var expectedOutput string
	switch Os {
	case "windows":
		expectedOutput = fmt.Sprintf(`
		Compiling source
		%s\projectForTestOffline:0.1.0
		ERROR [main.bal:(4:1,4:36)] cannot resolve module 'ballerinax\salesforce as sf'
		error: compilation contains errors
	`, currentUser.Username)
	default:
		expectedOutput = fmt.Sprintf(`
		Compiling source
		%s/projectForTestOffline:0.1.0
		ERROR [main.bal:(4:1,4:36)] cannot resolve module 'ballerinax/salesforce as sf'
		error: compilation contains errors
	`, currentUser.Username)
	}
	startStr := "source"
	endStr := ")]"
	outputMod := RemoveBetween(startStr, endStr, string(output))
	expectedOutputMod := RemoveBetween(startStr, endStr, expectedOutput)
	if removeWhitespace(outputMod) != removeWhitespace(expectedOutputMod) {
		t.Errorf("Actual log does not match expected log.\nExpected: %s\nActual: %s", expectedOutput, string(output))
	}
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
