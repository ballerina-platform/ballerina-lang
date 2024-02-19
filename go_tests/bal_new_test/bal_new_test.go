package main

import (
	"fmt"
	"os"
	"os/exec"
	"os/user"
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

	// Execute the new command
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
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", append([]string{"new"}, args...)...)
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
