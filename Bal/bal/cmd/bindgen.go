package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func bindgenCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "bindgen",
		Short:   "Generate Ballerina bindings for Java APIs",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("bindgen")
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

	cmd.Flags().String("classpath", "", "Specify the classpath for Java APIs.")
	cmd.Flags().String("maven", "", "Specify a Maven dependency with a colon-delimited group ID, artifact ID, and version.")
	cmd.Flags().StringP("output", "o", "", "Write the output to the given file.")
	cmd.Flags().Bool("public", false, "Generate public API.")
	cmd.Flags().Bool("with-optional-types", false, "Generate bindings with optional types.")
	cmd.Flags().Bool("with-optional-types-param", false, "Include optional types in function parameters.")
	cmd.Flags().Bool("with-optional-types-return", false, "Include optional types in function return types.")

	return cmd

}

func init() {
	bindgenCmd := bindgenCmd()
	RootCmd.AddCommand(bindgenCmd)

}
