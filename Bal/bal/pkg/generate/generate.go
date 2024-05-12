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
	"github.com/spf13/viper"
)

type CommandConfig struct {
	Name  string
	Short string
	Long  string
}
type FlagConfig struct {
	Name       string
	Usage      string
	Shorthend  string
	DefaultVal interface{}
}

type ToolData struct {
	Id         string
	Org        string
	Name       string
	Active     bool
	Version    string
	Repocitory string
}

func GenerateFlagLine(flag FlagConfig, cmdName string) string {
	if cmdName == "" {
		cmdName = "RootCmd"
	}

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

func GenerateSubCommands(subcommands []interface{}, config CommandConfig) (string, string) {
	const subCommandTemp = `
	var {{.Name}} = &cobra.Command{
		Use:   "{{.Use}}",
		Short: "{{.Short}}",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("search called")
			executeBallerinaCommand()
		},
	}
	`
	var subCommandsStr string
	var subLinesStr string

	for _, subcmd := range subcommands {
		if m, ok := subcmd.(map[string]interface{}); ok {
			subconfig := CommandConfig{
				Name:  cast.ToString(m["name"]),
				Short: cast.ToString(m["short"]),
				Long:  cast.ToString(m["description"]),
			}
			tmplSubcmd := template.Must(template.New("SubcommandTemplate").Parse(subCommandTemp))
			var subcmdStrBuffer bytes.Buffer
			data := struct {
				Name  string
				Use   string
				Short string
			}{
				Name:  subconfig.Name + config.Name + "Cmd",
				Use:   subconfig.Name,
				Short: subconfig.Short,
			}
			if err := tmplSubcmd.Execute(&subcmdStrBuffer, data); err != nil {
				log.Fatalf("Error executing subcommand template: %s", err)
			}
			subCommandsStr += subcmdStrBuffer.String() + "\n"
			subline := fmt.Sprintf("%sCmd.AddCommand(%s)", config.Name, data.Name)
			subLinesStr += subline + "\n"
		}
	}

	return subCommandsStr, subLinesStr
}

func GenerateSubFlagLines(subflags []interface{}, config CommandConfig, generateFlagLine func(FlagConfig, string) string) string {
	var subflagLines string

	for _, table := range subflags {
		if m, ok := table.(map[string]interface{}); ok {
			subflag := FlagConfig{
				Name:       cast.ToString(m["name"]),
				Usage:      cast.ToString(m["usage"]),
				DefaultVal: m["default_val"],
				Shorthend:  cast.ToString(m["shorthand"]),
			}
			commandName := cast.ToString(m["command"]) + config.Name + "Cmd"
			subflagline := generateFlagLine(subflag, commandName)
			subflagLines += subflagline + "\n"
		}
	}

	return subflagLines
}

func GeneratingBaseCommandFlags(flags []interface{}, name string) string {
	var flagLines string
	for _, table := range flags {
		if m, ok := table.(map[string]interface{}); ok {
			flag := FlagConfig{
				Name:       cast.ToString(m["name"]),
				Usage:      cast.ToString(m["usage"]),
				DefaultVal: m["default_val"],
				Shorthend:  cast.ToString(m["shorthand"]),
			}
			flagline := GenerateFlagLine(flag, name)
			flagLines += flagline + "\n"
		}
	}
	return flagLines
}

func GeneratingCLICommands(path string, name string) {

	viper.SetConfigName(name)
	viper.AddConfigPath(path)
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	config := CommandConfig{
		Name:  viper.GetString("base_command.name"),
		Short: viper.GetString("base_command.short"),
		Long:  viper.GetString("base_command.long"),
	}
	filename := config.Name + ".go"
	fmt.Println(filename)
	file, err := os.Create(filepath.Join("cmd", filename))
	if err != nil {
		log.Fatalf("Error creating command.go file: %s", err)
	}
	defer file.Close()

	const templateContent = `
package cmd

import (
	"fmt"
	"github.com/spf13/cobra"
)

var {{.Name}}Cmd = &cobra.Command{
	Use:     "{{.Name}}",
	Short:   "{{.Short}}",
	Long:    "{{.Long}}",
	Run: func(cmd *cobra.Command, args []string) {
		executeBallerinaCommand()
		fmt.Println("running")
	},
}

func init() {
	RootCmd.AddCommand({{.Name}}Cmd)
	//Add subcommands
	{{.SubLines}}
	{{ .FlagLines }}
	// Add subcommand flags here
	{{.SubFlagLines}}

}

{{.SubCommands}}
`

	tmpl := template.Must(template.New("commandTemplate").Parse(templateContent))
	flags, _ := viper.Get("base_command.flag").([]interface{})
	nameCmd := config.Name + "Cmd"
	flagLines := GeneratingBaseCommandFlags(flags, nameCmd)
	subcommands, _ := viper.Get("base_command.subcommand").([]interface{})
	subCommandsStr, subLinesStr := GenerateSubCommands(subcommands, config)
	subflags, _ := viper.Get("base_command.subcommand_flag").([]interface{})
	subflagLines := GenerateSubFlagLines(subflags, config, GenerateFlagLine)

	data := struct {
		Name         string
		Short        string
		Long         string
		FlagLines    string
		SubCommands  string
		SubLines     string
		SubFlagLines string
	}{
		Name:         config.Name,
		Short:        config.Short,
		Long:         config.Long,
		FlagLines:    flagLines,
		SubCommands:  subCommandsStr,
		SubLines:     subLinesStr,
		SubFlagLines: subflagLines,
	}

	var commandData bytes.Buffer

	if err := tmpl.Execute(&commandData, data); err != nil {
		log.Fatalf("Error executing template: %s", err)
	}
	output := commandData.String()
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

func GeneratingCLICmd(path string) {

	viper.SetConfigType("json")
	viper.SetConfigFile(path)

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	config := CommandConfig{
		Name:  viper.GetString("tool_id"),
		Short: viper.GetString("short"),
		Long:  viper.GetString("long"),
	}

	filename := config.Name + ".go"
	fmt.Println(filename)
	file, err := os.Create(filepath.Join("cmd", filename))
	if err != nil {
		log.Fatalf("Error creating command.go file: %s", err)
	}
	defer file.Close()

	const templateContent = `
package cmd

import (
	"fmt"
	"github.com/spf13/cobra"
)

var {{.Name}}Cmd = &cobra.Command{
	Use:     "{{.Name}}",
	Short:   "{{.Short}}",
	Long:    "{{.Long}}",
	Run: func(cmd *cobra.Command, args []string) {
		executeBallerinaCommand()
		fmt.Println("running")
	},
}

func init() {
	RootCmd.AddCommand({{.Name}}Cmd)
	//Add subcommands
	{{.SubLines}}
	{{ .FlagLines }}
	// Add subcommand flags here
	{{.SubFlagLines}}

}

{{.SubCommands}}
`

	tmpl := template.Must(template.New("commandTemplate").Parse(templateContent))
	flags, _ := viper.Get("flag").([]interface{})
	nameCmd := config.Name + "Cmd"
	flagLines := GeneratingBaseCommandFlags(flags, nameCmd)
	subcommands, _ := viper.Get("subcommand").([]interface{})
	subCommandsStr, subLinesStr := GenerateSubCommands(subcommands, config)
	subflags, _ := viper.Get("subcommand_flag").([]interface{})
	subflagLines := GenerateSubFlagLines(subflags, config, GenerateFlagLine)

	data := struct {
		Name         string
		Short        string
		Long         string
		FlagLines    string
		SubCommands  string
		SubLines     string
		SubFlagLines string
	}{
		Name:         config.Name,
		Short:        config.Short,
		Long:         config.Long,
		FlagLines:    flagLines,
		SubCommands:  subCommandsStr,
		SubLines:     subLinesStr,
		SubFlagLines: subflagLines,
	}

	var commandData bytes.Buffer

	if err := tmpl.Execute(&commandData, data); err != nil {
		log.Fatalf("Error executing template: %s", err)
	}
	output := commandData.String()
	formattedContent, err := format.Source([]byte(output))
	if err != nil {
		log.Fatalf("Error formatting Go code: %s", err)
	}
	if err := os.WriteFile(filepath.Join("cmd", filename), formattedContent, 0644); err != nil {
		log.Fatalf("Error writing to file: %s", err)
	}
}
