package main

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"testing"
)

func TestAddCommand(t *testing.T) {
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
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "add", moduleName)
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
	tmpDir, err := os.MkdirTemp("", "add_test")
	if err != nil {
		return "", "", fmt.Errorf("failed to create temporary directory: %v", err)
	}
	err = os.Chdir(tmpDir)
	if err != nil {
		fmt.Printf("Failed to change directory: %v", err)
	}
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", append([]string{"new"}, projectName)...)
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
