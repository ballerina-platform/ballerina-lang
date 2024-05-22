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

package go_tests

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"testing"
)

func rootDir() string {
	_, file, _, ok := runtime.Caller(1)
	if !ok {
		panic("Failed to retrieve caller information")
	}
	dir := filepath.Dir(file)
	return filepath.Join(dir, "..", "..", "..", "..")
}

func TestAddCommand(t *testing.T) {
	rootDir := rootDir()
	fmt.Println(rootDir)
	projectPath, tmpDir, err := setup("sample_add")
	defer os.RemoveAll(tmpDir)
	if err != nil {
		t.Fatalf("Error setting up project: %v", err)
	}
	moduleDir := filepath.Join(projectPath, "modules")
	moduleName := "module1"
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)

	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	fmt.Println(bal_path)
	cmd := exec.Command(bal_path, "add", moduleName)
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute add command: %v", err)
	}

	modulePath := filepath.Join(moduleDir, moduleName)
	if _, err := os.Stat(modulePath); os.IsNotExist(err) {
		t.Errorf("Module directory does not exist: %v", modulePath)
	}

	moduleFilePath := filepath.Join(modulePath, moduleName+".bal")
	if _, err := os.Stat(moduleFilePath); os.IsNotExist(err) {
		t.Errorf("Module file does not exist: %v", moduleFilePath)
	}
	expectedOutput := "Added new Ballerina module at modules/module1"
	if removeWhitespace(string(output)) != removeWhitespace(expectedOutput) {
		t.Errorf("Actual log does not match expected log.\nExpected: %s\nActual: %s", expectedOutput, string(output))
	}

}

func setup(projectName string) (string, string, error) {
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	rootDir := rootDir()
	tmpDir, err := os.MkdirTemp("", "add_test")
	if err != nil {
		return "", "", fmt.Errorf("failed to create temporary directory: %v", err)
	}
	err = os.Chdir(tmpDir)
	if err != nil {
		fmt.Printf("Failed to change directory: %v", err)
	}
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, append([]string{"new"}, projectName)...)
	cmd.Dir = tmpDir
	if _, err := cmd.Output(); err != nil {
		return "", "", fmt.Errorf("failed to run 'bal new' command: %v", err)
	}
	projectPath := filepath.Join(tmpDir, projectName)
	return projectPath, tmpDir, nil
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}
