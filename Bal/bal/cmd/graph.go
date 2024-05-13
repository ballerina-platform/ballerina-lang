package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func graphCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "graph",
		Short:   "Print the dependency graph",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("graph")
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

	cmd.Flags().Bool("dump-raw-graphs", false, "Print all intermediate graphs created in the dependency resolution process.")
	cmd.Flags().Bool("offline", false, "Proceed without accessing the network.")
	cmd.Flags().Bool("sticky", false, "Attempt to stick to the dependency versions available in the 'Dependencies.toml' file.")
	cmd.Flags().String("target-dir", "", "Target directory path")

	return cmd

}

func init() {
	graphCmd := graphCmd()
	RootCmd.AddCommand(graphCmd)

}
