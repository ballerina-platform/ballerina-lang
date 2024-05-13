package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func formatCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "format",
		Short:   "Format the Ballerina source files",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("format")
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

	cmd.Flags().BoolP("dry-run", "d", false, "Perform a dry run of the formatter and see which files will be formatted after the execution")

	return cmd

}

func init() {
	formatCmd := formatCmd()
	RootCmd.AddCommand(formatCmd)

}
