package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func asyncapiCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "asyncapi",
		Short:   "short description of build",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("asyncapi")
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

	cmd.Flags().StringP("input", "i", "", "This is a mandatory input. The listener will be generated according to the given AsyncAPI contract.")
	cmd.Flags().StringP("output", "o", "", "Location of the generated Ballerina source code.")

	return cmd

}

func init() {
	asyncapiCmd := asyncapiCmd()
	RootCmd.AddCommand(asyncapiCmd)

}
