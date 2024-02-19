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
	tmpDir, err := os.MkdirTemp("", "run-test-resources")
	if err != nil {
		return "", fmt.Errorf("failed to create temporary directory: %v", err)
	}

	err = cp.Copy(dir, tmpDir)
	return tmpDir, nil
}

func TestPackCommand(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/validApplicationProject"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "pack")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute pack command: %v", err)
	}

	expectedBuildLog, err := os.ReadFile("/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/command-outputs/unix/compile-bal-project.txt")
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
