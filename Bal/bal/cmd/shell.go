package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func shellCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "shell",
		Short:   "Run Ballerina interactive REPL",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("shell")
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

	cmd.Flags().BoolP("debug", "d", false, "Enable debug mode from the beginning.")
	cmd.Flags().Bool("force-dumb", false, "Force the dumb terminal mode.")
	cmd.Flags().StringP("file", "f", "", "Open a file and load the initial declarations.")
	cmd.Flags().StringP("time-out", "t", "", "Set the tree parsing timeout value.")

	return cmd

}

func init() {
	shellCmd := shellCmd()
	RootCmd.AddCommand(shellCmd)

}
