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
	directory := filepath.Join(filepath.Dir(path), "..", "..", "ballerina-cli", "src", "main", "resources", "command-info")
	commandPath := filepath.Join(filepath.Dir(path), "bal", "cmd")
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
