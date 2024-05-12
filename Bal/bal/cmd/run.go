/*
Copyright © 2024 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"bal/pkg/utils"
	"fmt"

	"log"

	"strconv"

	"github.com/spf13/cobra"
)

var debugPortStr string

var runCmd = &cobra.Command{
	Use:   "run",
	Short: "A brief description of your command",
	Run: func(cmd *cobra.Command, args []string) {
		debugPortStr, _ := cmd.Flags().GetString("debug")
		var debugPort int
		if debugPortStr != "" {
			port, err := strconv.Atoi(debugPortStr)
			if err != nil {
				fmt.Println("Error: Invalid debug port number specified.")
				return
			}
			debugPort = port
			if debugPort <= 0 || debugPort > 65535 {
				fmt.Println("Error: Invalid debug port number specified.")
				return
			}
		}

		if len(args) >= 1 && utils.IsJarFile(args[0]) {
			if debugPort > 0 {
				fmt.Println("running debug port")
				runDebugMode(args, debugPort)
			} else {
				fmt.Println("running jar file execution")
				runJarExecution(args)
			}
		} else {
			fmt.Println("running Ballerina program")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		}
	},
}

func init() {

	RootCmd.AddCommand(runCmd)
	runCmd.Flags().StringVarP(&debugPortStr, "debug", "", "", "Enable debug mode with the specified port number")
	runCmd.Flags().Bool("offline", false, "Run in offline mode")
	runCmd.Flags().Bool("observability-included", false, "Run with including observability")
	runCmd.Flags().Bool("sticky", false, "Run with including sticky")
	runCmd.Flags().String("target-dir", "", "specify the target directory")
}

func runDebugMode(args []string, debugPort int) {
	cmdArgs := append(cmdLineArgsPass,
		fmt.Sprintf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d", debugPort),
		"-jar",
	)
	cmdArgs = append(cmdArgs, args[0:]...)
	err := utils.ExecuteCommand(javaCmdPass, cmdArgs)
	if err != nil {
		log.Fatalln("Error: executing bal run --debug=<PORT> <*.jar>: ", err)
	}
}

func runJarExecution(args []string) {
	cmdArgs := append(cmdLineArgsPass, "-jar")
	cmdArgs = append(cmdArgs, args[0:]...)
	err := utils.ExecuteCommand(javaCmdPass, cmdArgs)
	if err != nil {
		log.Fatalln("Error: executing bal run <*.jar> :", err)
	}
}
