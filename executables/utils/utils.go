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

package utils

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"strconv"
	"strings"
)

func IsJarFile(filePath string) bool {
	return len(filePath) > 4 && filePath[len(filePath)-4:] == ".jar"
}

func ExtractDebugPort(debugArg string) int {
	re := regexp.MustCompile(`--debug=([0-9]+)`)
	matches := re.FindStringSubmatch(debugArg)
	if len(matches) != 2 {
		return 0
	}
	port, err := strconv.Atoi(matches[1])
	if err != nil {
		port = 0
	}
	return port
}

func CommandExists(cmd string) bool {
	_, err := exec.LookPath(cmd)
	return err == nil
}

func GetTerminalColumns() (int, error) {
	cmd := exec.Command("tput", "cols")
	output, err := cmd.Output()
	if err != nil {
		return 0, err
	}

	cols, err := strconv.Atoi(string(output))
	if err != nil {
		return 0, err
	}
	return cols, nil
}

func SetBallerinaCLIWidth() {
	if !CommandExists("tput") {
		os.Setenv("BALLERINA_CLI_WIDTH", "80")
	} else {
		if cols, err := GetTerminalColumns(); err != nil {
			os.Setenv("BALLERINA_CLI_WIDTH", "80")
		} else {
			os.Setenv("BALLERINA_CLI_WIDTH", strconv.Itoa(cols))
		}
	}
}

func ValidateDebugPort(port int) bool {
	return port > 0
}

func SetBallerinaClassPath(ballerinaHome string, javaHome string) string {
	var ballerinaClasspath string
	toolsJarPath := filepath.Join(ballerinaHome, "bre", "lib", "tools.jar")
	if _, err := os.Stat(toolsJarPath); err == nil {
		ballerinaClasspath += filepath.Join(javaHome, "lib", "tools.jar")
	}
	jarPaths := []string{
		filepath.Join(ballerinaHome, "bre", "lib"),
		filepath.Join(ballerinaHome, "lib", "tools", "lang-server", "lib"),
		filepath.Join(ballerinaHome, "lib", "tools", "debug-adapter", "lib"),
	}
	for _, path := range jarPaths {
		files, err := filepath.Glob(filepath.Join(path, "*.jar"))
		if err != nil {
			os.Exit(1)
		}
		for _, f := range files {
			ballerinaClasspath += string(filepath.ListSeparator) + f
		}
	}

	return ballerinaClasspath
}

func GetJavaOpts() string {
	var balJavaDebug string
	balJavaDebug, isSet := os.LookupEnv("BAL_JAVA_DEBUG")
	javaOpts := ""
	if isSet {
		if balJavaDebug == "" {
			fmt.Println("Please specify the debug port for the BAL_JAVA_DEBUG variable")
			os.Exit(1)
		} else {
			javaOpts = os.Getenv("JAVA_OPTS")

			if javaOpts != "" {
				fmt.Println("Warning !!! User specified JAVA_OPTS may interfere with BAL_JAVA_DEBUG")
			}

			balDebugOpts := os.Getenv("BAL_DEBUG_OPTS")

			if balDebugOpts != "" {
				javaOpts = javaOpts + " " + balDebugOpts
			} else {
				javaOpts = javaOpts + " -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + balJavaDebug
			}
		}
	}
	return javaOpts
}

func GetJavaSettings(ballerinaHome string) (javaHome, javaCmd string) {
	javaPath := filepath.Join(ballerinaHome, "..", "..", "dependencies", "@JDK_VERSION@")
	javaCmd = os.Getenv("JAVACMD")
	javaHome = os.Getenv("JAVA_HOME")
	if stat, err := os.Stat(javaPath); err == nil && stat.IsDir() {
		javaHome = javaPath
	}
	OS := runtime.GOOS
	if OS == "windows" {
		javaExePath := filepath.Join(javaHome, "bin", "java.exe")
		if _, err := os.Stat(javaExePath); err != nil {
			javaCmd = javaExePath
		}
	} else {
		javaExePath := filepath.Join(javaHome, "bin", "java")
		if _, err := os.Stat(javaExePath); err != nil {
			javaCmd = javaExePath
		}
	}
	//Set javaHome to installed java version in the user's device
	switch OS {
	case "darwin":
		javaVersion := os.Getenv("JAVA_VERSION")
		path := filepath.Join(string(filepath.Separator), "usr", "libexec", "java_home")
		if javaHome == "" {
			if javaVersion == "" {
				CmdOutput, err := exec.Command(path).Output()
				if err == nil {
					javaHome = strings.TrimSpace(string(CmdOutput))
					//os.Setenv("JAVA_HOME", javaHome)
				}
			} else {
				// If JAVA_HOME is not set, but JAVA_VERSION is set, get the Java home for the specified version
				CmdOutput, err := exec.Command(path, "-v", javaVersion).Output()
				if err == nil {
					javaHome = strings.TrimSpace(string(CmdOutput))
					//os.Setenv("JAVA_HOME", javaHome)
				}
			}
		}

	}

	if javaCmd == "" {
		if javaHome != "" {
			javaCmdPath := filepath.Join(javaHome, "jre", "sh", "java")
			if _, err := os.Stat(javaCmdPath); err == nil {
				javaCmd = javaCmdPath
			} else {
				javaCmd = filepath.Join(javaHome, "bin", "java")
			}
		} else {
			javaCmd = "java"
		}
	}
	javaCmd = strings.TrimSpace(javaCmd)
	return javaHome, javaCmd
}

func ExecuteCommand(javaCmd string, cmdArgs []string) error {
	cmd := exec.Command(javaCmd, cmdArgs...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.Stdin = os.Stdin
	return cmd.Run()
}
