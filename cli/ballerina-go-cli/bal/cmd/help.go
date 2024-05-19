package cmd

import (
	"bal/pkg/templates"

	"github.com/spf13/cobra"
)

var helpcmd *cobra.Command

func helpCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "help [command]",
		Short: "Help about any command",
		Long:  `Help provides help for any command in the application.`,
		Run: func(cmd *cobra.Command, args []string) {
			customHelpFunc(cmd, args)
		},
	}
}

func init() {
	helpcmd = helpCmd()
	RootCmd.AddCommand(helpcmd)
}

func customHelpFunc(cmd *cobra.Command, args []string) {
	if len(args) == 0 {
		templates.Executing_Help_Template(*cmd, commandGroups)
	} else {
		templates.ValidateArgs(args, RootCmd, ToolsPass)
	}
}
