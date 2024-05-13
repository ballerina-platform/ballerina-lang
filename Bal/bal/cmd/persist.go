package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func persistCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "persist",
		Short:   "Manage data persistence",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("persist")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config") // Update this with your actual config path
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

	return cmd

}

func init() {
	persistCmd := persistCmd()
	RootCmd.AddCommand(persistCmd)
	persistCmd.AddCommand(initpersistCmd())
	persistCmd.AddCommand(generatepersistCmd())

}

func initpersistCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "init",
		Short:   "Initialize the Ballerina package for persistence.",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}
	cmd.Flags().String("module", "", "Initialize the Ballerina package for persistence with the given module")
	cmd.Flags().String("datastore", "", "The type of the datastore to be used for persistence")

	viper.SetConfigName("persist")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	if long := viper.GetString("help.init.long"); long != "" {
		cmd.Long = long
	}
	if examples := viper.GetString("help.init.examples"); examples != "" {
		cmd.Example = examples
	}

	return cmd
}

func generatepersistCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "generate",
		Short:   "Generate the client API based on the data model.",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("persist")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	if long := viper.GetString("help.generate.long"); long != "" {
		cmd.Long = long
	}
	if examples := viper.GetString("help.generate.examples"); examples != "" {
		cmd.Example = examples
	}

	return cmd
}
