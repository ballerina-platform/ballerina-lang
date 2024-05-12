package templates

import (
	"bytes"
	"log"
	"os"
	"text/template"

	"github.com/spf13/cobra"
	"github.com/spf13/pflag"
)

const subcommandTemplate = `
NAME:
       ballerina-{{.BName}} - {{.BShort}}

SYNOPSIS:
      {{.Synopsis}}

DESCRIPTION:
      {{.Long}}

OPTIONS:
	{{.Options}}

{{if .Commands}}
SUBCOMMANDS:
{{range .Commands}}
       {{.Name}}        {{.Short}}
{{end}}
{{end}}

EXAMPLES:
	{{.Examples}}

{{if .Commands}}Use 'bal {{.BName}} <subcommand> --help' for more information on a specific tool subcommand.{{end}}
`

const FlagTemplate = `
	{{if .ShortForm}}-{{.ShortForm}}, {{end}}{{if .Param}}--{{.LongForm}} <{{.Param}}>{{else}}--{{.LongForm}}{{end}}
		{{.Usage}}

`

type CommandData struct {
	BName    string
	BShort   string
	Synopsis string
	Long     string
	Options  string
	Commands []CommandInfo
	Examples string
}

type OptionData struct {
	ShortForm string
	LongForm  string
	Param     string
	Usage     string
}

func FillTemplate(commandData CommandData) {
	tmpl, err := template.New("subHelpTemplate").Parse(subcommandTemplate)
	if err != nil {
		log.Fatalln("Error parsing template:", err)
	}

	err = tmpl.Execute(os.Stdout, commandData)
	if err != nil {
		log.Fatalln("Error executing template:", err)
	}
}

func GetSubCommands(cmd *cobra.Command) []CommandInfo {
	if len(cmd.Commands()) == 0 {
		return nil
	}

	var commands []CommandInfo
	for _, subCmd := range cmd.Commands() {
		commands = append(commands, CommandInfo{
			Use:   subCmd.Use,
			Short: subCmd.Short,
		})
	}
	return commands
}

func GetCommandData(cmd *cobra.Command) CommandData {
	return CommandData{
		BName:    cmd.Name(),
		BShort:   cmd.Short,
		Synopsis: cmd.Short,
		Long:     cmd.Long,
		Options:  "No options available",
		Commands: GetSubCommands(cmd),
		Examples: cmd.Example,
	}
}

func FillOptions(cmd *cobra.Command, path string) string {
	var options string
	flags := cmd.Flags()
	tmpl, err := template.New("flagTemplate").Parse(FlagTemplate)
	if err != nil {
		log.Fatalln("Error parsing template:", err)
	}

	flags.VisitAll(func(flag *pflag.Flag) {
		var buf bytes.Buffer
		err := tmpl.Execute(&buf, OptionData{
			ShortForm: flag.Shorthand,
			LongForm:  flag.Name,
			Param:     flag.Value.Type(),
			Usage:     flag.Usage,
		})
		if err != nil {
			log.Fatalln("Error executing template:", err)
		}
		options += buf.String()
	})
	return options
}
