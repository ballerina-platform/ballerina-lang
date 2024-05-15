package generate

import (
	"bytes"
	"fmt"
	"go/format"
	"log"
	"os"
	"os/user"
	"path/filepath"
	"text/template"

	"github.com/spf13/cast"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

type CommandConfig struct {
	Name     string
	Short    string
	Function string
}

type Subcommand struct {
	Name     string          `json:"name"`
	Short    string          `json:"short"`
	Function string          `json:"function"`
	Flags    ([]interface{}) `json:"flags"`
}

type FlagConfig struct {
	Name       string
	Usage      string
	Shorthend  string
	DefaultVal interface{}
	Param      string
}
type ToolData struct {
	Id         string
	Org        string
	Name       string
	Active     bool
	Version    string
	Repocitory string
}

const templateContent = `
package cmd

import (
	"bal/pkg/generate"
	"bal/pkg/utils"
	"log"

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
path:=generate.FindPathForJson("{{.Name}}")
viper.SetConfigType("json")
viper.SetConfigFile(path)

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
		path:=generate.FindPathForJson("{{.Base}}")
		viper.SetConfigType("json")
		viper.SetConfigFile(path)

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
			subflagLines := generateSubFlagLines(subflags, generateFlagLine)

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

func generateSubFlagLines(subflags []interface{}, generateFlagLine func(FlagConfig, string) string) string {
	var subflagLines string

	for _, table := range subflags {
		if m, ok := table.(map[string]interface{}); ok {
			subflag := FlagConfig{
				Name:       cast.ToString(m["name"]),
				Usage:      cast.ToString(m["usage"]),
				DefaultVal: m["default_val"],
				Shorthend:  cast.ToString(m["shorthand"]),
			}
			subflagline := generateFlagLine(subflag, "cmd")
			subflagLines += subflagline + "\n"
		}
	}

	return subflagLines
}

func generatingBaseCommandFlags(flags []interface{}, name string) string {
	var flagLines string
	for _, table := range flags {
		if m, ok := table.(map[string]interface{}); ok {
			flag := FlagConfig{
				Name:       cast.ToString(m["name"]),
				Usage:      cast.ToString(m["usage"]),
				DefaultVal: m["default_val"],
				Shorthend:  cast.ToString(m["shorthand"]),
			}
			flagline := generateFlagLine(flag, name)
			flagLines += flagline + "\n"
		}
	}
	return flagLines
}

func GeneratingCLICommands(path string) {

	viper.SetConfigType("json")
	viper.SetConfigFile(path)

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	config := CommandConfig{
		Name:     viper.GetString("tool_id"),
		Short:    viper.GetString("short"),
		Function: "_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)",
	}

	filename := config.Name + ".go"
	fmt.Println(filename)
	file, err := os.Create(filepath.Join("cmd", filename))
	if err != nil {
		log.Fatalf("Error creating command.go file: %s", err)
	}
	defer file.Close()
	tmpl := template.Must(template.New("commandTemplate").Parse(templateContent))
	flags, _ := viper.Get("base_command.flag").([]interface{})
	flagLines := generatingBaseCommandFlags(flags, "cmd")
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
	fmt.Println(output)
	formattedContent, err := format.Source([]byte(output))
	if err != nil {
		log.Fatalf("Error formatting Go code: %s", err)
	}
	if err := os.WriteFile(filepath.Join("cmd", filename), formattedContent, 0644); err != nil {
		log.Fatalf("Error writing to file: %s", err)
	}
}

func FindPathForJson(toolName string) string {
	jasonPath := ""
	currentUser, _ := user.Current()
	balToolPath := filepath.Join(currentUser.HomeDir, ".ballerina", ".config")
	fmt.Println("Configuration path:", balToolPath)

	viper.SetConfigName("bal-tools")
	viper.AddConfigPath(balToolPath)
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		fmt.Println("Error reading config file:", err)
	}

	toolDetails, ok := viper.Get("tool").([]interface{})
	if !ok {
		fmt.Println("Error reading tool details from config")
	}
	for _, table := range toolDetails {
		if m, ok := table.(map[string]interface{}); ok {
			if cast.ToString(m["id"]) == toolName && cast.ToBool(m["active"]) {
				toolData := ToolData{
					Id:         cast.ToString(m["id"]),
					Org:        cast.ToString(m["org"]),
					Name:       cast.ToString(m["name"]),
					Active:     cast.ToBool(m["active"]),
					Version:    cast.ToString(m["version"]),
					Repocitory: cast.ToString(m["repocitory"]),
				}
				repocitoryType := ""
				if toolData.Repocitory == "" {
					repocitoryType = "central.ballerina.io"
				} else {
					repocitoryType = toolData.Repocitory
				}
				jasonPath = filepath.Join(currentUser.HomeDir, ".ballerina", "repositories", repocitoryType, "bala", toolData.Org, toolData.Name, toolData.Version, "java17", "tool", "bal-tool.json")
				break
			}
		}
	}

	if jasonPath == "" {
		fmt.Println("Tool not found or not active in config")
	}

	return jasonPath
}

func GetListOfTools() []string {
	currentUser, _ := user.Current()
	balToolPath := filepath.Join(currentUser.HomeDir, ".ballerina", ".config")
	fmt.Println("Configuration path:", balToolPath)

	viper.SetConfigName("bal-tools")
	viper.AddConfigPath(balToolPath)
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		fmt.Println("Error reading config file:", err)
	}

	toolDetails, ok := viper.Get("tool").([]interface{})
	if !ok {
		fmt.Println("Error reading tool details from config")
	}
	toolList := []string{}
	for _, table := range toolDetails {
		if m, ok := table.(map[string]interface{}); ok {
			if cast.ToBool(m["active"]) {
				toolList = append(toolList, cast.ToString(m["id"]))
			}
		}
	}
	return toolList
}

func GetCommandsList(names []string, rootCmd *cobra.Command) []*cobra.Command {
	var commands []*cobra.Command
	allCommands := rootCmd.Commands()
	for _, cmd := range allCommands {
		for _, name := range names {
			if cmd.Use == name {
				commands = append(commands, cmd)
			}
		}
	}
	return commands
}
