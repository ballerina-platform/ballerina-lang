package main

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
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
func TestPrintGraphForBalProjectWithNoDependencies(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/validApplicationProject"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "graph")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute graph command: %v", err)
	}

	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/command-outputs/unix/graph-for-bal-project-with-no-dependencies.txt"
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
