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

package bal_pack_test

import (
	"fmt"
	"os"
	"os/exec"
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
func TestPackCommand(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "validApplicationProject")
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	cmd := exec.Command(bal_path, "pack")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute pack command: %v", err)
	}
	expectedBuildLogPath := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "command-outputs", "unix", "compile-bal-project.txt")
	expectedBuildLog, err := os.ReadFile(expectedBuildLogPath)
	if err != nil {
		t.Fatalf("Failed to read expected build log: %v", err)
	}
	if removeWhitespace(string(output)) != removeWhitespace(string(expectedBuildLog)) {
		t.Errorf("Build log mismatch. Expected:\n%s\nGot:\n%s\n", expectedBuildLog, output)
	}
	balaFilePath := filepath.Join(projectPath, "target", "bala", "foo-winery-any-0.1.0.bala")
	_, err = os.Stat(balaFilePath)
	if os.IsNotExist(err) {
		t.Fatal("BALA file does not exist")
	}
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}
