package cmd

import (
	"bal/pkg/utils"
	"os"

	"github.com/spf13/cobra"
)

var (
	javaCmdPass     string
	cmdLineArgsPass []string
)

var RootCmd = &cobra.Command{
	Use:   "bal",
	Short: "The build system and package manager of Ballerina",
	Run: func(cmd *cobra.Command, args []string) {
		_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
	},
}

func Execute() {
	err := RootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
}

func init() {

	RootCmd.Flags().BoolP("version", "v", false, "Print version information.")
	cmdLineArgsPass, javaCmdPass = utils.Setup()

	// flags := buildCmd.Flags()
	// flags.VisitAll(func(flag *pflag.Flag) {
	// 	fmt.Printf("Long: %s, Short: %s, Usage: %s\n", flag.Name, flag.Shorthand, flag.Usage)
	// })
	// Accessing flags using Cobra

	// commandGroups := templates.CommandGroups{
	// 	{Message: "Core Commands", Commands: []*cobra.Command{runCmd, buildCmd}},
	// 	{Message: "Package Commands", Commands: []*cobra.Command{newCmd, addCmd}},
	// 	{Message: "Other Commands", Commands: []*cobra.Command{cleanCmd, formatCmd, versionCmd, pullCmd}},
	// }

	// RootCmd.SetHelpFunc(func(cmd *cobra.Command, args []string) {
	// 	fmt.Println("Executing custom help template")
	// 	templates.Executing_Help_Template(*cmd, commandGroups)
	// })

}
