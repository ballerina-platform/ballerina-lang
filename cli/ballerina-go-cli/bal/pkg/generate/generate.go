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

func RegisterDynamicCommands(javaCmdPass string, cmdLineArgsPass []string, path string, rootCmd *cobra.Command) error {
	viper.SetConfigFile(path)
	viper.SetConfigType("json")
	if err := viper.ReadInConfig(); err != nil {
		log.Fatalln("Error reading config file:", err)
	}
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
					cmd.Flags().BoolP(flag.Name, flag.Shorthend, flag.DefaultVal.(bool), flag.Usage)
				case string:
					cmd.Flags().StringP(flag.Name, flag.Shorthend, flag.DefaultVal.(string), flag.Usage)
				case int:
					cmd.Flags().IntP(flag.Name, flag.Shorthend, flag.DefaultVal.(int), flag.Usage)

				}
			}
		}
	}
	// Register subcommands
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
				if subFlags != nil {
					for _, table := range subFlags.([]interface{}) {
						if m, ok := table.(map[string]interface{}); ok {
							subflag := FlagConfig{
								Name:       cast.ToString(m["name"]),
								Usage:      cast.ToString(m["usage"]),
								DefaultVal: m["default_val"],
								Shorthend:  cast.ToString(m["shorthand"]),
							}
							switch subflag.DefaultVal.(type) {
							case bool:
								subCmd.Flags().BoolP(subflag.Name, subflag.Shorthend, subflag.DefaultVal.(bool), subflag.Usage)
							case string:
								subCmd.Flags().StringP(subflag.Name, subflag.Shorthend, subflag.DefaultVal.(string), subflag.Usage)
							case int:
								subCmd.Flags().IntP(subflag.Name, subflag.Shorthend, subflag.DefaultVal.(int), subflag.Usage)
							}
						}
					}
				}
				cmd.AddCommand(subCmd)
			}
		}
	}
	rootCmd.AddCommand(cmd)
	return nil
}

func GetTools(javaCmd string, cmdLineArgs []string, rootCmd *cobra.Command) []string {
	currentUser, _ := user.Current()
	balToolPath := filepath.Join(currentUser.HomeDir, ".ballerina", ".config")
	viper.SetConfigName("bal-tools")
	viper.AddConfigPath(balToolPath)
	viper.SetConfigType("toml")
	toolList := []string{}
	if fileExists(filepath.Join(balToolPath, "bal-tools.toml")) {
		if err := viper.ReadInConfig(); err != nil {
			log.Println("Error reading config file:", err)
		}
		toolDetails := viper.Get("tool")
		if toolDetails != nil {
			for _, table := range toolDetails.([]interface{}) {
				if m, ok := table.(map[string]interface{}); ok {
					if cast.ToBool(m["active"]) {
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
						toolList = append(toolList, toolData.Id)
						jasonPath := filepath.Join(currentUser.HomeDir, ".ballerina", "repositories", repocitoryType, "bala", toolData.Org, toolData.Name, toolData.Version, "java17", "tool", "bal-tool.json")
						jasonPath = "/home/wso2/Bal/executables/config/health.json"
						err := RegisterDynamicCommands(javaCmd, cmdLineArgs, jasonPath, rootCmd)
						if err != nil {
							fmt.Println("Error registering dynamic commands:", err)
						}

					}
				}
			}

		}

	}
	return toolList
}

func fileExists(filepath string) bool {
	info, err := os.Stat(filepath)
	if os.IsNotExist(err) {
		return false
	}
	return !info.IsDir()
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
	toolDetails := viper.Get("tool")
	if toolDetails != nil {
		for _, table := range toolDetails.([]interface{}) {
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
	}

	if jasonPath == "" {
		fmt.Println("Tool not found or not active in config")
	}

	return jasonPath
}
