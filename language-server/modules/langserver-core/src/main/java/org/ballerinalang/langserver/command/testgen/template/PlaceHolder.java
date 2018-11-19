/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.command.testgen.template;

import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Contains placeholders for the templates.
 *
 * @since 0.985.0
 */
public enum PlaceHolder {

    DECLARATIONS("declarations") {
        @Override
        public DiagnosticPos getPosition(BLangPackage bLangPackage) {
            final DiagnosticPos[] pos = {new DiagnosticPos(null, 0, 0, 0, 0)};
            //after imports
            bLangPackage.getImports().forEach(imp -> pos[0] = getMaximumPosition(imp.getPosition(), pos[0]));
            bLangPackage.getGlobalVariables().forEach(var -> pos[0] = getMaximumPosition(var.getPosition(), pos[0]));
            return resetColumnPosition(pos[0]);
        }
    },

    CONTENT("content") {
        @Override
        public DiagnosticPos getPosition(BLangPackage bLangPackage) {
            final DiagnosticPos[] pos = {new DiagnosticPos(null, 0, 0, 0, 0)};
            bLangPackage.topLevelNodes.forEach(topLevelNode -> {
                if (topLevelNode instanceof BLangNode) {
                    pos[0] = getMaximumPosition(((BLangNode) topLevelNode).getPosition(), pos[0]);
                }
            });
            pos[0].eLine = pos[0].eLine + 1;
            pos[0].sLine = pos[0].eLine;
            return resetColumnPosition(pos[0]);
        }
    },

    IMPORTS("imports") {
        @Override
        public DiagnosticPos getPosition(BLangPackage bLangPackage) {
            final DiagnosticPos[] pos = {new DiagnosticPos(null, 0, 0, 0, 0)};
            bLangPackage.getImports().forEach(pkg -> pos[0] = getMaximumPosition(pkg.getPosition(), pos[0]));
            return resetColumnPosition(pos[0]);
        }
    },

    OTHER("_other_") {
        @Override
        public DiagnosticPos getPosition(BLangPackage bLangPackage) {
            throw new UnsupportedOperationException("Not supported!");
        }

        @Override
        public PlaceHolder get(String name) {
            this.name = name;
            return this;
        }
    };

    protected String name;

    PlaceHolder(String name) {
        this.name = name;
    }

    private static DiagnosticPos resetColumnPosition(DiagnosticPos pos) {
        pos.sCol = 0;
        pos.eCol = 0;
        return pos;
    }

    private static DiagnosticPos getMaximumPosition(DiagnosticPos pos1, DiagnosticPos pos2) {
        // handle null
        if (pos1 == null) {
            return pos2;
        } else if (pos2 == null) {
            return pos1;
        }
        if (pos1.getEndLine() > pos2.getEndLine()) {
            // pos1.Line > pos2.Line
            return pos1;
        } else if (pos1.getEndLine() < pos2.getEndLine()) {
            // pos1.Line < pos2.Line
            return pos2;
        } else {
            // pos1.Line == pos2.Line
            if (pos1.getEndColumn() > pos2.getEndColumn()) {
                // pos1.Col > pos2.Col
                return pos1;
            } else {
                // pos1.Col < pos2.Col
                return pos2;
            }
        }
    }

    public abstract DiagnosticPos getPosition(BLangPackage bLangPackage);

    public String getName() {
        return this.name;
    }

    public PlaceHolder get(String name) {
        throw new UnsupportedOperationException("Not supported!");
    }
}
