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
	"ballerina/executables/utils"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
)

func main() {
	//ballerinaHome := os.Getenv("BALLERINA_HOME")
	ballerinaVersion := "@VERSION@"
	balScriptPath, err := os.Executable()
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

	// Get the directory of the script
	scriptPathDir := filepath.Dir(balScriptPath)
	// Set BALLERINA_HOME
	ballerinaHome, _ := filepath.Abs(filepath.Join(scriptPathDir, ".."))
	javaHome, javaCmd := utils.GetJavaSettings(ballerinaHome)

	if !utils.CommandExists(javaCmd) {
		fmt.Println("Error: JAVA_HOME is not defined correctly.")
		os.Exit(1)
	}
	if javaHome == "" {
		fmt.Println("You must set the JAVA_HOME variable before running Ballerina.")
		os.Exit(1)
	}

	utils.SetBallerinaCLIWidth()
	javaOpts := utils.GetJavaOpts()
	ballerinaClasspath := utils.SetBallerinaClassPath(ballerinaHome, javaHome)
	//BALLERINA_CLASSPATH_EXT is for outsiders to additionally add
	//classpath locations, e.g. AWS Lambda function libraries.
	ballerinaClasspathExt := os.Getenv("BALLERINA_CLASSPATH_EXT")
	if ballerinaClasspathExt != "" {
		ballerinaClasspath = ballerinaClasspath + string(filepath.ListSeparator) + ballerinaClasspathExt
	}
	//Define Ballerina CLI Arguments
	cmdLineArgs := []string{
		"-Xms@XMS_VAL@",
		"-Xmx@XMX_VAL@",
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
	noOfArgsInBalRunDebugWithEqual := 4
	noOfArgsInBalRunDebugWithoutEqual := 5
	if len(os.Args) >= 3 && os.Args[1] == "run" { //check whether the user has given bal run command with other arguments
		if utils.IsJarFile(os.Args[2]) { //Implementation of bal run <*.jar>
			cmdArgs := append(cmdLineArgs, "-jar")
			cmdArgs = append(cmdArgs, os.Args[2:]...)
			err = utils.ExecuteCommand(javaCmd, cmdArgs)
			if err != nil {
				log.Fatalln("Error: executing bal run <*.jar> :", err)
			}
			os.Exit(0)
		} else if len(os.Args) == noOfArgsInBalRunDebugWithEqual && utils.IsJarFile(os.Args[3]) && os.Args[2][:8] == "--debug=" {
			debugPort := utils.ExtractDebugPort(os.Args[2]) //Implementation of bal run --debug=<PORT> <*.jar>
			if utils.ValidateDebugPort(debugPort) {
				cmdArgs := append(cmdLineArgs,
					fmt.Sprintf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d", debugPort),
					"-jar",
					os.Args[3],
				)
				err = utils.ExecuteCommand(javaCmd, cmdArgs)
				if err != nil {
					log.Fatalln("Error: executing bal run --debug=<PORT> <*.jar>: ", err)
				}
				os.Exit(0)
			} else {
				log.Println("Error: Invalid debug port number specified.")
				os.Exit(1)
			}
		} else if len(os.Args) == noOfArgsInBalRunDebugWithoutEqual && os.Args[2] == "--debug" && utils.IsJarFile(os.Args[4]) {
			debugPort, err := strconv.Atoi(os.Args[3]) // handles "bal run --debug <PORT> <JAR_PATH>" command
			if err != nil {
				debugPort = 0
			}
			if utils.ValidateDebugPort(debugPort) {
				cmdArgs := append(cmdLineArgs,
					fmt.Sprintf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d", debugPort),
					"-jar")
				cmdArgs = append(cmdArgs, os.Args[4:]...)
				err = utils.ExecuteCommand(javaCmd, cmdArgs)
				if err != nil {
					log.Fatalln("Error: executing bal run --debug <PORT> <JAR_PATH>: ", err)
				}
				os.Exit(0)
			} else {
				log.Println("Error: Invalid debug port number specified.")
				os.Exit(1)
			}

		} else {
			cmdArgs := append(cmdLineArgs, "io.ballerina.cli.launcher.Main")
			cmdArgs = append(cmdArgs, os.Args[1:]...)
			err := utils.ExecuteCommand(javaCmd, cmdArgs)
			if err != nil {
				os.Exit(1)
			}
			os.Exit(0)
		}
	} else {
		cmdArgs := append(cmdLineArgs, "io.ballerina.cli.launcher.Main")
		cmdArgs = append(cmdArgs, os.Args[1:]...)
		err := utils.ExecuteCommand(javaCmd, cmdArgs)
		if err != nil {
			os.Exit(1)
		}
		os.Exit(0)
	}
}
