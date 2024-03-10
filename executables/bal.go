// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package main

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

func main() {
	ballerinaHome := os.Getenv("BALLERINA_HOME")
	ballerinaVersion := "@VERSION@"
	javaHome, javaCmd := getJavaSettings(ballerinaHome)
	prg, err := os.Executable()
	if err != nil {
		os.Exit(1)
	}
	for {
		link, err := filepath.EvalSymlinks(prg)
		if err != nil {
			fmt.Println("Error resolving symbolic link:", err)
			os.Exit(1)
		}
		if link == prg {
			break
		}
		prg = link
	}

	// Get the directory of the script
	prgDir := filepath.Dir(prg)
	// Set BALLERINA_HOME
	ballerinaHome, _ = filepath.Abs(filepath.Join(prgDir, ".."))

	if !commandExists(javaCmd) {
		fmt.Println("Error: JAVA_HOME is not defined correctly.")
		os.Exit(1)
	}
	if javaHome == "" {
		fmt.Println("You must set the JAVA_HOME variable before running Ballerina.")
		os.Exit(1)
	}

	setBallerinaCLIWidth()
	javaOpts := getJavaOpts()
	ballerinaClasspath := setBallerinaClassPath(ballerinaHome, javaHome)

	//Define Ballerina CLI Arguments
	cmdLineArgs := []string{
		"-Xms256m", "-Xmx1024m",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-classpath", ballerinaClasspath,
		"-Dballerina.home=" + ballerinaHome,
		"-Dballerina.version=" + ballerinaVersion,
		"-Denable.nonblocking=false",
		"-Djava.security.egd=file:/dev/./urandom",
		"-Dfile.encoding=UTF8",
		"-Dballerina.target=jvm",
		"-Djava.command=" + javaCmd,
	}

	if javaOpts != "" {
		JAVA_OPTS := strings.Fields(javaOpts)
		cmdLineArgs = append(cmdLineArgs, JAVA_OPTS...)
	}
	//Implementation of bal run <*.jar>
	if len(os.Args) >= 3 && os.Args[1] == "run" && isJarFile(os.Args[2]) {
		cmdArgs := append(cmdLineArgs, "-jar")
		cmdArgs = append(cmdArgs, os.Args[2:]...)
		cmd := exec.Command(javaCmd, cmdArgs...)
		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr
		cmd.Stdin = os.Stdin
		err := cmd.Run()
		if err != nil {
			os.Exit(1)
		}
		os.Exit(0)
	} else if len(os.Args) == 4 && os.Args[1] == "run" && isJarFile(os.Args[3]) && os.Args[2][:8] == "--debug=" {
		debugPort := extractDebugPort(os.Args[2]) //Implementation of bal run --debug=<PORT> <*.jar>
		if validateDebugPort(debugPort) {
			cmdArgs := append(cmdLineArgs,
				"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address="+strconv.Itoa(debugPort),
				"-jar",
				os.Args[3],
			)
			cmd := exec.Command(javaCmd, cmdLineArgs...)
			cmd.Args = append(cmd.Args, cmdArgs...)
			cmd.Stdout = os.Stdout
			cmd.Stderr = os.Stderr

			err := cmd.Run()
			if err != nil {
				os.Exit(1)
			}
			os.Exit(0)
		} else {
			fmt.Println("Error: Invalid debug port number specified.")
			os.Exit(1)
		}
	} else if len(os.Args) == 5 && os.Args[1] == "run" && os.Args[2] == "--debug" && isJarFile(os.Args[4]) {
		debugPort, err := strconv.Atoi(os.Args[3]) // handles "bal run --debug <PORT> <JAR_PATH>" command
		if err != nil {
			debugPort = 0
		}
		if validateDebugPort(debugPort) {
			cmdArgs := append(cmdLineArgs,
				"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address="+strconv.Itoa(debugPort),
				"-jar")
			cmdArgs = append(cmdArgs, os.Args[4:]...)
			cmd := exec.Command(javaCmd, cmdArgs...)
			cmd.Stdout = os.Stdout
			cmd.Stderr = os.Stderr

			err := cmd.Run()
			if err != nil {
				os.Exit(1)
			}
			os.Exit(0)
		} else {
			fmt.Println("Error: Invalid debug port number specified.")
			os.Exit(1)
		}
	} else {

		cmdArgs := append(cmdLineArgs, "io.ballerina.cli.launcher.Main")
		cmdArgs = append(cmdArgs, os.Args[1:]...)
		cmd := exec.Command(javaCmd, cmdArgs...)
		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr
		err = cmd.Run()
		if err != nil {
			os.Exit(1)
		}
		os.Exit(0)
	}
}

func isJarFile(filePath string) bool {
	return len(filePath) > 4 && filePath[len(filePath)-4:] == ".jar"
}

func extractDebugPort(debugArg string) int {
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

func commandExists(cmd string) bool {
	_, err := exec.LookPath(cmd)
	return err == nil
}

func getTerminalColumns() (int, error) {
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

func setBallerinaCLIWidth() {
	if !commandExists("tput") {
		os.Setenv("BALLERINA_CLI_WIDTH", "80")
	} else {
		if cols, err := getTerminalColumns(); err != nil {
			os.Setenv("BALLERINA_CLI_WIDTH", "80")
		} else {
			os.Setenv("BALLERINA_CLI_WIDTH", strconv.Itoa(cols))
		}
	}
}

func validateDebugPort(port int) bool {
	return port > 0
}

func setBallerinaClassPath(ballerinaHome string, javaHome string) string {
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

func getJavaOpts() string {
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

func getJavaSettings(ballerinaHome string) (javaHome, javaCmd string) {
	javaPath := filepath.Join(ballerinaHome, "..", "..", "dependencies", "jdk-17.0.7+7-jre")
	javaCmd = os.Getenv("JAVACMD")
	javaHome = os.Getenv("JAVA_HOME")

	if stat, err := os.Stat(javaPath); err == nil && stat.IsDir() {
		javaHome = javaPath
		os.Setenv("JAVA_HOME", javaHome)
	}

	switch runtime.GOOS {
	case "darwin":
		javaVersion := os.Getenv("JAVA_VERSION")
		path := filepath.Join("/", "usr", "libexec", "java_home")
		if javaHome == "" {
			if javaVersion == "" {
				javaHomeCmd, _ := exec.Command(path).Output()
				javaHome = strings.TrimSpace(string(javaHomeCmd))
				os.Setenv("JAVA_HOME", javaHome)
			} else {
				// If JAVA_HOME is not set, but JAVA_VERSION is set, get the Java home for the specified version
				fmt.Println("Using Java version: ", javaVersion)
				javaHomeCmd := exec.Command(path, "-v", javaVersion)
				javaHomeOutput, err := javaHomeCmd.Output()
				if err == nil {
					javaHome = strings.TrimSpace(string(javaHomeOutput))
					os.Setenv("JAVA_HOME", javaHome)
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
