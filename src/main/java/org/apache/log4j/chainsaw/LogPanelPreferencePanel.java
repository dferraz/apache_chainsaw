/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j.chainsaw;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * GUI panel used to manipulate the PreferenceModel for a Log Panel
 *
 * @author Paul Smith
 */
public class LogPanelPreferencePanel extends AbstractPreferencePanel {
    //~ Instance fields =========================================================

    private final LogPanelPreferenceModel preferenceModel;
    private final ModifiableListModel columnListModel = new ModifiableListModel();
    private static final Logger logger = LogManager.getLogger(LogPanelPreferencePanel.class);
    private ApplicationPreferenceModel appPreferenceModel;

    //~ Constructors ============================================================

    public LogPanelPreferencePanel(LogPanelPreferenceModel model, ApplicationPreferenceModel appModel) {
        preferenceModel = model;
        appPreferenceModel = appModel;
        initComponents();

        getOkButton().addActionListener(e -> hidePanel());

        getCancelButton().addActionListener(e -> hidePanel());
    }

    //~ Methods =================================================================

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("Preferences Panel Test Bed");
        LogPanelPreferenceModel model = new LogPanelPreferenceModel();
        ApplicationPreferenceModel appModel = new ApplicationPreferenceModel();
        LogPanelPreferencePanel panel = new LogPanelPreferencePanel(model, appModel);
        f.getContentPane().add(panel);

        model.addPropertyChangeListener(evt -> logger.warn(evt.toString()));
        panel.setOkCancelActionListener(e -> System.exit(1));

