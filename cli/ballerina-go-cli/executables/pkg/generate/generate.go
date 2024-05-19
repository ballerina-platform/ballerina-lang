package generate

import (
	"bytes"
	"fmt"
	"go/format"
	"log"
	"os"
	"path/filepath"
	"text/template"

	"github.com/spf13/cast"
	"github.com/spf13/viper"
)

type CommandConfig struct {
	Name     string
	Short    string
	Function string
}

type FlagConfig struct {
	Name       string
	Usage      string
	Shorthend  string
	DefaultVal interface{}
	Param      string
}

const templateContent = `
package cmd

import (
	"bal/pkg/utils"
	"log"
	"path/filepath"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)
var {{.Name}}cmd *cobra.Command
func {{.Name}}Cmd() *cobra.Command{
	cmd := &cobra.Command{
	Use:     "{{.Name}}",
	Short:   "{{.Short}}",
	Long:   "",
	Example: "",
	Run: func(cmd *cobra.Command, args []string) {
		{{.Function}}
	},
}
	viper.SetConfigName("{{.Name}}")
	scriptPathDir:=utils.GetBalScriptDir()
	configDir:=filepath.Join(scriptPathDir,"..","resources","command-info")
	viper.AddConfigPath(configDir)
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	if long := viper.GetString("help.base.long"); long != "" {
		cmd.Long = long
	}
	if examples := viper.GetString("help.base.examples"); examples != "" {
		cmd.Example = examples
	}

	{{.FlagLines}}

	return cmd

}

func init() {
	{{.Name}}cmd= {{.Name}}Cmd()
	RootCmd.AddCommand({{.Name}}cmd)
	{{.SubLines}}

}

{{.SubCommands}}
`

func generateFlagLine(flag FlagConfig, cmdName string) string {

	var flagline string

	switch flag.DefaultVal.(type) {
	case bool:
		if flag.Shorthend == "" {
			flagline = fmt.Sprintf("%s.Flags().Bool(\"%s\", %t, \"%s\")", cmdName, flag.Name, flag.DefaultVal.(bool), flag.Usage)
		} else {
			flagline = fmt.Sprintf("%s.Flags().BoolP(\"%s\", \"%s\", %t, \"%s\")", cmdName, flag.Name, flag.Shorthend, flag.DefaultVal.(bool), flag.Usage)
		}
	case string:
		if flag.Shorthend == "" {
			flagline = fmt.Sprintf("%s.Flags().String(\"%s\", \"%s\", \"%s\")", cmdName, flag.Name, flag.DefaultVal.(string), flag.Usage)
		} else {
			flagline = fmt.Sprintf("%s.Flags().StringP(\"%s\", \"%s\", \"%s\", \"%s\")", cmdName, flag.Name, flag.Shorthend, flag.DefaultVal.(string), flag.Usage)
		}
	case int:
		flagline = fmt.Sprintf("%s.Flags().IntP(\"%s\", \"%s\", %d, \"%s\")", cmdName, flag.Name, flag.Shorthend, flag.DefaultVal.(int), flag.Usage)
	}

	return flagline
}

