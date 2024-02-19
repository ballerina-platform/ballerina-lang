package main

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
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
func TestDocCommandWithCustomTarget(t *testing.T) {
	testResourcesDir := "/home/pabadhi/myGoProjects/ballerina-lang/cli/ballerina-cli/src/test/resources/test-resources/doc_project"
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
	cmd := exec.Command("/home/pabadhi/myGoProjects/ballerina-lang/distribution/zip/jballerina-tools/build/extracted-distributions/jballerina-tools-2201.9.0-SNAPSHOT/bin/bal_linux_amd64", "doc", "-o", outputDir)
	_, err = cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Failed to execute doc command: %v", err)
	}

	indexFilePath := filepath.Join(outputDir, "foo", "winery", "0.1.0", "index.html")
	if _, err := os.Stat(indexFilePath); os.IsNotExist(err) {
		t.Fatalf("index.html file does not exist in custom target directory %s", err)
	}

}
