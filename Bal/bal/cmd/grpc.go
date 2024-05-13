package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func grpcCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "grpc",
		Short:   "Generate Ballerina sources for the given Protocol Buffer definition",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("grpc")
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

	cmd.Flags().String("input", "", "Path to a '.proto' file or a directory containing multiple '.proto' files.")
	cmd.Flags().String("output", "", "Location of the generated Ballerina source files.")
	cmd.Flags().String("proto-path", "", "Path to a directory in which to look for '.proto' files when resolving import directives.")
	cmd.Flags().Bool("mode", false, "Set the 'client' or 'service' mode to generate sample code.")

	return cmd

}

func init() {
	grpcCmd := grpcCmd()
	RootCmd.AddCommand(grpcCmd)

}
