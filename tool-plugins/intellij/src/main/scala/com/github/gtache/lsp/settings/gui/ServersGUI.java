package com.github.gtache.lsp.settings.gui;

import com.github.gtache.lsp.PluginMain$;
import com.github.gtache.lsp.client.languageserver.serverdefinition.*;
import com.github.gtache.lsp.settings.LSPState;
import com.github.gtache.lsp.utils.Utils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The GUI for the LSP ServerDefinition settings
 */ //TODO improve
public final class ServersGUI implements LSPGUI {

    private static final String EXT_LABEL = "Extension";
    private static final String EXT_TOOLTIP = "e.g. scala, java, c, js, ...";
    private static final String EXT = "ext";
    private static final String MAINCLASS = "mainclass";
    private static final String ARGS = "args";
    private static final String PACKGE = "packge";
    private static final String COMMAND = "command";
    private static final String PATH = "path";
    private static final Logger LOG = Logger.getInstance(ServersGUI.class);
    private static final String FILE_PATH_LABEL = "Path";
    private final LSPState state = state();
    private final JPanel rootPanel;
    private final List<ServersGUIRow> rows = new ArrayList<>(5);
    private final Map<String, UserConfigurableServerDefinition> serverDefinitions = new LinkedHashMap<>(5);

    public ServersGUI() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.add(createArtifactRow("", "", "", ""));
    }

    @Override
    public JPanel getRootPanel() {
        return rootPanel;
    }

    public Collection<ServersGUIRow> getRows() {
        return rows;
    }

    public void clear() {
        rows.clear();
        serverDefinitions.clear();
        rootPanel.removeAll();
    }

    public void addServerDefinition(final UserConfigurableServerDefinition serverDefinition) {
        if (serverDefinition != null) {
            serverDefinitions.put(serverDefinition.ext(), serverDefinition);
            if (serverDefinition.getClass().equals(ArtifactLanguageServerDefinition.class)) {
                final ArtifactLanguageServerDefinition def = (ArtifactLanguageServerDefinition) serverDefinition;
                rootPanel.add(createArtifactRow(def.ext(), def.packge(), def.mainClass(), Utils.arrayToString(def.args(), " ")));
            } else if (serverDefinition.getClass().equals(ExeLanguageServerDefinition.class)) {
                final ExeLanguageServerDefinition def = (ExeLanguageServerDefinition) serverDefinition;
                rootPanel.add(createExeRow(def.ext(), def.path(), Utils.arrayToString(def.args(), " ")));
            } else if (serverDefinition.getClass().equals(RawCommandServerDefinition.class)) {
                final RawCommandServerDefinition def = (RawCommandServerDefinition) serverDefinition;
                rootPanel.add(createCommandRow(def.ext(), Utils.arrayToString(def.command(), " ")));
            } else {
                LOG.error("Unknown UserConfigurableServerDefinition : " + serverDefinition);
            }
        }
    }

    @Override
    public void apply() {
        final List<String> extensions = rows.stream().map(row -> row.getText(EXT)).collect(Collectors.toList());
        final Set<String> distinct = new ArrayList<>(extensions).stream().distinct().collect(Collectors.toSet());
        distinct.forEach(extensions::remove);
        if (!extensions.isEmpty()) {
            Messages.showWarningDialog(extensions.stream().reduce((f, s) -> "Duplicate : " + f + Utils.lineSeparator() + s).orElse("Error while getting extensions") + Utils.lineSeparator() + "Unexpected behavior may occur", "Duplicate Extensions");
        }
        //TODO manage without restarting
        //Messages.showInfoMessage("The changes will be applied after restarting the IDE.", "LSP Settings");
        serverDefinitions.clear();
        for (final ServersGUIRow row : rows) {
            final String[] arr = row.toStringArray();
            final String ext = row.getText(EXT);
            final UserConfigurableServerDefinition serverDefinition = UserConfigurableServerDefinition$.MODULE$.fromArray(arr);
            if (serverDefinition != null) {
                serverDefinitions.put(ext, serverDefinition);
            }
        }
        LSPState.getInstance().setExtToServ(serverDefinitions);
        PluginMain$.MODULE$.setExtToServerDefinition(serverDefinitions);
    }

    @Override
    public boolean isModified() {
        if (serverDefinitions.size() == rows.stream().filter(row -> Arrays.stream(row.toStringArray()).skip(1).anyMatch(s -> s != null && !s.isEmpty())).collect(Collectors.toList()).size()) {
            for (final ServersGUIRow row : rows) {
                final UserConfigurableServerDefinition stateDef = serverDefinitions.get(row.getText(EXT));
                final UserConfigurableServerDefinition rowDef = UserConfigurableServerDefinition$.MODULE$.fromArray(row.toStringArray());
                if (rowDef != null && !rowDef.equals(stateDef)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void reset() {
        this.clear();
        if (state.getExtToServ() != null && !state.getExtToServ().isEmpty()) {
            for (final UserConfigurableServerDefinition serverDefinition : state.getExtToServ().values()) {
                addServerDefinition(serverDefinition);
            }
        } else {
            rootPanel.add(createArtifactRow("", "", "", ""));
        }
    }

    private JComboBox<String> createComboBox(final JPanel panel, final String selected) {
        final JComboBox<String> typeBox = new ComboBox<>();
        final ConfigurableTypes[] types = ConfigurableTypes.values();
        for (final ConfigurableTypes type : types) {
            typeBox.addItem(type.getTyp());
        }
        typeBox.setSelectedItem(selected);
        typeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final int idx = getComponentIndex(panel);
                if (e.getItem().equals(ConfigurableTypes.ARTIFACT.getTyp())) {
                    rootPanel.add(createArtifactRow("", "", "", ""), idx);
                    rootPanel.remove(panel);
                    rows.remove(idx);
                } else if (e.getItem().equals(ConfigurableTypes.RAWCOMMAND.getTyp())) {
                    rootPanel.add(createCommandRow("", ""), idx);
                    rootPanel.remove(panel);
                    rows.remove(idx);
                } else if (e.getItem().equals(ConfigurableTypes.EXE.getTyp())) {
                    rootPanel.add(createExeRow("", "", ""), idx);
                    rootPanel.remove(panel);
                    rows.remove(idx);
                } else {
                    LOG.error("Unknown type : " + e.getItem());
                }
            }
        });
        return typeBox;
    }

    private JButton createNewRowButton() {
        final JButton newRowButton = new JButton();
        newRowButton.setText("+");
        newRowButton.addActionListener(e -> rootPanel.add(createArtifactRow("", "", "", "")));
        return newRowButton;
    }

    private JButton createRemoveRowButton(final JPanel panel) {
        final JButton removeRowButton = new JButton();
        removeRowButton.setText("-");
        removeRowButton.addActionListener(e -> {
            final int idx = getComponentIndex(panel);
            rootPanel.remove(panel);
            rows.remove(idx);
        });
        return removeRowButton;
    }

    private JPanel createRow(final Collection<JComponent> labelFields, final String selectedItem) {
        final JPanel panel = new JPanel();
        int colIdx = 0;
        panel.setLayout(new GridLayoutManager(2, 17, JBUI.emptyInsets(), -1, -1));

        panel.add(new Spacer(), new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(0, 10), new Dimension(0, 10), new Dimension(0, 10), 0, false));
        final JComboBox<String> typeBox = createComboBox(panel, selectedItem);
        panel.add(typeBox, new GridConstraints(0, colIdx++, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        final Iterator<JComponent> iterator = labelFields.iterator();
        while (iterator.hasNext()) {
            final JComponent label = iterator.next();
            final JComponent field = iterator.next();
            panel.add(new Spacer(), new GridConstraints(0, colIdx++, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
            panel.add(label, new GridConstraints(0, colIdx++, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            panel.add(field, new GridConstraints(0, colIdx++, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        }

        panel.add(new Spacer(), new GridConstraints(0, 14, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JButton newRowButton = createNewRowButton();
        panel.add(newRowButton, new GridConstraints(0, 15, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        if (rows.isEmpty()) {
            panel.add(new Spacer(), new GridConstraints(0, 16, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        } else {
            final JButton removeRowButton = createRemoveRowButton(panel);
            panel.add(removeRowButton, new GridConstraints(0, 16, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        }

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel createArtifactRow(final String ext, final String serv, final String mainClass, final String args) {
        final JLabel extLabel = new JLabel(EXT_LABEL);
        final JTextField extField = new JTextField();
        extField.setToolTipText(EXT_TOOLTIP);
        extField.setText(ext);
        final JLabel packgeLabel = new JLabel("Artifact");
        final JTextField packgeField = new JTextField();
        packgeField.setToolTipText("e.g. ch.epfl.lamp:dotty-language-server_0.3:0.3.0-RC2");
        packgeField.setText(serv);
        final JLabel mainClassLabel = new JLabel("Main class");
        final JTextField mainClassField = new JTextField();
        mainClassField.setToolTipText("e.g. dotty.tools.languageserver.Main");
        mainClassField.setText(mainClass);
        final JLabel argsLabel = new JLabel("Args");
        final JTextField argsField = new JTextField();
        argsField.setToolTipText("e.g. -stdio");
        argsField.setText(args);

        final List<JComponent> components = Arrays.asList(extLabel, extField, packgeLabel, packgeField, mainClassLabel, mainClassField, argsLabel, argsField);
        final JPanel panel = createRow(components, ArtifactLanguageServerDefinition$.MODULE$.getPresentableTyp());
        final scala.collection.mutable.LinkedHashMap<String, JComponent> map = new scala.collection.mutable.LinkedHashMap<>();
        map.put(EXT, extField);
        map.put(PACKGE, packgeField);
        map.put(MAINCLASS, mainClassField);
        map.put(ARGS, argsField);
        rows.add(new ServersGUIRow(panel, ArtifactLanguageServerDefinition$.MODULE$.typ(), map));
        return panel;
    }

    private JPanel createExeRow(final String ext, final String path, final String args) {
        final JLabel extLabel = new JLabel(EXT_LABEL);
        final JTextField extField = new JTextField();
        extField.setToolTipText(EXT_TOOLTIP);
        extField.setText(ext);
        final JLabel pathLabel = new JLabel(FILE_PATH_LABEL);
        final TextFieldWithBrowseButton pathField = new TextFieldWithBrowseButton();
        pathField.setToolTipText("e.g. C:\\rustLS\\rls.exe");
        pathField.setText(path);
        pathField.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(true, false, true, true, true, false).withShowHiddenFiles(true)));
        final JLabel argsLabel = new JLabel("Args");
        final JTextField argsField = new JTextField();
        argsField.setToolTipText("e.g. -stdio");
        argsField.setText(args);

        final List<JComponent> components = Arrays.asList(extLabel, extField, pathLabel, pathField, argsLabel, argsField);
        final JPanel panel = createRow(components, ExeLanguageServerDefinition$.MODULE$.getPresentableTyp());
        final scala.collection.mutable.LinkedHashMap<String, JComponent> map = new scala.collection.mutable.LinkedHashMap<>();
        map.put(EXT, extField);
        map.put(PATH, pathField);
        map.put(ARGS, argsField);
        rows.add(new ServersGUIRow(panel, ExeLanguageServerDefinition$.MODULE$.typ(), map));
        return panel;
    }

    private JPanel createCommandRow(final String ext, final String command) {
        final JLabel extLabel = new JLabel(EXT_LABEL);
        final JTextField extField = new JTextField();
        extField.setToolTipText(EXT_TOOLTIP);
        extField.setText(ext);
        final JLabel commandLabel = new JLabel("Command");
        final TextFieldWithBrowseButton commandField = new TextFieldWithBrowseButton();
        commandField.setText(command);
        commandField.setToolTipText("e.g. python.exe -m C:\\python-ls\\pyls");
        commandField.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(true, false, true, true, true, false).withShowHiddenFiles(true)));

        final List<JComponent> components = Arrays.asList(extLabel, extField, commandLabel, commandField);
        final JPanel panel = createRow(components, RawCommandServerDefinition$.MODULE$.getPresentableTyp());
        final scala.collection.mutable.LinkedHashMap<String, JComponent> map = new scala.collection.mutable.LinkedHashMap<>();
        map.put(EXT, extField);
        map.put(COMMAND, commandField);
        rows.add(new ServersGUIRow(panel, RawCommandServerDefinition$.MODULE$.typ(), map));
        return panel;
    }

    private int getComponentIndex(final JComponent component) {
        for (int i = 0; i < rootPanel.getComponentCount(); ++i) {
            if (rootPanel.getComponent(i).equals(component)) {
                return i;
            }
        }
        return -1;
    }
}
