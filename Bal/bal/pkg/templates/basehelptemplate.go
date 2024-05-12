package templates

import (
	"fmt"
	"os"
	"text/template"

	"github.com/spf13/cobra"
)

// CommandInfo holds information about a command
type CommandInfo struct {
	Use   string
	Short string
}

// CommandGroup holds a group of commands
type CommandGroup struct {
	Message  string
	Commands []*cobra.Command
}

// CommandGroups holds multiple CommandGroup instances
type CommandGroups []CommandGroup

// Executing_Help_Template generates and executes a custom help template
func Executing_Help_Template(rootCmd cobra.Command, commandGroups CommandGroups) {
	customHelpTemplate := `
NAME:
   {{.Base_Short}}

SYNOPSIS:
   bal <command> [args]
   bal [OPTIONS]

OPTIONS:
   -v, --version
       Print version information.

   -h, --help
       Print the usage details of a command.

COMMANDS:
   The available subcommands are:

   {{range .CommandGroups}}
   {{.Message}}:
   {{range .Commands}}
     {{.Use | printf "%-15s"}} {{.Short}}
   {{end}}
   {{end}}

Use 'bal help <command>' for more information on a specific command.
`

	// Create and parse the template
	tmpl, err := template.New("customHelpTemplate").Parse(customHelpTemplate)
	if err != nil {
		fmt.Println(err)
		return
	}

	// Data for filling out the template
	data := struct {
		Base_Short    string
		CommandGroups CommandGroups
	}{
		Base_Short:    rootCmd.Short,
		CommandGroups: commandGroups,
	}

	// Execute the template
	err = tmpl.Execute(os.Stdout, data)
	if err != nil {
		fmt.Println(err)
	}
}
