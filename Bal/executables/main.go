package main

import (
	"executables/pkg/generate"
	"fmt"
	"log"
	"os"
	"path/filepath"
)

func main() {
	path, err := os.Executable()
	if err != nil {
		panic(err)
	}
	for {
		link, err := filepath.EvalSymlinks(path)
		if err != nil {
			log.Fatalln("Error resolving symbolic link:", err)
		}
		if link == path {
			break
		}
		path = link
	}
	directory := filepath.Join(filepath.Dir(path), "config")
	commandPath := filepath.Join(filepath.Dir(path), "..", "bal", "cmd")
	err = filepath.Walk(directory, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.IsDir() && filepath.Ext(path) == ".toml" {
			fileName := filepath.Base(path)
			name := fileName[:len(fileName)-len(".toml")]
			fmt.Printf("Processing file: %s\n", name)
			generate.GeneratingCLICommands(directory, name, commandPath)
		}
		return nil
	})
	if err != nil {
		fmt.Printf("Error walking the path: %s\n", err)
	}
}
