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

package bal_doc_test

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"testing"

	cp "github.com/otiai10/copy"
)

func rootDir() string {
	_, file, _, ok := runtime.Caller(1)
	if !ok {
		panic("Failed to retrieve caller information")
	}
	dir := filepath.Dir(file)
	return filepath.Join(dir, "..", "..", "..", "..")
}

func setup(dir string) (string, error) {
	tmpDir, err := os.MkdirTemp("", "doc-test-resources")
	if err != nil {
		return "", fmt.Errorf("failed to create temporary directory: %v", err)
	}

	err = cp.Copy(dir, tmpDir)
	return tmpDir, nil
}
func TestDocCommandWithCustomTarget(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "doc_project")
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	outputDir := filepath.Join(tmpDir, "MyOutputDir")

	err = os.MkdirAll(outputDir, os.ModePerm)
	if err != nil {
		t.Fatalf("Error creating directory: %v\n", err)
	}
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	Os := runtime.GOOS
	arch := runtime.GOARCH
	balName := fmt.Sprintf("bal_%s_%s", Os, arch)
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", balName)
	cmd := exec.Command(bal_path, "doc", "-o", outputDir)
	_, err = cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute doc command: %v", err)
	}

	indexFilePath := filepath.Join(outputDir, "foo", "winery", "0.1.0", "index.html")
	if _, err := os.Stat(indexFilePath); os.IsNotExist(err) {
		t.Fatalf("index.html file does not exist in custom target directory %s", err)
	}
}
