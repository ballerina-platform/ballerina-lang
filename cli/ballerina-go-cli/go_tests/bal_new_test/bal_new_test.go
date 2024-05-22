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

package bal_new_test

import (
	"fmt"
	"os"
	"os/exec"
	"os/user"
	"path/filepath"
	"regexp"
	"runtime"
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

// bal new template with a relative path
func TestNewCommandWithLibRelativePath(t *testing.T) {
	tmpDir, err := os.MkdirTemp("", "sample")
	if err != nil {
		t.Fatalf("Error creating temporary directory: %v", err)
	}
	defer os.RemoveAll(tmpDir)
	packageDir := filepath.Join(tmpDir, "sample_new")
	err = os.Mkdir(packageDir, os.ModePerm)
	if err != nil {
		fmt.Println("Error creating subdirectory:", err)
		return
	}
	err = os.Chdir(tmpDir)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	relativePath := "sample_new"
	args := []string{"-t", "lib", relativePath}
	_, err = runBallerinaNew(args)
	if err != nil {
		t.Fatalf("Error executing new command: %v", err)
	}
	currentUser, err := user.Current()
	expectedTomlContent := generateExpectedTomlContent(currentUser.Username)
	actualTomlContent, err := os.ReadFile(filepath.Join(packageDir, "Ballerina.toml"))
	assertDirExists(t, packageDir)
	assertFileExists(t, filepath.Join(packageDir, "Package.md"))
	assertFileExists(t, filepath.Join(packageDir, "Ballerina.toml"))
	assertFileExists(t, filepath.Join(packageDir, "sample_new.bal"))
	assertDirExists(t, filepath.Join(packageDir, "tests"))
	assertDirExists(t, filepath.Join(packageDir, "resources"))
	if removeWhitespace(string(expectedTomlContent)) != removeWhitespace(string(actualTomlContent)) {
		t.Errorf("expcted toml content does not match with the achual one:\nExpected: %s\nGot: %s", expectedTomlContent, actualTomlContent)
	}

}

func runBallerinaNew(args []string) (string, error) {
	rootDir := rootDir()
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, append([]string{"new"}, args...)...)
	output, err := cmd.Output()
	if err != nil {
		return "", err
	}
	return string(output), nil
}

func assertFileExists(t *testing.T, filePath string) {
	if _, err := os.Stat(filePath); os.IsNotExist(err) {
		t.Errorf("Expected file %q does not exist", filePath)
	}
}
func assertDirExists(t *testing.T, dirPath string) {
	if _, err := os.Stat(dirPath); os.IsNotExist(err) {
		t.Errorf("Expected directory %q does not exist", dirPath)
	}
}

func generateExpectedTomlContent(username string) string {
	// Generate the expected TOML content with the provided username
	return fmt.Sprintf(`[package]
org = "%s"
name = "sample_new"
version = "0.1.0"
distribution = "2201.9.0-SNAPSHOT"
`, username)
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}
