package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"log"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

func healthCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "health",
		Short:   "short description of health",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("health")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config") // Update this with your actual config path
	viper.SetConfigType("json")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	if long := viper.GetString("help.long"); long != "" {
		cmd.Long = long
	}
	if examples := viper.GetString("help.examples"); examples != "" {
		cmd.Example = examples
	}

	cmd.Flags().StringP("output", "o", "", "Write the output to the given file.")
	cmd.Flags().StringP("mode", "m", "", "Mode can be 'package' or 'template'")

	return cmd

}

func init() {
	healthCmd := healthCmd()
	RootCmd.AddCommand(healthCmd)
	healthCmd.AddCommand(sub1healthCmd())
	healthCmd.AddCommand(sub2healthCmd())

}

func sub1healthCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "sub1",
		Short:   "this is short description",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}
	cmd.Flags().String("repository", "", "Use a tool from a custom repository.")

	viper.SetConfigName("health")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("json")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}

func sub2healthCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "sub2",
		Short:   "this is short description",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}
	cmd.Flags().String("some_flag", "", "Description of the flag")

	viper.SetConfigName("health")
	viper.AddConfigPath("/home/wso2/Final_implementation/ballerina-lang/Bal/executables/config")
	viper.SetConfigType("json")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file: %s", err)
	}

	return cmd
}
