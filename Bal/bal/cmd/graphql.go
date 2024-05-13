package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func graphqlCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "graphql",
		Short:   "Generate the GraphQL schema for a Ballerina GraphQL service",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("graphql")
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

	cmd.Flags().StringP("input", "i", "", "This is mandatory input. The given Ballerina GraphQL service file will generate the GraphQL schema (SDL) file relevant to the service.")
	cmd.Flags().StringP("output", "o", "", "Output file path")
	cmd.Flags().StringP("service", "s", "", "Service name")
	cmd.Flags().BoolP("use-records-for-objects", "r", false, "Use records for objects")
	cmd.Flags().BoolP("mode", "m", false, "Mode flag")

	return cmd

}

func init() {
	graphqlCmd := graphqlCmd()
	RootCmd.AddCommand(graphqlCmd)

}