func generateSubCommands(subcommands []interface{}, config CommandConfig) (string, string) {
	const subCommandTemp = `
	var {{.Name}}cmd *cobra.Command
	func {{.Name}}Cmd() *cobra.Command {
		cmd := &cobra.Command{
		Use:   "{{.Use}}",
		Short: "{{.Short}}",
		Long:  "",
		Example:"",
		Run: func(cmd *cobra.Command, args []string) {
			{{.Function}}
		},
	}
		{{.SubFlagLines}}
		viper.SetConfigName("{{.Base}}")
		scriptPathDir:=utils.GetBalScriptDir()
		configDir:=filepath.Join(scriptPathDir,"..","resources","command-info")
		viper.AddConfigPath(configDir)
		viper.SetConfigType("toml")

		if err := viper.ReadInConfig(); err != nil {
			log.Fatalf("Error reading config file: %s", err)
		}

		if long := viper.GetString("help.{{.Use}}.long"); long != "" {
			cmd.Long = long
		}
		if examples := viper.GetString("help.{{.Use}}.examples"); examples != "" {
			cmd.Example = examples
		}

		return cmd
	}
	`
	var subCommandsStr string
	var subLinesStr string

	for _, subcmd := range subcommands {
		if m, ok := subcmd.(map[string]interface{}); ok {
			subconfig := CommandConfig{
				Name:     cast.ToString(m["name"]),
				Short:    cast.ToString(m["short"]),
				Function: cast.ToString(m["function"]),
			}
			function := ""
			if subconfig.Function == "" {
				function = "_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)"
			} else {
				function = subconfig.Function
			}

			subflags, _ := viper.Get(fmt.Sprintf("%s.flag", subconfig.Name)).([]interface{})
			subflagLines := generateFlagLines(subflags, generateFlagLine)

			tmplSubcmd := template.Must(template.New("SubcommandTemplate").Parse(subCommandTemp))
			var subcmdStrBuffer bytes.Buffer
			data := struct {
				Name         string
				Use          string
				Short        string
				Function     string
				SubFlagLines string
				Base         string
			}{
				Name:         subconfig.Name + config.Name,
				Use:          subconfig.Name,
				Short:        subconfig.Short,
				Function:     function,
				SubFlagLines: subflagLines,
				Base:         config.Name,
			}
			if err := tmplSubcmd.Execute(&subcmdStrBuffer, data); err != nil {
				log.Fatalf("Error executing subcommand template: %s", err)
			}
			subCommandsStr += subcmdStrBuffer.String() + "\n"
			subassign := fmt.Sprintf("%scmd = %sCmd()", data.Name, data.Name)
			subline := fmt.Sprintf("%scmd.AddCommand(%scmd)", config.Name, data.Name)
			subLinesStr += subassign + "\n" + subline + "\n"
		}
	}

	return subCommandsStr, subLinesStr
}

func generateFlagLines(flags []interface{}, generateFlagLine func(FlagConfig, string) string) string {
	var flagLines string

	for _, table := range flags {
		if m, ok := table.(map[string]interface{}); ok {
			subflag := FlagConfig{
				Name:       cast.ToString(m["name"]),
				Usage:      cast.ToString(m["usage"]),
				DefaultVal: m["default_val"],
				Shorthend:  cast.ToString(m["shorthand"]),
			}
			flagline := generateFlagLine(subflag, "cmd")
			flagLines += flagline + "\n"
		}
	}

	return flagLines
}

func GeneratingCLICommands(path string, name string, commandPath string) {

	viper.SetConfigName(name)
	viper.AddConfigPath(path)
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}
	baseFunction := viper.GetString("base_command.function")
	if baseFunction == "" {
		baseFunction = "_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)"
	}
	config := CommandConfig{
		Name:     viper.GetString("base_command.name"),
		Short:    viper.GetString("base_command.short"),
		Function: baseFunction,
	}
	filename := config.Name + ".go"
	filePath := filepath.Join(commandPath, filename)
	file, err := os.Create(filePath)
	if err != nil {
		log.Fatalf("Error creating command.go file: %s", err)
	}
	defer file.Close()
	tmpl := template.Must(template.New("commandTemplate").Parse(templateContent))
	flags, _ := viper.Get("base_command.flag").([]interface{})
	flagLines := generateFlagLines(flags, generateFlagLine)
	subcommands, _ := viper.Get("base_command.subcommand").([]interface{})
	subCommandsStr, subLinesStr := generateSubCommands(subcommands, config)

	data := struct {
		Name        string
		Short       string
		Function    string
		FlagLines   string
		SubCommands string
		SubLines    string
	}{
		Name:        config.Name,
		Short:       config.Short,
		Function:    config.Function,
		FlagLines:   flagLines,
		SubCommands: subCommandsStr,
		SubLines:    subLinesStr,
	}

	var commandData bytes.Buffer

	if err := tmpl.Execute(&commandData, data); err != nil {
		log.Fatalf("Error executing template: %s", err)
	}
	output := commandData.String() //format the content of the document
	formattedContent, err := format.Source([]byte(output))
	if err != nil {
		log.Fatalf("Error formatting Go code: %s", err)
	}
	if err := os.WriteFile(filePath, formattedContent, 0644); err != nil {
		log.Fatalf("Error writing to file: %s", err)
	}
}
