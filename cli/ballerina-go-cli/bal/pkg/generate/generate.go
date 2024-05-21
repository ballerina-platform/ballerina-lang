package generate

import (
	"bal/pkg/utils"
	"fmt"
	"log"
	"os"
	"os/user"
	"path/filepath"

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

func readToolToml() error {
	currentUser, err := user.Current()
	if err != nil {
		return err
	}

	balToolPath := filepath.Join(currentUser.HomeDir, ".ballerina", ".config")
	viper.SetConfigName("bal-tools")
	viper.AddConfigPath(balToolPath)
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		return fmt.Errorf("error reading config file: %w", err)
	}

	return nil
}

func FindPathForJson(toolName string) string {
	jasonPath := ""
	currentUser, _ := user.Current()
	readToolToml()
	toolDetails := viper.Get("tool")
	if toolDetails != nil {
		for _, table := range toolDetails.([]interface{}) {
			if m, ok := table.(map[string]interface{}); ok {
				if cast.ToString(m["id"]) == toolName && cast.ToBool(m["active"]) {
					toolData := createToolData(m)
					repocitoryType := getRepositoryType(toolData)
					jasonPath = getJasonPath(currentUser, repocitoryType, toolData)
					break
				}
			}
		}
	}

	if jasonPath == "" {
		fmt.Println("Tool not found or not active in config")
	}
	return jasonPath
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

func addFlagsToCommand(cmd *cobra.Command, flags interface{}) {
	if flags != nil {
		for _, table := range flags.([]interface{}) {
			if m, ok := table.(map[string]interface{}); ok {
				flag := FlagConfig{
					Name:       cast.ToString(m["name"]),
					Usage:      cast.ToString(m["usage"]),
					DefaultVal: m["default_val"],
					Shorthend:  cast.ToString(m["shorthand"]),
				}
				switch flag.DefaultVal.(type) {
				case bool:
					if flag.Shorthend == "" {
						cmd.Flags().Bool(flag.Name, flag.DefaultVal.(bool), flag.Usage)
					} else {
						cmd.Flags().BoolP(flag.Name, flag.Shorthend, flag.DefaultVal.(bool), flag.Usage)
					}
				case string:
					if flag.Shorthend == "" {
						cmd.Flags().String(flag.Name, flag.DefaultVal.(string), flag.Usage)
					} else {
						cmd.Flags().StringP(flag.Name, flag.Shorthend, flag.DefaultVal.(string), flag.Usage)
					}
				case int:
					if flag.Shorthend == "" {
						cmd.Flags().Int(flag.Name, flag.DefaultVal.(int), flag.Usage)
					} else {
						cmd.Flags().IntP(flag.Name, flag.Shorthend, flag.DefaultVal.(int), flag.Usage)
					}
				}
			}
		}
	}
}

// RegisterDynamicCommands function reads the json file and creates the cobra commands
// according to the json data.
func RegisterDynamicCommands(javaCmdPass string, cmdLineArgsPass []string, path string, rootCmd *cobra.Command) error {
	viper.SetConfigFile(path)
	viper.SetConfigType("json")
	if err := viper.ReadInConfig(); err != nil {
		log.Fatalln("Error reading config file:", err)
	}
	//Create Base command
	toolID := viper.GetString("tool_id")
	toolShort := viper.GetString("short")
	toolLong := viper.GetString("help.base.long")
	toolExamples := viper.GetString("help.base.examples")
	cmd := &cobra.Command{
		Use:     toolID,
		Short:   toolShort,
		Long:    toolLong,
		Example: toolExamples,
		Run: func(cmd *cobra.Command, args []string) {
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}
	flags := viper.Get("base_command.flag")
	addFlagsToCommand(cmd, flags)
	// Create sub commands
	subcommands := viper.Get("base_command.subcommand")
	if subcommands != nil {
		for _, subcmd := range subcommands.([]interface{}) {
			if m, ok := subcmd.(map[string]interface{}); ok {
				subconfig := CommandConfig{
					Name:  cast.ToString(m["name"]),
					Short: cast.ToString(m["short"]),
				}
				subCmd := &cobra.Command{
					Use:     subconfig.Name,
					Short:   subconfig.Short,
					Long:    viper.GetString(fmt.Sprintf("help.%s.long", subconfig.Name)),
					Example: viper.GetString(fmt.Sprintf("help.%s.examples", subconfig.Name)),
					Run: func(cmd *cobra.Command, args []string) {
						_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
					},
				}
				subFlags := viper.Get(subconfig.Name + ".flag")
				addFlagsToCommand(subCmd, subFlags)
				cmd.AddCommand(subCmd)
			}
		}
	}
	rootCmd.AddCommand(cmd)
	return nil
}

// GetTools function reads the bal-tools.toml file and returns the list of tools that are active and
// creates the cobra commands for the tools that are active.
func GetTools(javaCmd string, cmdLineArgs []string, rootCmd *cobra.Command) []string {
	currentUser, _ := user.Current()
	balToolPath := filepath.Join(currentUser.HomeDir, ".ballerina", ".config")
	toolList := []string{}
	if fileExists(filepath.Join(balToolPath, "bal-tools.toml")) {
		readToolToml()
		toolDetails := viper.Get("tool")
		if toolDetails != nil {
			toolList = processToolDetails(toolDetails, javaCmd, cmdLineArgs, rootCmd, currentUser)
		}
	}
	return toolList
}

func processToolDetails(toolDetails interface{}, javaCmd string, cmdLineArgs []string, rootCmd *cobra.Command, currentUser *user.User) []string {
	toolList := []string{}
	for _, table := range toolDetails.([]interface{}) {
		if m, ok := table.(map[string]interface{}); ok {
			if cast.ToBool(m["active"]) {
				toolData := createToolData(m)
				repocitoryType := getRepositoryType(toolData)
				toolList = append(toolList, toolData.Id)
				jasonPath := getJasonPath(currentUser, repocitoryType, toolData)
				registerCommands(javaCmd, cmdLineArgs, jasonPath, rootCmd)
			}
		}
	}
	return toolList
}

func createToolData(m map[string]interface{}) ToolData {
	return ToolData{
		Id:         cast.ToString(m["id"]),
		Org:        cast.ToString(m["org"]),
		Name:       cast.ToString(m["name"]),
		Active:     cast.ToBool(m["active"]),
		Version:    cast.ToString(m["version"]),
		Repocitory: cast.ToString(m["repocitory"]),
	}
}

func getRepositoryType(toolData ToolData) string {
	if toolData.Repocitory == "" {
		return "central.ballerina.io"
	}
	return toolData.Repocitory
}

func getJasonPath(currentUser *user.User, repocitoryType string, toolData ToolData) string {
	repoPath := filepath.Join(currentUser.HomeDir, ".ballerina", "repositories", repocitoryType)
	jasonPath := filepath.Join(repoPath, "bala", toolData.Org, toolData.Name, toolData.Version, "java17", "tool", "bal-tool.json")
	return jasonPath
}

func registerCommands(javaCmd string, cmdLineArgs []string, jasonPath string, rootCmd *cobra.Command) {
	err := RegisterDynamicCommands(javaCmd, cmdLineArgs, jasonPath, rootCmd)
	if err != nil {
		log.Println("Error registering dynamic commands:", err)
	}
}

func fileExists(filepath string) bool {
	info, err := os.Stat(filepath)
	if os.IsNotExist(err) {
		return false
	}
	return !info.IsDir()
}
