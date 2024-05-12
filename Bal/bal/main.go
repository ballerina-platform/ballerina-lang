/*
Copyright © 2024 NAME HERE <EMAIL ADDRESS>
*/
package main

import (
	"bal/cmd"
)

func main() {
	cmd.Execute()
	// subCommands := make([]string, 0)
	// for _, cmd := range cmd.RootCmd.Commands() {
	// 	subCommands = append(subCommands, cmd.Use)
	// }
	// fmt.Println("Subcommands appended to the slice:")
	// fmt.Println(subCommands)

	//generate.GeneratingCLICmd("/home/wso2/BalWithCobra/config/health.json")
}

// Call it with the root command and an empty prefix
