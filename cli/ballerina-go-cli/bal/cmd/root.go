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
	"bal/pkg/generate"
	"bal/pkg/templates"
	"bal/pkg/utils"
	"os"

	"github.com/spf13/cobra"
)

var (
	javaCmdPass     string
	cmdLineArgsPass []string
	ToolsPass       []*cobra.Command
	commandGroups   templates.CommandGroups
)

var RootCmd = &cobra.Command{
	Use:   "bal",
	Short: "The build system and package manager of Ballerina",
	Run: func(cmd *cobra.Command, args []string) {
		_ = utils.ExecuteBallerinaCommand(javaCmdPass, cmdLineArgsPass)
	},
}

func Execute() {
	err := RootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
}

func init() {
	RootCmd.Flags().BoolP("version", "v", false, "Print version information.")
	cmdLineArgsPass, javaCmdPass = utils.Setup()

	toolList := generate.GetTools(javaCmdPass, cmdLineArgsPass, RootCmd)
	Tools := templates.CommandGroup{Message: "Tool Commands", Commands: generate.GetCommandsList(toolList, RootCmd)}
	ToolsPass = Tools.Commands

	commandGroups = templates.CommandGroups{
		{Message: "Core Commands", Commands: []*cobra.Command{buildCmd(), runCmd(), testCmd(), docCmd(), packCmd()}},
		{Message: "Package Commands", Commands: []*cobra.Command{newCmd(), addCmd(), pullCmd(), pushCmd(), searchCmd(), semverCmd(), graphCmd(), deprecateCmd()}},
		{Message: "Other Commands", Commands: []*cobra.Command{cleanCmd(), formatCmd(), grpcCmd(), graphqlCmd(), openapiCmd(), asyncapiCmd(), persistCmd(), persistCmd(), bindgenCmd(), shellCmd(), toolCmd(), versionCmd(), profileCmd()}},
		Tools,
	}

	RootCmd.SetHelpFunc(func(cmd *cobra.Command, args []string) {
		if len(args) == 1 || len(args) == 0 {
			templates.Executing_Help_Template(*cmd, commandGroups)
		} else {
			templates.ValidateArgs(args[:len(args)-1], RootCmd, ToolsPass)
		}
	})
}
