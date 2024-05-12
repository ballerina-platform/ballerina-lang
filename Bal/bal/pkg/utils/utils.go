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
	"bal/pkg/generate"
	"fmt"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"

	tsize "github.com/kopoli/go-terminal-size"
)

func IsJarFile(filePath string) bool {
	return len(filePath) > 4 && filePath[len(filePath)-4:] == ".jar"
}

func CommandExists(cmd string) bool {
	_, err := exec.LookPath(cmd)
	return err == nil
}

func SetBallerinaCLIWidth() {
	s, err := tsize.GetSize()
	if err != nil {
		os.Setenv("BALLERINA_CLI_WIDTH", strconv.Itoa(s.Width))
		return
	} else {
		os.Setenv("BALLERINA_CLI_WIDTH", "80")
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
		files, _ := filepath.Glob(filepath.Join(path, "*.jar"))
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
} //optimise the validation with validation functions

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
	return cmd.Run() //type error  check md  run command
}

func Setup() ([]string, string) {
	ballerinaVersion := "2201.8.5"
	balScriptPath, err := os.Executable()
	balScriptPath = "/usr/lib/ballerina/distributions/ballerina-2201.8.5/bin/bal"
	if err != nil {
		log.Fatalln("Error: Cannot find the path of the executable file")
	}

	for {
		link, err := filepath.EvalSymlinks(balScriptPath)
		if err != nil {
			log.Fatalln("Error resolving symbolic link:", err)
		}
		if link == balScriptPath {
			break
		}
		balScriptPath = link
	}
	scriptPathDir := filepath.Dir(balScriptPath)

	ballerinaHome, _ := filepath.Abs(filepath.Join(scriptPathDir, ".."))
	javaHome, javaCmd := GetJavaSettings(ballerinaHome)

	if !CommandExists(javaCmd) {
		log.Fatalln("Error: JAVA_HOME is not defined correctly.")
	}
	if javaHome == "" {
		log.Fatalln("You must set the JAVA_HOME variable before running Ballerina.")
	}

	javaOpts := GetJavaOpts()
	ballerinaClasspath := SetBallerinaClassPath(ballerinaHome, javaHome)
	//BALLERINA_CLASSPATH_EXT is for outsiders to additionally add
	//classpath locations, e.g. AWS Lambda function libraries.
	ballerinaClasspathExt := os.Getenv("BALLERINA_CLASSPATH_EXT")
	if ballerinaClasspathExt != "" {
		ballerinaClasspath = ballerinaClasspath + string(filepath.ListSeparator) + ballerinaClasspathExt
	}

	// Define Ballerina CLI Arguments
	cmdLineArgs := []string{
		"-Xms256m",
		"-Xmx1024m",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-classpath", ballerinaClasspath,
		fmt.Sprintf("-Dballerina.home=%s", ballerinaHome),
		fmt.Sprintf("-Dballerina.version=%s", ballerinaVersion),
		"-Denable.nonblocking=false",
		"-Dfile.encoding=UTF8",
		"-Dballerina.target=jvm",
		fmt.Sprintf("-Djava.command=%s", javaCmd),
	}

	if runtime.GOOS == "windows" {
		cmdLineArgs = append(cmdLineArgs, "-Dcom.sun.management.jmxremote")
	} else {
		cmdLineArgs = append(cmdLineArgs, "-Djava.security.egd=file:/dev/./urandom")
	}

	if javaOpts != "" {
		JAVA_OPTS := strings.Fields(javaOpts)
		cmdLineArgs = append(cmdLineArgs, JAVA_OPTS...)
	}

	return cmdLineArgs, javaCmd
}

func ExecuteBallerinaCommand(javaCmd string, cmdLineArgs []string) error {
	cmdArgs := append(cmdLineArgs, "io.ballerina.cli.launcher.Main")
	cmdArgs = append(cmdArgs, os.Args[1:]...)
	err := ExecuteCommand(javaCmd, cmdArgs)
	return err

}

func GenerateTool(javaCmd string, cmdLineArgs []string) {
	err := ExecuteBallerinaCommand(javaCmd, cmdLineArgs)
	if err == nil {
		filePath := generate.FindPathForJson(os.Args[3])
		fmt.Println(filePath)
		if _, err = os.Stat(filePath); err == nil {
			fmt.Println("File exists.")
			fmt.Println(filePath)
			generate.GeneratingCLICommands("/home/wso2/BalWithCobra/config/health.json")
		} else {
			fmt.Println("Could not find the command information json")
		}

	}
}

func removeFileIfExists(filePath string) error {
	if _, err := os.Stat(filePath); err == nil {
		if err := os.Remove(filePath); err != nil {
			return err
		}
		fmt.Println("File deleted successfully.")
	}
	return nil
}

func RemoveTool(javaCmd string, cmdLineArgs []string) {
	err := ExecuteBallerinaCommand(javaCmd, cmdLineArgs)
	if err == nil {
		filepath := filepath.Join("cmd", os.Args[3]+".go")
		fmt.Print(filepath)
		_ = removeFileIfExists(filepath)
	}
}
