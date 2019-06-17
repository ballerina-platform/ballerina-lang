/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.webview.diagram.split;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.JBSplitter;
import io.ballerina.plugins.idea.webview.diagram.settings.DiagramApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Split file editor.
 *
 * @param <E1> Document editor
 * @param <E2> Diagram editor
 */
public abstract class SplitFileEditor<E1 extends FileEditor, E2 extends FileEditor> extends UserDataHolderBase
        implements FileEditor {
    public static final Key<SplitFileEditor> PARENT_SPLIT_KEY = Key.create("parentSplit");

    private static final String MY_PROPORTION_KEY = "SplitFileEditor.Proportion";

    @NotNull
    protected final E1 myMainEditor;
    @NotNull
    protected final E2 mySecondEditor;
    @NotNull
    private final JComponent myComponent;
    @NotNull
    private final MyListenersMultimap myListenersGenerator = new MyListenersMultimap();
    @NotNull
    private SplitEditorLayout mySplitEditorLayout = DiagramApplicationSettings.getInstance().getDiagramPreviewSettings()
            .getSplitEditorLayout();
    private SplitEditorToolbar myToolbarWrapper;

    public SplitFileEditor(@NotNull E1 mainEditor, @NotNull E2 secondEditor) {
        myMainEditor = mainEditor;
        mySecondEditor = secondEditor;

        myComponent = createComponent();

        if (myMainEditor instanceof TextEditor) {
            myMainEditor.putUserData(PARENT_SPLIT_KEY, this);
        }
        if (mySecondEditor instanceof TextEditor) {
            mySecondEditor.putUserData(PARENT_SPLIT_KEY, this);
        }

        DiagramApplicationSettings.SettingsChangedListener settingsChangedListener =
                new DiagramApplicationSettings.SettingsChangedListener() {
                    @Override
                    public void beforeSettingsChanged(@NotNull DiagramApplicationSettings newSettings) {
                        SplitEditorLayout oldSplitEditorLayout = DiagramApplicationSettings.getInstance()
                                .getDiagramPreviewSettings().getSplitEditorLayout();

                        ApplicationManager.getApplication().invokeLater(() -> {
                            if (oldSplitEditorLayout == mySplitEditorLayout) {
                                triggerLayoutChange(newSettings.getDiagramPreviewSettings()
                                        .getSplitEditorLayout(), false);
                            }
                        });
                    }
                };

        ApplicationManager.getApplication().getMessageBus().connect(this)
                .subscribe(DiagramApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
    }

    @NotNull
    private JComponent createComponent() {
        final JBSplitter splitter = new JBSplitter(false, 0.5f, 0.15f, 0.85f);
        splitter.setSplitterProportionKey(MY_PROPORTION_KEY);
        splitter.setFirstComponent(myMainEditor.getComponent());
        splitter.setSecondComponent(mySecondEditor.getComponent());

        myToolbarWrapper = new SplitEditorToolbar(splitter);
        if (myMainEditor instanceof TextEditor) {
            myToolbarWrapper
                    .addGutterToTrack(((EditorGutterComponentEx) ((TextEditor) myMainEditor).getEditor().getGutter()));
        }
        if (mySecondEditor instanceof TextEditor) {
            myToolbarWrapper.addGutterToTrack(
                    ((EditorGutterComponentEx) ((TextEditor) mySecondEditor).getEditor().getGutter()));
        }

        final JPanel result = new JPanel(new BorderLayout());
        result.add(myToolbarWrapper, BorderLayout.NORTH);
        result.add(splitter, BorderLayout.CENTER);
        adjustEditorsVisibility();

        return result;
    }

    public void triggerLayoutChange() {
        final int oldValue = mySplitEditorLayout.ordinal();
        final int n = SplitEditorLayout.values().length;
        final int newValue = (oldValue + n - 1) % n;

        triggerLayoutChange(SplitEditorLayout.values()[newValue], true);
    }

    public void triggerLayoutChange(@NotNull SplitEditorLayout newLayout, boolean requestFocus) {
        if (mySplitEditorLayout == newLayout) {
            return;
        }

        mySplitEditorLayout = newLayout;
        invalidateLayout(requestFocus);
    }

    @NotNull
    public SplitEditorLayout getCurrentEditorLayout() {
        return mySplitEditorLayout;
    }

    private void invalidateLayout(boolean requestFocus) {
        adjustEditorsVisibility();
        myToolbarWrapper.refresh();
        myComponent.repaint();

        if (!requestFocus) {
            return;
        }

        final JComponent focusComponent = getPreferredFocusedComponent();
        if (focusComponent != null) {
            IdeFocusManager.findInstanceByComponent(focusComponent).requestFocus(focusComponent, true);
        }
    }

    private void adjustEditorsVisibility() {
        myMainEditor.getComponent().setVisible(mySplitEditorLayout.showFirst);
        mySecondEditor.getComponent().setVisible(mySplitEditorLayout.showSecond);
    }

    @NotNull
    public E1 getMainEditor() {
        return myMainEditor;
    }

    @NotNull
    public E2 getSecondEditor() {
        return mySecondEditor;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myComponent;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        if (mySplitEditorLayout.showFirst) {
            return myMainEditor.getPreferredFocusedComponent();
        }
        if (mySplitEditorLayout.showSecond) {
            return mySecondEditor.getPreferredFocusedComponent();
        }
        return null;
    }

    @NotNull
    @Override
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return new MyFileEditorState(mySplitEditorLayout.name(), myMainEditor.getState(level),
                mySecondEditor.getState(level));
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        if (state instanceof MyFileEditorState) {
            final MyFileEditorState compositeState = (MyFileEditorState) state;
            if (compositeState.getFirstState() != null) {
                myMainEditor.setState(compositeState.getFirstState());
            }
            if (compositeState.getSecondState() != null) {
                mySecondEditor.setState(compositeState.getSecondState());
            }
        }
    }

    @Override
    public boolean isModified() {
        return myMainEditor.isModified() || mySecondEditor.isModified();
    }

    @Override
    public boolean isValid() {
        return myMainEditor.isValid() && mySecondEditor.isValid();
    }

    @Override
    public void selectNotify() {
        myMainEditor.selectNotify();
        mySecondEditor.selectNotify();
    }

    @Override
    public void deselectNotify() {
        myMainEditor.deselectNotify();
        mySecondEditor.deselectNotify();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        myMainEditor.addPropertyChangeListener(listener);
        mySecondEditor.addPropertyChangeListener(listener);

        final DoublingEventListenerDelegate delegate = myListenersGenerator.addListenerAndGetDelegate(listener);
        myMainEditor.addPropertyChangeListener(delegate);
        mySecondEditor.addPropertyChangeListener(delegate);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        myMainEditor.removePropertyChangeListener(listener);
        mySecondEditor.removePropertyChangeListener(listener);

        final DoublingEventListenerDelegate delegate = myListenersGenerator.removeListenerAndGetDelegate(listener);
        if (delegate != null) {
            myMainEditor.removePropertyChangeListener(delegate);
            mySecondEditor.removePropertyChangeListener(delegate);
        }
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return myMainEditor.getBackgroundHighlighter();
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return myMainEditor.getCurrentLocation();
    }

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder() {
        return myMainEditor.getStructureViewBuilder();
    }

    @Override
    public void dispose() {
        Disposer.dispose(myMainEditor);
        Disposer.dispose(mySecondEditor);
    }

    /**
     * Split editor view types.
     */
    public enum SplitEditorLayout {
        FIRST(true, false, "Show editor only"),
        SECOND(false, true, "Show preview only"),
        SPLIT(true, true, "Show editor and preview");

        public final boolean showFirst;
        public final boolean showSecond;
        public final String presentationName;

        SplitEditorLayout(boolean showFirst, boolean showSecond, String presentationName) {
            this.showFirst = showFirst;
            this.showSecond = showSecond;
            this.presentationName = presentationName;
        }

        public String presentationName() {
            //noinspection ConstantConditions
            return StringUtil.capitalize(StringUtil.substringAfter(presentationName, "Show "));
        }

        @Override
        public String toString() {
            return presentationName;
        }
    }

    /**
     * File editor state implementation.
     */
    public static class MyFileEditorState implements FileEditorState {
        @Nullable
        private final String mySplitLayout;
        @Nullable
        private final FileEditorState myFirstState;
        @Nullable
        private final FileEditorState mySecondState;

        MyFileEditorState(@Nullable String splitLayout, @Nullable FileEditorState firstState,
                          @Nullable FileEditorState secondState) {
            mySplitLayout = splitLayout;
            myFirstState = firstState;
            mySecondState = secondState;
        }

        @Nullable
        public String getSplitLayout() {
            return mySplitLayout;
        }

        @Nullable
        FileEditorState getFirstState() {
            return myFirstState;
        }

        @Nullable
        FileEditorState getSecondState() {
            return mySecondState;
        }

        @Override
        public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
            return otherState instanceof MyFileEditorState && (myFirstState == null || myFirstState
                    .canBeMergedWith(((MyFileEditorState) otherState).myFirstState, level)) && (mySecondState == null
                    || mySecondState.canBeMergedWith(((MyFileEditorState) otherState).mySecondState, level));
        }
    }

    private class DoublingEventListenerDelegate implements PropertyChangeListener {
        @NotNull
        private final PropertyChangeListener myDelegate;

        private DoublingEventListenerDelegate(@NotNull PropertyChangeListener delegate) {
            myDelegate = delegate;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            myDelegate.propertyChange(
                    new PropertyChangeEvent(SplitFileEditor.this, evt.getPropertyName(), evt.getOldValue(),
                            evt.getNewValue()));
        }
    }

    /**
     * File editor listener map.
     */
    private class MyListenersMultimap {
        private final Map<PropertyChangeListener, Pair<Integer, DoublingEventListenerDelegate>> myMap = new HashMap<>();

        @NotNull
        DoublingEventListenerDelegate addListenerAndGetDelegate(@NotNull PropertyChangeListener listener) {
            if (!myMap.containsKey(listener)) {
                myMap.put(listener, Pair.create(1, new DoublingEventListenerDelegate(listener)));
            } else {
                final Pair<Integer, DoublingEventListenerDelegate> oldPair = myMap.get(listener);
                myMap.put(listener, Pair.create(oldPair.getFirst() + 1, oldPair.getSecond()));
            }

            return myMap.get(listener).getSecond();
        }

        @Nullable
        DoublingEventListenerDelegate removeListenerAndGetDelegate(@NotNull PropertyChangeListener listener) {
            final Pair<Integer, DoublingEventListenerDelegate> oldPair = myMap.get(listener);
            if (oldPair == null) {
                return null;
            }

            if (oldPair.getFirst() == 1) {
                myMap.remove(listener);
            } else {
                myMap.put(listener, Pair.create(oldPair.getFirst() - 1, oldPair.getSecond()));
            }
            return oldPair.getSecond();
        }
    }
}