        f.setSize(640, 480);
        f.setVisible(true);
    }

    protected TreeModel createTreeModel() {
        final DefaultMutableTreeNode rootNode =
            new DefaultMutableTreeNode("Preferences");
        DefaultTreeModel model = new DefaultTreeModel(rootNode);

        DefaultMutableTreeNode visuals =
            new DefaultMutableTreeNode(new VisualsPrefPanel());
        DefaultMutableTreeNode formatting =
            new DefaultMutableTreeNode(new FormattingPanel());
        DefaultMutableTreeNode columns =
            new DefaultMutableTreeNode(new ColumnSelectorPanel());

        rootNode.add(visuals);
        rootNode.add(formatting);
        rootNode.add(columns);

        return model;
    }

    //~ Inner Classes ===========================================================

    /**
     * Allows the user to choose which columns to display.
     *
     * @author Paul Smith
     */
    public class ColumnSelectorPanel extends BasicPrefPanel {
        //~ Constructors ==========================================================

        ColumnSelectorPanel() {
            super("Columns");
            initComponents();
        }

        //~ Methods ===============================================================

        private void initComponents() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            Box columnBox = new Box(BoxLayout.Y_AXIS);

            //		columnBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Displayed Columns"));
            final JList columnList = new JList();
            columnList.setVisibleRowCount(17);

            for (Object o : preferenceModel.getColumns()) {
                TableColumn col = (TableColumn) o;
                Enumeration enumeration = columnListModel.elements();
                boolean found = false;
                while (enumeration.hasMoreElements()) {
                    TableColumn thisCol = (TableColumn) enumeration.nextElement();
                    if (thisCol.getHeaderValue().equals(col.getHeaderValue())) {
                        found = true;
                    }
                }
                if (!found) {
                    columnListModel.addElement(col);
                }
            }

            columnList.setModel(columnListModel);

            CheckListCellRenderer cellRenderer = new CheckListCellRenderer() {
                protected boolean isSelected(Object value) {
                    return LogPanelPreferencePanel.this.preferenceModel.isColumnVisible((TableColumn)
                        value);
                }
            };

            columnList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int i = columnList.locationToIndex(e.getPoint());

                    if (i >= 0) {
                        Object column = columnListModel.get(i);
                        preferenceModel.toggleColumn(((TableColumn) column));
                    }
                }
            });
            JButton setAsDefaultsButton = new JButton("Use selected columns as default visible columns");
            setAsDefaultsButton.addActionListener(actionEvent -> {
                List selectedColumns = new ArrayList();
                for (int i = 0; i < columnListModel.getSize(); i++) {
                    if (preferenceModel.isColumnVisible((TableColumn) columnListModel.get(i))) {
                        selectedColumns.add(((TableColumn) columnListModel.get(i)).getHeaderValue());
                    }
                }
                appPreferenceModel.setDefaultColumnNames(selectedColumns);
            });
            columnList.setCellRenderer(cellRenderer);
            columnBox.add(new JScrollPane(columnList));
            columnBox.add(Box.createVerticalStrut(5));
            columnBox.add(setAsDefaultsButton);
            add(columnBox);
            add(Box.createVerticalGlue());
        }
    }

    /**
     * Provides preference gui's for all the Formatting options
     * available for the columns etc.
     */
    private class FormattingPanel extends BasicPrefPanel {
        //~ Instance fields =======================================================

        private JTextField customFormatText = new JTextField("", 10);
        private JTextField loggerPrecision = new JTextField(10);
        private JRadioButton rdCustom = new JRadioButton("Custom Format ");
        private final JRadioButton rdISO =
            new JRadioButton(
                "<html><b>Fast</b> ISO 8601 format (yyyy-MM-dd HH:mm:ss) </html>");
        private final JTextField timeZone = new JTextField(10);
        private final JRadioButton rdLevelIcons = new JRadioButton("Icons ");
        private final JRadioButton rdLevelText = new JRadioButton("Text ");
        private JRadioButton rdLast;

        //~ Constructors ==========================================================

        private FormattingPanel() {
            super("Formatting");
            this.initComponents();
            setupListeners();
        }

        //~ Methods ===============================================================

        private void initComponents() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel dateFormatPanel = new JPanel();
            dateFormatPanel.setLayout(new BoxLayout(dateFormatPanel, BoxLayout.Y_AXIS));
            dateFormatPanel.setBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Timestamp"));

            ButtonGroup bgDateFormat = new ButtonGroup();

            rdISO.setSelected(preferenceModel.isUseISO8601Format());

            rdISO.setHorizontalTextPosition(SwingConstants.RIGHT);
            rdISO.setAlignmentX(Component.LEFT_ALIGNMENT);

            bgDateFormat.add(rdISO);
            dateFormatPanel.add(rdISO);

            for (Object DATE_FORMAT : LogPanelPreferenceModel.DATE_FORMATS) {
                final String format = (String) DATE_FORMAT;
                final JRadioButton rdFormat = new JRadioButton(format);
                rdFormat.setHorizontalTextPosition(SwingConstants.RIGHT);
                rdFormat.setAlignmentX(Component.LEFT_ALIGNMENT);

                rdFormat.addActionListener(e -> {
                    preferenceModel.setDateFormatPattern(format);
                    customFormatText.setEnabled(rdCustom.isSelected());
                    rdLast = rdFormat;
                });
                //update based on external changes to dateformatpattern (column context
                //menu)
                preferenceModel.addPropertyChangeListener(
                    "dateFormatPattern", evt -> {
                        rdFormat.setSelected(
                            preferenceModel.getDateFormatPattern().equals(format));
                        rdLast = rdFormat;
                    });

                dateFormatPanel.add(rdFormat);
                bgDateFormat.add(rdFormat);
            }

            customFormatText.setPreferredSize(new Dimension(100, 20));
            customFormatText.setMaximumSize(customFormatText.getPreferredSize());
            customFormatText.setMinimumSize(customFormatText.getPreferredSize());
            customFormatText.setEnabled(false);

            bgDateFormat.add(rdCustom);
            rdCustom.setSelected(preferenceModel.isCustomDateFormat());

            // add a custom date format
            if (preferenceModel.isCustomDateFormat()) {
                customFormatText.setText(preferenceModel.getDateFormatPattern());
                customFormatText.setEnabled(true);
            }

            JPanel customPanel = new JPanel();
            customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.X_AXIS));
            customPanel.add(rdCustom);
            customPanel.add(customFormatText);
            customPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            dateFormatPanel.add(customPanel);
            dateFormatPanel.add(Box.createVerticalStrut(5));

            JLabel dateFormatLabel = new JLabel("Time zone of events (or blank for local time zone");
            dateFormatPanel.add(dateFormatLabel);

            timeZone.setMaximumSize(timeZone.getPreferredSize());
            dateFormatPanel.add(Box.createVerticalStrut(5));
            dateFormatPanel.add(timeZone);

            add(dateFormatPanel);

            JPanel levelFormatPanel = new JPanel();
            levelFormatPanel.setLayout(
                new BoxLayout(levelFormatPanel, BoxLayout.Y_AXIS));
            levelFormatPanel.setBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Level"));
            levelFormatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            ButtonGroup bgLevel = new ButtonGroup();
            bgLevel.add(rdLevelIcons);
            bgLevel.add(rdLevelText);

            rdLevelIcons.setSelected(preferenceModel.isLevelIcons());

            levelFormatPanel.add(rdLevelIcons);
            levelFormatPanel.add(rdLevelText);

            add(levelFormatPanel);

            JPanel loggerFormatPanel = new JPanel();
            loggerFormatPanel.setLayout(
                new BoxLayout(loggerFormatPanel, BoxLayout.Y_AXIS));
            loggerFormatPanel.setBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Logger"));
            loggerFormatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            loggerFormatPanel.add(Box.createVerticalStrut(3));

            final JLabel precisionLabel =
                new JLabel("Number of package levels to hide (or blank to display full logger)");

            loggerFormatPanel.add(precisionLabel);
            loggerFormatPanel.add(Box.createVerticalStrut(5));

            loggerPrecision.setMaximumSize(loggerPrecision.getPreferredSize());
            loggerFormatPanel.add(loggerPrecision);

            add(loggerFormatPanel);
        }

        /*
         * Restore text fields to current model values
         */
        private void reset() {

            if (preferenceModel.isCustomDateFormat()) {
                customFormatText.setText(preferenceModel.getDateFormatPattern());
            } else {
                if (rdLast != null) {
                    rdLast.setSelected(true);
                }
                customFormatText.setEnabled(false);
            }

            loggerPrecision.setText(preferenceModel.getLoggerPrecision());
            timeZone.setText(preferenceModel.getTimeZone());
        }

        /*
         * Commit text fields to model
         */
        private void commit() {
            if (rdCustom.isSelected()) {
                preferenceModel.setDateFormatPattern(customFormatText.getText());
            }
            preferenceModel.setLoggerPrecision(loggerPrecision.getText());
            preferenceModel.setTimeZone(timeZone.getText());
        }

        /**
         * DOCUMENT ME!
         */
        private void setupListeners() {
            getOkButton().addActionListener(evt -> commit());

            getCancelButton().addActionListener(evt -> reset());

            rdCustom.addActionListener(e -> {
                customFormatText.setEnabled(rdCustom.isSelected());
                customFormatText.grabFocus();
            });

            //a second?? listener for dateformatpattern
            preferenceModel.addPropertyChangeListener(
                "dateFormatPattern", evt -> {
                    /**
                     * we need to make sure we are not reacting to the user typing, so only do this
                     * if the text box is not the same as the model
                     */
                    if (
                        preferenceModel.isCustomDateFormat()
                            && !customFormatText.getText().equals(
                            evt.getNewValue().toString())) {
                        customFormatText.setText(preferenceModel.getDateFormatPattern());
                        rdCustom.setSelected(true);
                        customFormatText.setEnabled(true);
                    } else {
                        rdCustom.setSelected(false);
                    }
                });

            rdISO.addActionListener(e -> {
                preferenceModel.setDateFormatPattern("ISO8601");
                customFormatText.setEnabled(rdCustom.isSelected());
                rdLast = rdISO;
            });
            preferenceModel.addPropertyChangeListener(
                "dateFormatPattern", evt -> {
                    rdISO.setSelected(preferenceModel.isUseISO8601Format());
                    rdLast = rdISO;
                });
            preferenceModel.addPropertyChangeListener(
                "dateFormatTimeZone", evt -> timeZone.setText(preferenceModel.getTimeZone())
            );

            ActionListener levelIconListener = e -> preferenceModel.setLevelIcons(rdLevelIcons.isSelected());

            rdLevelIcons.addActionListener(levelIconListener);
            rdLevelText.addActionListener(levelIconListener);

            preferenceModel.addPropertyChangeListener(
                "levelIcons", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    rdLevelIcons.setSelected(value);
                    rdLevelText.setSelected(!value);
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @author psmith
     * @version $Revision$, $Date$
     */
    private class VisualsPrefPanel extends BasicPrefPanel {
        //~ Instance fields =======================================================

        private final JCheckBox detailPanelVisible =
            new JCheckBox("Show Event Detail panel");

        private final JCheckBox loggerTreePanel =
            new JCheckBox("Show Logger Tree");
        private final JCheckBox wrapMessage = new JCheckBox("Wrap message field (display multi-line rows) ");
        private final JCheckBox searchResultsVisible = new JCheckBox("Display find results in details panel ");
        private final JCheckBox highlightSearchMatchText = new JCheckBox("Highlight find match text ");
        private final JCheckBox scrollToBottom =
            new JCheckBox("Scroll to bottom (view tracks with new events)");
        private final JCheckBox showMillisDeltaAsGap =
            new JCheckBox("Display timestamp delta between events as row gap");
        private final JCheckBox toolTips =
            new JCheckBox("Show Event Detail Tooltips");
        private final JCheckBox thumbnailBarToolTips =
            new JCheckBox("Show Thumbnail Bar Tooltips");
        private final JEditorPane clearTableExpression = new JEditorPane();

        //~ Constructors ==========================================================

        /**
         * Creates a new VisualsPrefPanel object.
         */
        private VisualsPrefPanel() {
            super("Visuals");
            initPanelComponents();
            setupListeners();
        }

        //~ Methods ===============================================================

        /**
         * DOCUMENT ME!
         */
        private void initPanelComponents() {
            JTextComponentFormatter.applySystemFontAndSize(clearTableExpression);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            toolTips.setAlignmentX(Component.LEFT_ALIGNMENT);
            thumbnailBarToolTips.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailPanelVisible.setAlignmentX(Component.LEFT_ALIGNMENT);
            loggerTreePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            scrollToBottom.setAlignmentX(Component.LEFT_ALIGNMENT);
            showMillisDeltaAsGap.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(toolTips);
            add(thumbnailBarToolTips);
            add(detailPanelVisible);
            add(loggerTreePanel);
            add(scrollToBottom);
            add(wrapMessage);
            add(highlightSearchMatchText);
            add(searchResultsVisible);
            add(showMillisDeltaAsGap);
            JPanel clearPanel = new JPanel(new BorderLayout());
            clearPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            clearPanel.add(new JLabel("Clear all events if expression matches"), BorderLayout.NORTH);
            clearTableExpression.setText(preferenceModel.getClearTableExpression());
            clearTableExpression.setPreferredSize(new Dimension(300, 50));
            JPanel clearTableScrollPanel = new JPanel(new BorderLayout());
            clearTableScrollPanel.add(new JScrollPane(clearTableExpression), BorderLayout.NORTH);
            clearPanel.add(clearTableScrollPanel, BorderLayout.CENTER);
            add(clearPanel);

            toolTips.setSelected(preferenceModel.isToolTips());
            thumbnailBarToolTips.setSelected(preferenceModel.isThumbnailBarToolTips());
            detailPanelVisible.setSelected(preferenceModel.isDetailPaneVisible());
            searchResultsVisible.setSelected(preferenceModel.isSearchResultsVisible());
            loggerTreePanel.setSelected(preferenceModel.isLogTreePanelVisible());
        }

        /**
         * DOCUMENT ME!
         */
        private void setupListeners() {
            ActionListener wrapMessageListener = e -> preferenceModel.setWrapMessage(wrapMessage.isSelected());

            wrapMessage.addActionListener(wrapMessageListener);

            ActionListener searchResultsVisibleListener = e -> preferenceModel.setSearchResultsVisible(searchResultsVisible.isSelected());

            searchResultsVisible.addActionListener(searchResultsVisibleListener);

            ActionListener highlightSearchMatchTextListener = e -> preferenceModel.setHighlightSearchMatchText(highlightSearchMatchText.isSelected());

            highlightSearchMatchText.addActionListener(highlightSearchMatchTextListener);

            preferenceModel.addPropertyChangeListener(
                "wrapMessage", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    wrapMessage.setSelected(value);
                });

            preferenceModel.addPropertyChangeListener(
                "searchResultsVisible", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    searchResultsVisible.setSelected(value);
                });

            preferenceModel.addPropertyChangeListener(
                "highlightSearchMatchText", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    highlightSearchMatchText.setSelected(value);
                });

            toolTips.addActionListener(e -> preferenceModel.setToolTips(toolTips.isSelected()));

            thumbnailBarToolTips.addActionListener(e -> preferenceModel.setThumbnailBarToolTips(thumbnailBarToolTips.isSelected()));

            getOkButton().addActionListener(e -> preferenceModel.setClearTableExpression(clearTableExpression.getText().trim()));

            preferenceModel.addPropertyChangeListener(
                "toolTips", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    toolTips.setSelected(value);
                });

            preferenceModel.addPropertyChangeListener(
                "thumbnailBarToolTips", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    thumbnailBarToolTips.setSelected(value);
                });

            detailPanelVisible.addActionListener(e -> preferenceModel.setDetailPaneVisible(detailPanelVisible.isSelected()));

            preferenceModel.addPropertyChangeListener(
                "detailPaneVisible", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    detailPanelVisible.setSelected(value);
                });

            scrollToBottom.addActionListener(e -> preferenceModel.setScrollToBottom(scrollToBottom.isSelected()));

            showMillisDeltaAsGap.addActionListener(e -> preferenceModel.setShowMillisDeltaAsGap(showMillisDeltaAsGap.isSelected()));

            preferenceModel.addPropertyChangeListener("showMillisDeltaAsGap", evt -> {
                boolean value = (Boolean) evt.getNewValue();
                showMillisDeltaAsGap.setSelected(value);
            });
            preferenceModel.addPropertyChangeListener(
                "scrollToBottom", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    scrollToBottom.setSelected(value);
                });

            loggerTreePanel.addActionListener(e -> preferenceModel.setLogTreePanelVisible(loggerTreePanel.isSelected()));

            preferenceModel.addPropertyChangeListener(
                "logTreePanelVisible", evt -> {
                    boolean value = (Boolean) evt.getNewValue();
                    loggerTreePanel.setSelected(value);
                });

            preferenceModel.addPropertyChangeListener("columns", evt -> {
                List cols = (List) evt.getNewValue();
                for (Object col1 : cols) {
                    TableColumn col = (TableColumn) col1;
                    Enumeration enumeration = columnListModel.elements();
                    boolean found = false;
                    while (enumeration.hasMoreElements()) {
                        TableColumn thisCol = (TableColumn) enumeration.nextElement();
                        if (thisCol.getHeaderValue().equals(col.getHeaderValue())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        columnListModel.addElement(col);
                        columnListModel.fireContentsChanged();
                    }
                }
            });

            preferenceModel.addPropertyChangeListener(
                "visibleColumns", evt -> columnListModel.fireContentsChanged());

            preferenceModel.addPropertyChangeListener("clearTableExpression", evt -> clearTableExpression.setText(((LogPanelPreferenceModel) evt.getSource()).getClearTableExpression()));
        }
    }
}
