package templates

import (
	"bal/cmd"
	"fmt"
	"log"
)

func ValidateArgs(args []string) {
	if len(args) == 0 {
		fmt.Println("No subcommand provided")
	}
	if len(args) == 1 {
		if _, _, err := cmd.RootCmd.Find([]string{args[0]}); err != nil {
			log.Fatal("'" + args[0] + "' is not a valid command.")
		} else {
			fmt.Println("'" + args[0] + "' is a command.")
		}
	}
	if len(args) >= 2 {
		for i := 1; i < len(args); i++ {
			checkSubCommand(args[0 : i+1])
		}

	}
}

func checkSubCommand(cmdPath []string) {
	foundCmd, _, err := cmd.RootCmd.Find(cmdPath)
	if err != nil || foundCmd.Name() != cmdPath[len(cmdPath)-1] {
		log.Fatalln("'" + cmdPath[len(cmdPath)-1] + "' is not a valid subcommand of '" + cmdPath[len(cmdPath)-2] + "'.")
	} else {
		log.Println("'" + cmdPath[len(cmdPath)-1] + "' is a subcommand of '" + cmdPath[len(cmdPath)-2] + "'.")
	}
}
