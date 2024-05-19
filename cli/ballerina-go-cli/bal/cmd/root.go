package cmd

import (
	"bal/pkg/generate"
	"bal/pkg/templates"
	"bal/pkg/utils"
	"os"

	"github.com/spf13/cobra"
)

var (
	javaCmdPass     string
	cmdLineArgsPass []string
	ToolsPass       []*cobra.Command
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

	toolList := generate.GetTools(javaCmdPass, cmdLineArgsPass, RootCmd)
	Tools := templates.CommandGroup{Message: "Tool Commands", Commands: generate.GetCommandsList(toolList, RootCmd)}
	ToolsPass = Tools.Commands

	commandGroups := templates.CommandGroups{
		{Message: "Core Commands", Commands: []*cobra.Command{buildCmd(), runCmd(), testCmd(), docCmd(), packCmd()}},
		{Message: "Package Commands", Commands: []*cobra.Command{newCmd(), addCmd(), pullCmd(), pushCmd(), searchCmd(), semverCmd(), graphCmd(), deprecateCmd()}},
		{Message: "Other Commands", Commands: []*cobra.Command{cleanCmd(), formatCmd(), grpcCmd(), graphqlCmd(), openapiCmd(), asyncapiCmd(), persistCmd(), persistCmd(), bindgenCmd(), shellCmd(), toolCmd(), versionCmd(), profileCmd()}},
		Tools,
	}

	RootCmd.SetHelpFunc(func(cmd *cobra.Command, args []string) {
		if len(args) == 1 || len(args) == 0 {
			templates.Executing_Help_Template(*cmd, commandGroups)
		} else {
			templates.ValidateArgs(args[:len(args)-1], RootCmd, ToolsPass)
		}
	})

}
