package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func healthCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "health",
		Short:   "short description of health",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("health")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config") // Update this with your actual config path
	viper.SetConfigType("json")

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
	healthCmd := healthCmd()
	RootCmd.AddCommand(healthCmd)

}
