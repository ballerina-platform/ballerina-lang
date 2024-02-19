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

func TestBuildProjectByGivingTargertDir(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/validApplicationProject"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	testTargetDir := filepath.Join(tmpDir, "test_target")
	err = os.Mkdir(testTargetDir, os.ModePerm)
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "build", "--target-dir", testTargetDir, projectPath)
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
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/valid-bal-file"
	tmpDir, err := setup(testResourcesDir)
	validBalFilePath := filepath.Join(tmpDir, "hello_world.bal")
	defer os.RemoveAll(tmpDir)
	err = os.Chdir(filepath.Dir(validBalFilePath))
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "build", validBalFilePath)
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to build Ballerina file: %v\n%s", err, output)
	}
	fmt.Println(validBalFilePath)
	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_build_test/bal_build_outputs/build-hello-world-bal.txt"
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
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/validProjectWithBuildOptions"
	tmpDir, err := setup(testResourcesDir)
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "build")

	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to build Ballerina project: %v\nOutput: %s", err, output)
	}
	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/command-outputs/unix/build-project-default-build-options.txt"
	expectedOutput, err := os.ReadFile(expectedOutputPath)
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
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_build_test/projectForTestOffline"
	tmpDir, err := setup(testResourcesDir)
	currentUser, err := user.Current()
	defer os.RemoveAll(tmpDir)
	projectPath := filepath.Join(tmpDir)
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "build", "--offline")
	output, err := cmd.CombinedOutput()
	if err == nil {
		t.Fatalf("Not get expected behaviour: %v", err)
	}

	expectedOutput := fmt.Sprintf(`
		Compiling source
		%s/projectForTestOffline:0.1.0
		ERROR [main.bal:(4:1,4:36)] cannot resolve module 'ballerinax/salesforce as sf'
		error: compilation contains errors
	`, currentUser.Username)

	if removeWhitespace(string(output)) != removeWhitespace(expectedOutput) {
		t.Errorf("Actual log does not match expected log.\nExpected: %s\nActual: %s", expectedOutput, string(output))
	}
}
func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}

func main() {
}
