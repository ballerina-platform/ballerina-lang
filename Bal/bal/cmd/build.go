package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func buildCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "build",
		Short:   "short description of build",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("build")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config") // Update this with your actual config path
	viper.SetConfigType("toml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	if long := viper.GetString("base_command.help.long"); long != "" {
		cmd.Long = long
	}
	if examples := viper.GetString("base_command.help.examples"); examples != "" {
		cmd.Example = examples
	}

	cmd.Flags().StringP("output", "o", "", "Write the output to the given file.")
	cmd.Flags().Bool("offline", false, "Run in offline mode")
	cmd.Flags().Bool("observability-included", false, "Run with including observability")
	cmd.Flags().Bool("sticky", false, "Run with including sticky")
	cmd.Flags().String("target-dir", "", "Specify the target directory")
	cmd.Flags().Bool("export-openapi", false, "Export OpenAPI specification")
	cmd.Flags().Bool("list-conflicted-classes", false, "List conflicted classes")
	cmd.Flags().Bool("graalvm", false, "Use GraalVM")
	cmd.Flags().String("graalvm-build-options", "", "Specify GraalVM build options")
	cmd.Flags().String("cloud", "", "Specify the cloud provider")

	return cmd

}

func init() {
	buildCmd := buildCmd()
	RootCmd.AddCommand(buildCmd)

}
