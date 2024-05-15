package templates

import (
	"bal/pkg/generate"
	"bal/pkg/utils"
	"fmt"
	"log"
	"path/filepath"

	"github.com/spf13/cobra"
)

func ValidateArgs(args []string, rootCmd *cobra.Command, cmdArr []*cobra.Command) {
	var foundCmd *cobra.Command
	var err error
	var helpField string
	var flagField string
	name := args[0]

	if len(args) == 1 {
		if foundCmd, _, err = rootCmd.Find([]string{args[0]}); err != nil {
			log.Fatal("'" + args[0] + "' is not a valid command.")
		} else if foundCmd != nil {
			helpField = "help.base.synopsis"
			flagField = "base_command.flag"
		}
	}

	if len(args) >= 2 {
		for i := 1; i < len(args); i++ {
			cmdPath := args[0 : i+1]
			foundCmd, _, err = rootCmd.Find(cmdPath)
			if err != nil || foundCmd == nil || foundCmd.Name() != cmdPath[len(cmdPath)-1] {
				log.Fatalln("'" + cmdPath[len(cmdPath)-1] + "' is not a valid subcommand of '" + cmdPath[len(cmdPath)-2] + "'.")
			}
		}
		if foundCmd != nil {
			helpField = fmt.Sprintf("help.%s.synopsis", args[len(args)-1])
			flagField = fmt.Sprintf("%s.flag", args[1])
		}
	}
	processCommand(foundCmd, name, helpField, flagField, cmdArr)
}

// Implement logic to find the path and reurn the path and type
func FindPathHelp(cmd *cobra.Command, cmdArr []*cobra.Command, base string) (string, string) {
	for _, v := range cmdArr {
		if v == cmd {
			path := generate.FindPathForJson(base)
			return path, "json"
		}
	}
	scriptPathDir := utils.GetBalScriptDir()
	path := filepath.Join(scriptPathDir, "..", "resources", "command-info")

	return path, "toml"
}

func processCommand(foundCmd *cobra.Command, name string, field string, fieldFlag string, cmdArr []*cobra.Command) {
	path, filetype := FindPathHelp(foundCmd, cmdArr, name)
	configInfo := ConfigFileInfo{
		Name:     name,
		Path:     path,
		FileType: filetype,
	}
	cmdData := GetCommandData(foundCmd, field, configInfo, fieldFlag)
	FillTemplate(cmdData)
}
