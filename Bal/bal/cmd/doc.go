package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func docCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "doc",
		Short:   "A brief description of your command",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("doc")
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

	cmd.Flags().StringP("output", "o", "", "Write the output to the given file.")
	cmd.Flags().StringP("exclude", "e", "", "Write the output to the given file.")
	cmd.Flags().Bool("offline", false, "Run in offline mode")
	cmd.Flags().Bool("sticky", false, "Run with including sticky")
	cmd.Flags().String("target-dir", "", "Specify the target directory")

	return cmd

}

func init() {
	docCmd := docCmd()
	RootCmd.AddCommand(docCmd)

}
