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
package org.ballerinalang.langserver.command.testgen.renderer;

import org.ballerinalang.langserver.command.testgen.template.PlaceHolder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a BLangPackage based render output.
 *
 * @since 0.985.0
 */
public class BLangPkgBasedRendererOutput implements RendererOutput {
    private BLangPackage bLangPackageOfTestFile;
    /**
     * List of placeholders to keep track of placeholder content.
     */
    private Map<DiagnosticPos, Map<String, String>> positions = new HashMap<>();

    /**
     * Returns a new FileTemplate.
     *
     * @param bLangPackage content of the test file
     */
    public BLangPkgBasedRendererOutput(BLangPackage bLangPackage) {
        this.bLangPackageOfTestFile = bLangPackage;
    }

    /**
     * Appends the contents into the placeholder.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    @Override
    public void append(PlaceHolder placeHolder, String content) {
        merge(placeHolder, (oldContent) -> (oldContent == null ? "" : oldContent) + content);
    }

    /**
     * Prepends the contents into the placeholder.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    @Override
    public void prepend(PlaceHolder placeHolder, String content) {
        merge(placeHolder, (oldContent) -> content + (oldContent == null ? "" : oldContent));
    }

    private void merge(PlaceHolder placeHolder, Function<String, String> merger) {
        DiagnosticPos position = placeHolder.getPosition(bLangPackageOfTestFile);
        Map<String, String> placeHolders = positions.get(position);
        placeHolders = (placeHolders == null) ? new HashMap<>() : placeHolders;
        String oldContent = placeHolders.get(placeHolder.getName());
        placeHolders.put(placeHolder.getName(), merger.apply(oldContent));
        positions.put(position, placeHolders);
    }

    /**
     * Replaces place holder content.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    @Override
    public void put(PlaceHolder placeHolder, String content) {
        DiagnosticPos position = placeHolder.getPosition(bLangPackageOfTestFile);
        Map<String, String> placeHolders = positions.get(position);
        placeHolders = (placeHolders == null) ? new HashMap<>() : placeHolders;
        placeHolders.put(placeHolder.getName(), content);
        positions.put(position, placeHolders);
    }

    /**
     * Returns rendered text-edits.
     *
     * @return rendered edits
     */
    @Override
    public List<TextEdit> getRenderedTextEdits() {
        List<TextEdit> edits = new ArrayList<>();

        //Calculate text-edits
        positions.forEach(
                (pos, value) -> value.forEach((placeHolder, content) -> {
                    Position position = new Position(pos.eLine, pos.eCol);
                    Range range = new Range(position, position);
                    TextEdit textEdit = new TextEdit(range, content);
                    edits.add(textEdit);
                })
        );
        return edits;
    }

    /**
     * Returns rendered content.
     *
     * @return rendered string
     */
    @Override
    public String getRenderedContent() {
        throw new UnsupportedOperationException("Not supported!");
    }

    /**
     * Returns True when creating a new test file.
     *
     * @return True when creating a new file, False otherwise
     */
    public boolean isNewTestFile() {
        return false;
    }
}
