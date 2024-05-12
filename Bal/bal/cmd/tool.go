package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func toolCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "tool",
		Short:   "Extend the Ballerina CLI with custom commands",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("tool")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config") // Update this with your actual config path
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	if long := viper.GetString("base_command.help.long"); long != "" {
		cmd.Long = long
	}
	if examples := viper.GetString("base_command.help.examples"); examples != "" {
		cmd.Example = examples
	}

	return cmd

}

func init() {
	toolCmd := toolCmd()
	RootCmd.AddCommand(toolCmd)
	toolCmd.AddCommand(pulltoolCmd())
	toolCmd.AddCommand(removetoolCmd())
	toolCmd.AddCommand(updatetoolCmd())
	toolCmd.AddCommand(usetoolCmd())
	toolCmd.AddCommand(listtoolCmd())
	toolCmd.AddCommand(searchtoolCmd())

}

func pulltoolCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "pull",
		Short:   "Pull a tool from the Ballerina Central",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			utils.GenerateTool(javaCmdPass, cmdLineArgsPass)
		},
	}
	cmd.Flags().String("repository", "", "Pull a tool from a custom repository.")

	viper.SetConfigName("tool")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}

func removetoolCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "remove",
		Short:   "Remove a tool from the local environment.",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			utils.RemoveTool(javaCmdPass, cmdLineArgsPass)
		},
	}
	cmd.Flags().String("repository", "", "Remove a tool from a custom repository.")

	viper.SetConfigName("tool")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}

func updatetoolCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "update",
		Short:   "Update a tool to the latest version compatible with the current distribution.",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			utils.GenerateTool(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("tool")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}

func usetoolCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "use",
		Short:   "Set a tool version as the active version.",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			utils.GenerateTool(javaCmdPass, cmdLineArgsPass)
		},
	}
	cmd.Flags().String("repository", "", "Use a tool from a custom repository.")

	viper.SetConfigName("tool")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}

func listtoolCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "list",
		Short:   "List all tools available in the local environment.",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("tool")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}

func searchtoolCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "search",
		Short:   "Search the Ballerina Central for tools",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("tool")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}
