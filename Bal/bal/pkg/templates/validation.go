package templates

import (
	"fmt"
	"log"

	"github.com/spf13/cobra"
)

func ValidateArgs(args []string, rootCmd *cobra.Command) {
	if len(args) == 0 {
		fmt.Println("No subcommand provided")
	}
	if len(args) == 1 {
		if _, _, err := rootCmd.Find([]string{args[0]}); err != nil {
			log.Fatal("'" + args[0] + "' is not a valid command.")
		} else {
			fmt.Println("'" + args[0] + "' is a command.")
		}
	}
	if len(args) >= 2 {
		for i := 1; i < len(args); i++ {
			checkSubCommand(args[0:i+1], rootCmd)
		}

	}
}

func checkSubCommand(cmdPath []string, rootCmd *cobra.Command) {
	foundCmd, _, err := rootCmd.Find(cmdPath)
	if err != nil || foundCmd.Name() != cmdPath[len(cmdPath)-1] {
		log.Fatalln("'" + cmdPath[len(cmdPath)-1] + "' is not a valid subcommand of '" + cmdPath[len(cmdPath)-2] + "'.")
	}
}
