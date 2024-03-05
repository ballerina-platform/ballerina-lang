/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package main

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
	tmpDir, err := os.MkdirTemp("", "doc-test-resources")
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
	return filepath.Join(dir, "..", "..")
}
func TestPrintGraphForBalProjectWithNoDependencies(t *testing.T) {
	rootDir := rootDir()
	testResourcesDir := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "validApplicationProject")
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	bal_path := filepath.Join(rootDir, "distribution", "zip", "jballerina-tools", "build", "extracted-distributions", "jballerina-tools-2201.9.0-SNAPSHOT", "bin", "bal_linux_amd64")
	cmd := exec.Command(bal_path, "graph")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute graph command: %v", err)
	}
	expectedOutputPath := filepath.Join(rootDir, "cli", "ballerina-cli", "src", "test", "resources", "test-resources", "command-outputs", "unix", "graph-for-bal-project-with-no-dependencies.txt")
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}
	if removeWhitespace(string(output)) != removeWhitespace(string(expectedOutput)) {
		t.Errorf("Actual log does not match expected log.\nExpected: %s\nActual: %s", string(expectedOutput), string(output))
	}
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}
