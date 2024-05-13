package cmd

import (
	"bal/pkg/utils"
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"log"
)

func semverCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:     "semver",
		Short:   "Validate SemVer compliance of the package changes",
		Long:    "",
		Example: "",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("called")
			_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
		},
	}

	viper.SetConfigName("semver")
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

	cmd.Flags().BoolP("show-diff", "d", false, "Display the detailed information of source code differences between the compared package versions")
	cmd.Flags().StringP("compare-version", "c", "", "Analyze SemVer compliance of the local changes against the specified release version")

	return cmd

}

func init() {
	semverCmd := semverCmd()
	RootCmd.AddCommand(semverCmd)

}
