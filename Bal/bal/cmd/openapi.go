package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func openapiCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "openapi",
		Short:   "Generate a Ballerina service or a client from an OpenAPI contract and vice versa.",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("openapi")
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

	cmd.Flags().StringP("input", "i", "", "This is mandatory input. The given Ballerina GraphQL service file will generate the GraphQL schema (SDL) file relevant to the service")
	cmd.Flags().StringP("output", "o", "", "Location of the generated Ballerina source code.")
	cmd.Flags().String("mode", "", "The Ballerina service and client will be generated according to the specified mode.")
	cmd.Flags().BoolP("nullable", "n", false, "This is a safe option to generate all data types in the record with Ballerina nil support")
	cmd.Flags().String("license", "", "Optional. The `.bal` files will generate with the given copyright or license header.")
	cmd.Flags().StringP("service", "s", "", "This option is used with the command of Ballerina to OpenAPI specification generation.")
	cmd.Flags().Bool("json", false, "Generate the Ballerina service to OpenAPI output in JSON. The default is YAML.")
	cmd.Flags().String("tags", "", "This option is used with the OpenAPI to Ballerina file generation command.")
	cmd.Flags().String("operations", "", "List of operations to generate the Ballerina service or client.")
	cmd.Flags().Bool("with-tests", false, "Work with the client generation command and generate a test")
	cmd.Flags().Bool("without-data-binding", false, "This option can be used in the service generation to generate a low-level service without any data-binding logic.")
	cmd.Flags().String("client-methods", "", "This option can be used in client generation to select the client method type, which can be `resource` or `remote`.")

	return cmd

}

func init() {
	openapiCmd := openapiCmd()
	RootCmd.AddCommand(openapiCmd)

}
