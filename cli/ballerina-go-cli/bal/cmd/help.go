/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package cmd

import (
	"bal/pkg/templates"

	"github.com/spf13/cobra"
)

var helpcmd *cobra.Command

func helpCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "help [command]",
		Short: "Help about any command",
		Long:  `Help provides help for any command in the application.`,
		Run: func(cmd *cobra.Command, args []string) {
			customHelpFunc(cmd, args)
		},
	}
}

func init() {
	helpcmd = helpCmd()
	RootCmd.AddCommand(helpcmd)
}

func customHelpFunc(cmd *cobra.Command, args []string) {
	if len(args) == 0 {
		templates.Executing_Help_Template(*cmd, commandGroups)
	} else {
		templates.ValidateArgs(args, RootCmd, ToolsPass)
	}
}
