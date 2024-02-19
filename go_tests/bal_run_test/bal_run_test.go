package main

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"strings"
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

func TestRunValidBalProjectFromProjectDir(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources"
	tmpDir, err := setup(testResourcesDir)
	projectPath := filepath.Join(tmpDir, "validRunProject")
	tempFile := filepath.Join(projectPath, "temp.txt")
	err = os.Chdir(projectPath)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "run", "--", tempFile)
	err = cmd.Run()
	if err != nil {
		t.Fatalf("Failed to run Ballerina project: %v", err)
	}
	if _, err := os.Stat(tempFile); os.IsNotExist(err) {
		t.Error("Expected temporary file 'temp.txt' does not exist")
	}
	err = os.Remove(tempFile)
	if err != nil {
		t.Fatalf("Failed to delete temporary file: %v", err)
	}
	fmt.Println("All tests Passed")
	os.RemoveAll(tmpDir)
}

func TestRunBalProjectWithConfigurables(t *testing.T) {

	Dir := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_run_test/bal_run_sample"
	tmpDir, err := setup(Dir)
	tempFile := filepath.Join(tmpDir, "temp.txt")
	err = os.Chdir(tmpDir)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}

	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "run", "--", tempFile, "-Cint1=5", "-Cint2=8")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to run Ballerina project: %v", err)
	}
	if _, err := os.Stat(tempFile); os.IsNotExist(err) {
		t.Error("Expected temporary file 'temp.txt' does not exist")
	}
	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_run_test/run_output/run_output.txt"
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}
	startString := " Compiling"
	endString := "main.bal"
	outputMod := replaceVaryingString(startString, endString, string(expectedOutput))
	expectedOutputMod := replaceVaryingString(startString, endString, string(expectedOutput))
	if removeWhitespace(outputMod) != removeWhitespace(expectedOutputMod) {
		t.Errorf("output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
	os.RemoveAll(tmpDir)

	fmt.Println("All tests Passed")
}

func TestRunBalProject(t *testing.T) {

	Dir := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_run_test/bal_run_hello"
	tmpDir, err := setup(Dir)
	err = os.Chdir(tmpDir)
	if err != nil {
		t.Fatalf("Failed to change directory: %v", err)
	}

	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "run")
	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to run Ballerina project: %v", err)
	}

	expectedOutputPath := "/home/pabadhi/myGoProjects/ballerina-lang/go_tests/bal_run_test/run_output/run_hello_output.txt"
	expectedOutput, err := os.ReadFile(expectedOutputPath)
	if err != nil {
		t.Fatalf("Failed to read expected output file: %v", err)
	}
	if removeWhitespace(string(output)) != removeWhitespace(string(expectedOutput)) {
		t.Errorf("output does not match expected output:\nExpected: %s\nGot: %s", expectedOutput, output)
	}
	os.RemoveAll(tmpDir)

	fmt.Println("All tests Passed")
}

func removeWhitespace(input string) string {
	re := regexp.MustCompile(`\s+`)
	return re.ReplaceAllString(input, "")
}
func main() {

}
func replaceVaryingString(firstString, endString, output string) string {
	startIndex := strings.Index(output, firstString)
	endIndex := strings.Index(output, endString)

	if startIndex == -1 || endIndex == -1 {
		return output
	}

	return output[startIndex:endIndex]
}
