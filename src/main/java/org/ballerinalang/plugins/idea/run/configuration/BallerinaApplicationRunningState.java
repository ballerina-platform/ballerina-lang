///*
// *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.ballerinalang.plugins.idea.run.configuration;
//
//import com.intellij.execution.ExecutionException;
//import com.intellij.execution.configurations.GeneralCommandLine;
//import com.intellij.execution.process.KillableColoredProcessHandler;
//import com.intellij.execution.process.ProcessHandler;
//import com.intellij.execution.process.ProcessTerminatedListener;
//import com.intellij.execution.runners.ExecutionEnvironment;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.vfs.CharsetToolkit;
//import com.intellij.util.ObjectUtils;
//import org.ballerinalang.plugins.idea.sdk.BallerinaSdkUtil;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static org.ballerinalang.plugins.idea.run.configuration.BallerinaRunUtil.PARAMETER_REGEX;
//
//public class BallerinaApplicationRunningState extends BallerinaRunningState {
//
//    public BallerinaApplicationRunningState(Project project, String params, ExecutionEnvironment environment) {
//        super(project, params, environment);
//    }
//
//    @Override
//    public String getCommand() {
//        return "main";
//    }
//
//    @NotNull
//    @Override
//    protected ProcessHandler startProcess() throws ExecutionException {
//        GeneralCommandLine commandLine = new GeneralCommandLine();
//        commandLine.setExePath(ObjectUtils.notNull(BallerinaSdkUtil.getBallerinaExecutablePath(getProject())));
//        commandLine.addParameter("run");
//        commandLine.addParameter(getCommand());
//        commandLine.addParameter(BallerinaRunUtil.getOpenFilePath(getProject()));
//        commandLine.withCharset(CharsetToolkit.UTF8_CHARSET);
//        // Add program arguments
//        getArguments(getParams()).forEach(commandLine::addParameter);
//
//        KillableColoredProcessHandler handler = new KillableColoredProcessHandler(commandLine, true);
//        ProcessTerminatedListener.attach(handler);
//        return handler;
//    }
//
//    private List<String> getArguments(String params) {
//        List<String> results = new ArrayList<>();
//        Pattern pattern = Pattern.compile(PARAMETER_REGEX);
//        Matcher matcher = pattern.matcher(params);
//        while (matcher.find()) {
//            results.add(matcher.group(0));
//        }
//        return results;
//    }
//}
