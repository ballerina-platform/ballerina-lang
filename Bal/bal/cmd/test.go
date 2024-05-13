package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func testCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "test",
		Short:   "A brief description of your command",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("test")
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

	cmd.Flags().Bool("code-coverage", false, "Generate code coverage in tests.")
	cmd.Flags().String("coverage-format", "", "Generate a coverage report in the specified format.")

	cmd.Flags().String("disable-groups", "", "Specify the test groups to be excluded.")
	cmd.Flags().String("groups", "", "Specify the test groups to be executed.")
	cmd.Flags().Bool("list-groups", false, "List the test groups available in the test files.")
	cmd.Flags().Bool("offline", false, "Proceed without accessing the network.")
	cmd.Flags().Bool("observability-included", false, "Include dependencies required to enable observability.")
	cmd.Flags().Bool("sticky", false, "Attempt to stick to the dependency versions available in the 'Dependencies.toml' file.")
	cmd.Flags().Bool("rerun-failed", false, "Rerun failed tests.")
	cmd.Flags().String("target-dir", "", "Specify the target directory.")
	cmd.Flags().Bool("test-report", false, "Generate an HTML report containing the test results.")
	cmd.Flags().String("tests", "", "Specify the test functions to be executed.")
	cmd.Flags().Bool("graalvm", false, "Execute test cases using GraalVM native image.")
	cmd.Flags().String("graalvm-build-options", "", "Additional build options passed to GraalVM native image.")
	cmd.Flags().String("excludes", "", "Exclude specific Ballerina source files or folders from code coverage calculation.")

	return cmd

}

func init() {
	testCmd := testCmd()
	RootCmd.AddCommand(testCmd)

}
