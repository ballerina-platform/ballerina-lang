package templates

import (
	"bytes"
	"fmt"
	"log"
	"os"
	"text/template"

	"github.com/spf13/cast"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

type SubCommandInfo struct {
	Name  string
	Short string
}

type ConfigFileInfo struct {
	Path     string
	FileType string
}

const subcommandTemplate = `
NAME:
       ballerina-{{.BName}} - {{.BShort}}

SYNOPSIS:
      {{.Synopsis}}

DESCRIPTION:
      {{.Long}}

OPTIONS:
	{{.Options}}

{{- if .Commands}}
SUBCOMMANDS:
{{range .Commands}}
       {{.Name}}        {{.Short}}
{{end}}
{{end -}}

{{- if .Examples}}
EXAMPLES:
	{{.Examples}}
{{end}}

{{if .Commands}}Use 'bal {{.BName}} <subcommand> --help' for more information on a specific tool subcommand.{{end}}
`

const FlagTemplate = `	{{if .ShortForm}}-{{.ShortForm}}, {{end}}{{if .Param}}--{{.LongForm}} <{{.Param}}>{{else}}--{{.LongForm}}{{end}}
		{{.Usage}}

`

type CommandData struct {
	BName    string
	BShort   string
	Synopsis string
	Long     string
	Options  string
	Commands []SubCommandInfo
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

func GetSubCommands(cmd *cobra.Command) []SubCommandInfo {
	if len(cmd.Commands()) == 0 {
		return nil
	}

	var commands []SubCommandInfo
	for _, subCmd := range cmd.Commands() {
		commands = append(commands, SubCommandInfo{
			Name:  subCmd.Use,
			Short: subCmd.Short,
		})
	}

	return commands
}

func GetCommandData(cmd *cobra.Command, field string, configInfo ConfigFileInfo, flagfield string, name string) CommandData {
	viper.SetConfigFile(configInfo.Path)
	viper.SetConfigType(configInfo.FileType)
	return CommandData{
		BName:    name,
		BShort:   cmd.Short,
		Synopsis: GetSynopsis(field),
		Long:     cmd.Long,
		Options:  FillOptions(flagfield, cmd),
		Commands: GetSubCommands(cmd),
		Examples: cmd.Example,
	}
}

func FillOptions(field string, cmd *cobra.Command) string {
	flags, _ := viper.Get(field).([]interface{})
	var options string
	tmpl, err := template.New("flagTemplate").Parse(FlagTemplate)
	if err != nil {
		log.Fatalln("Error parsing template:", err)
	}

	for _, table := range flags {
		if m, ok := table.(map[string]interface{}); ok {

			var buf bytes.Buffer
			err := tmpl.Execute(&buf, OptionData{
				ShortForm: cast.ToString(m["shorthand"]),
				LongForm:  cast.ToString(m["name"]),
				Param:     cast.ToString(m["param"]),
				Usage:     cast.ToString(m["usage"]),
			})
			if err != nil {
				log.Fatalln("Error executing template:", err)
			}
			options += buf.String()

		}
	}
	// Manually add the help flag
	var buf bytes.Buffer
	err = tmpl.Execute(&buf, OptionData{
		ShortForm: "h",
		LongForm:  "help",
		Param:     "",
		Usage:     fmt.Sprintf("Print the usage details of %s command. ", cmd.Name()),
	})
	if err != nil {
		log.Fatalln("Error executing template:", err)
	}
	options += buf.String()

	return options
}

func GetSynopsis(field string) string {
	synopsis := ""
	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading synopsis config file: %s", err)
	}
	if viper.GetString(field) != "" {
		synopsis = viper.GetString(field)
	}
	return synopsis
}
