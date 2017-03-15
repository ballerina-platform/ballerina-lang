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
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.fileEditor.FileDocumentManager;
//import com.intellij.openapi.fileEditor.FileEditorManager;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.vfs.VirtualFile;
//import org.ballerinalang.plugins.idea.BallerinaFileType;
//
//public class BallerinaRunUtil {
//
//    public static String PARAMETER_REGEX = "\".+?(?<!\\\\)\"|\\w+";
//
//    private BallerinaRunUtil() {
//
//    }
//
//    /**
//     * Get the file path of the currently opened file in the editor.
//     *
//     * @param project current project
//     * @return filepath or empty string if filepath cannot be found
//     */
//    public static String getOpenFilePath(Project project) {
//        Editor selectedTextEditor = getEditor(project);
//        if (selectedTextEditor == null) {
//            return "";
//        }
//        VirtualFile file = getVirtualFile(selectedTextEditor);
//        if (file == null) {
//            return "";
//        }
//        return file.getPath();
//    }
//
//    public static boolean isBallerinaFileOpen(Project project) {
//        Editor editor = getEditor(project);
//        VirtualFile file = getVirtualFile(editor);
//        if (file.getFileType() == BallerinaFileType.INSTANCE) {
//            return true;
//        }
//        return false;
//    }
//
//    public static Editor getEditor(Project project) {
//        return FileEditorManager.getInstance(project).getSelectedTextEditor();
//    }
//
//    public static VirtualFile getVirtualFile(Editor editor) {
//        return FileDocumentManager.getInstance().getFile(editor.getDocument());
//    }
//}
