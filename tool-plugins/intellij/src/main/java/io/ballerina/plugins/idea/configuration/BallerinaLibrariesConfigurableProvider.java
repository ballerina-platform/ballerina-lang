/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.configuration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.CompositeConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HideableDecorator;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.project.BallerinaApplicationLibrariesService;
import io.ballerina.plugins.idea.project.BallerinaProjectLibrariesService;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Responsible for providing library configurations.
 */
public class BallerinaLibrariesConfigurableProvider extends ConfigurableProvider {

    @NotNull
    private final Project myProject;

    public BallerinaLibrariesConfigurableProvider(@NotNull Project project) {
        myProject = project;
    }

    @Nullable
    @Override
    public Configurable createConfigurable() {
        return createConfigurable(false);
    }

    @Nullable
    private Configurable createConfigurable(boolean dialogMode) {
        return new CompositeConfigurable<UnnamedConfigurable>() {

            @Nullable
            @Override
            public JComponent createComponent() {
                List<UnnamedConfigurable> configurables = getConfigurables();
                Collection<HideableDecorator> hideableDecorators = ContainerUtil.newHashSet();

                GridLayoutManager layoutManager = new GridLayoutManager(configurables.size() + 1, 1, new Insets(0, 0,
                        0, 0), -1, -1);
                JPanel rootPanel = new JPanel(layoutManager);
                Spacer spacer = new Spacer();
                rootPanel.add(spacer, new GridConstraints(configurables.size(), 0, 1, 1, GridConstraints.ANCHOR_SOUTH,
                        GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));

                for (int i = 0; i < configurables.size(); i++) {
                    UnnamedConfigurable configurable = configurables.get(i);
                    JComponent configurableComponent = configurable.createComponent();
                    assert configurableComponent != null;
                    JPanel hideablePanel = new JPanel(new BorderLayout());

                    rootPanel.add(hideablePanel, configurableConstrains(i));

                    if (configurable instanceof Configurable) {
                        String displayName = ((Configurable) configurable).getDisplayName();
                        ListenableHideableDecorator decorator = new ListenableHideableDecorator(hideablePanel,
                                displayName, configurableComponent);
                        decorator.addListener(new MyHideableDecoratorListener(layoutManager, hideablePanel,
                                spacer, hideableDecorators,
                                configurableExpandedPropertyKey((Configurable) configurable)
                        ));
                        hideableDecorators.add(decorator);
                        decorator.setOn(isConfigurableExpanded(i, (Configurable) configurable));
                    }
                }
                if (dialogMode) {
                    rootPanel.setPreferredSize(new Dimension(400, 600));
                }
                rootPanel.revalidate();
                return rootPanel;
            }

            @NotNull
            @Override
            protected List<UnnamedConfigurable> createConfigurables() {
                List<UnnamedConfigurable> result = ContainerUtil.newArrayList();
                String[] urlsFromEnv =
                        ContainerUtil.map2Array(BallerinaSdkUtils.getBallerinaPathsRootsFromEnvironment(), String.class,
                                VirtualFile::getUrl);
                result.add(new BallerinaLibrariesConfigurable("Global libraries",
                        BallerinaApplicationLibrariesService.getInstance(), urlsFromEnv));
                if (!myProject.isDefault()) {
                    result.add(new BallerinaLibrariesConfigurable("Project libraries",
                            BallerinaProjectLibrariesService.getInstance(myProject)));
                }
                return result;
            }

            @NotNull
            @Nls
            @Override
            public String getDisplayName() {
                return "Ballerina Libraries";
            }

            @Nullable
            @Override
            public String getHelpTopic() {
                return null;
            }

            @NotNull
            private GridConstraints configurableConstrains(int i) {
                return new GridConstraints(i, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW |
                                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                        GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                        null, null, null);
            }

            private boolean isConfigurableExpanded(int index, @NotNull Configurable configurable) {
                return PropertiesComponent.getInstance(myProject)
                        .getBoolean(configurableExpandedPropertyKey(configurable), index < 2);
            }

            private void storeConfigurableExpandedProperty(@NotNull String storeKey, @NotNull Boolean value) {
                PropertiesComponent.getInstance(myProject).setValue(storeKey, value.toString());
            }

            private String configurableExpandedPropertyKey(@NotNull Configurable configurable) {
                String keyName = "configurable " + configurable.getDisplayName() +
                        " is expanded".toLowerCase(Locale.US);
                return StringUtil.replaceChar(keyName, ' ', '.');
            }

            class MyHideableDecoratorListener extends ListenableHideableDecorator.MyListener {
                private final GridLayoutManager myLayoutManager;
                private final JPanel myHideablePanel;
                @NotNull
                private final String myStoreKey;
                private final Spacer mySpacer;
                private final Collection<HideableDecorator> myHideableDecorators;

                public MyHideableDecoratorListener(@NotNull GridLayoutManager layoutManager,
                                                   @NotNull JPanel hideablePanel,
                                                   @NotNull Spacer spacer,
                                                   @NotNull Collection<HideableDecorator> hideableDecorators,
                                                   @NotNull String storeKey) {
                    myLayoutManager = layoutManager;
                    myHideablePanel = hideablePanel;
                    myStoreKey = storeKey;
                    mySpacer = spacer;
                    myHideableDecorators = hideableDecorators;
                }

                @Override
                public void on() {
                    GridConstraints c = myLayoutManager.getConstraintsForComponent(myHideablePanel);
                    c.setVSizePolicy(c.getVSizePolicy() | GridConstraints.SIZEPOLICY_WANT_GROW);

                    GridConstraints spacerConstraints = myLayoutManager.getConstraintsForComponent(mySpacer);
                    spacerConstraints.setVSizePolicy(spacerConstraints.getVSizePolicy() &
                            ~GridConstraints.SIZEPOLICY_WANT_GROW);

                    storeConfigurableExpandedProperty(myStoreKey, Boolean.TRUE);
                }

                @Override
                public void beforeOff() {
                    GridConstraints c = myLayoutManager.getConstraintsForComponent(myHideablePanel);
                    c.setVSizePolicy(c.getVSizePolicy() & ~GridConstraints.SIZEPOLICY_WANT_GROW);
                }

                @Override
                public void afterOff() {
                    if (isAllDecoratorsCollapsed()) {
                        GridConstraints c = myLayoutManager.getConstraintsForComponent(mySpacer);
                        c.setVSizePolicy(c.getVSizePolicy() | GridConstraints.SIZEPOLICY_WANT_GROW);
                    }
                    storeConfigurableExpandedProperty(myStoreKey, Boolean.FALSE);
                }

                private boolean isAllDecoratorsCollapsed() {
                    for (HideableDecorator hideableDecorator : myHideableDecorators) {
                        if (hideableDecorator.isExpanded()) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        };
    }

    public static void showModulesConfigurable(@NotNull Project project) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (!project.isDisposed()) {
            Configurable configurable = new BallerinaLibrariesConfigurableProvider(project).createConfigurable(true);
            ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
        }
    }
}
