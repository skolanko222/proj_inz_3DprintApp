/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.gui_proj_inz;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import connection.ControlPrinter;
import gcode.GcodeFileReader;
import gcode.GcodeObject;
import gcode.Point;
import gcode.previewer.SimpleGLCanvas;
import connection.transmiter.BaseTransmHandler;
import connection.PrinterSettings;

import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author User
 */
public class MainGui extends javax.swing.JFrame {

    BaseTransmHandler baseTransmHandler = null;
    PrinterSettings printerSettings = new PrinterSettings(9600, null);
    ControlPrinter controlPrinter = null;
    SimpleGLCanvas renderer;
    GLJPanel glCanvas;
    FPSAnimator animator;
    GcodeFileReader gcodeFileReader = null;

    Integer desiredTempExt = null;
    Integer desiredTempBed = null;
    Integer desiredFanSpeed = null;

    Float actualTempExt = null;
    Float actualTempBed = null;
    Float actualFanSpeed = null;

    int [] layerStarts = new int[2];
    /**
     * Creates new form MainGui
     */
    public MainGui() {
        initComponents();
        addMenuActions();
        updateVisibilityOnConnection();
        updateVisibilityOnFileLoad();

        //gcode preview
        GLProfile profile = GLProfile.getDefault();
        GLCapabilities capabilities = new GLCapabilities(profile);
        glCanvas = new GLJPanel(capabilities);
        oneLayerCheckBox.addActionListener(this::oneLayerCheckBoxActionPerformed);
        rangeLayerCheckBox.addActionListener(this::rangeLayerCheckBoxActionPerformed);
        idleMoveCheckBox.addActionListener(this::idleMoveCheckBoxActionPerformed);
        //rotation
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent ke) {
                float DELTA_SIZE = 1.1F;
                System.out.println("Key pressed: " + ke.getKeyCode());
                switch (ke.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        renderer.getRotation().setX(renderer.getRotation().getX() - DELTA_SIZE);
                        break;
                    case KeyEvent.VK_RIGHT:
                        renderer.getRotation().setX(renderer.getRotation().getX() + DELTA_SIZE);
                        break;
                    case KeyEvent.VK_UP:
                        renderer.getRotation().setY(renderer.getRotation().getY() - DELTA_SIZE);
                        break;
                    case KeyEvent.VK_DOWN:
                        renderer.getRotation().setY(renderer.getRotation().getY() + DELTA_SIZE);
                        break;
                    case KeyEvent.VK_W:
                        Point eye = renderer.getEye();
                        renderer.setEye(new Point(renderer.getEye().getX(), renderer.getEye().getY(), renderer.getEye().getZ() + DELTA_SIZE));
                        break;
                    case KeyEvent.VK_S:
                        renderer.setEye(new Point(renderer.getEye().getX(), renderer.getEye().getY(), renderer.getEye().getZ() - DELTA_SIZE));
                        break;
                    case KeyEvent.VK_A:
                        renderer.setEye(new Point(renderer.getEye().getX() - DELTA_SIZE, renderer.getEye().getY(), renderer.getEye().getZ()));
                        break;
                    case KeyEvent.VK_D:
                        renderer.setEye(new Point(renderer.getEye().getX() + DELTA_SIZE, renderer.getEye().getY(), renderer.getEye().getZ()));
                        break;
                    case KeyEvent.VK_R:
                        renderer.setEye(new Point(renderer.getEye().getX(), renderer.getEye().getY() + DELTA_SIZE, renderer.getEye().getZ()));
                        break;
                    case KeyEvent.VK_F:
                        renderer.setEye(new Point(renderer.getEye().getX(), renderer.getEye().getY() - DELTA_SIZE, renderer.getEye().getZ()));
                        break;
                    case KeyEvent.VK_H: //home
                        renderer.setEye(new Point(0, 0, 0));
                        renderer.setRotation(new Point(0, 0, 0));
                        break;
                    default:
                        break;
                }
                System.out.println("Eye: " + renderer.getEye().getX() + " " + renderer.getEye().getY() + " " + renderer.getEye().getZ());
                System.out.println("Rotation: " + renderer.getRotation().getX() + " " + renderer.getRotation().getY() + " " + renderer.getRotation().getZ());
            }

            @Override
            public void keyReleased(KeyEvent e){
            }
        });
        //zoom
        this.addMouseWheelListener(e -> {
            int delta = e.getWheelRotation();
            if (delta == 0)
                return;
            renderer.zoom(delta);
        });

        gcodePreviewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                focus();

            }
        });

        firstRangeSlider.addChangeListener(e -> {
            if(oneLayerCheckBox.isSelected()){
                renderer.setDrawModeOneLayer((int) firstRangeSpinner.getValue());
            }
            else if(rangeLayerCheckBox.isSelected()){
                renderer.setDrawModeRangeOfLayers(new int[]{(int) firstRangeSpinner.getValue(), (int) secondRangeSpinner.getValue()});
            }
            else
                renderer.setDrawModeAll();

            firstRangeSpinner.setValue(firstRangeSlider.getValue());
        });

        secondRangeSlider.addChangeListener(e -> {
            if(oneLayerCheckBox.isSelected()){
                renderer.setDrawModeOneLayer((int) firstRangeSpinner.getValue());
            }
            else if(rangeLayerCheckBox.isSelected()){
                renderer.setDrawModeRangeOfLayers(new int[]{(int) firstRangeSpinner.getValue(), (int) secondRangeSpinner.getValue()});
                System.out.println("Range: " + firstRangeSpinner.getValue() + " " + secondRangeSpinner.getValue());
            }
            else
                renderer.setDrawModeAll();
            secondRangeSpinner.setValue(secondRangeSlider.getValue());
        });

        firstRangeSpinner.addChangeListener(e -> {
            if(oneLayerCheckBox.isSelected()){
                renderer.setDrawModeOneLayer((int) firstRangeSpinner.getValue());
            }
            if(rangeLayerCheckBox.isSelected()){
                renderer.setDrawModeRangeOfLayers(new int[]{(int) firstRangeSpinner.getValue(), (int) secondRangeSpinner.getValue()});
            }
            firstRangeSlider.setValue((int) firstRangeSpinner.getValue());
        });

        secondRangeSpinner.addChangeListener(e -> {
            if(oneLayerCheckBox.isSelected()){
                renderer.setDrawModeOneLayer((int) firstRangeSpinner.getValue());
            }
            if(rangeLayerCheckBox.isSelected()){
                renderer.setDrawModeRangeOfLayers(new int[]{(int) firstRangeSpinner.getValue(), (int) secondRangeSpinner.getValue()});
            }
            secondRangeSlider.setValue((int) secondRangeSpinner.getValue());
        });

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        jTabbedPane1 = new JTabbedPane();
        jPanel1 = new JPanel();
        connectButton = new JToggleButton();
        commandLine = new JTextField();
        sendCommandButton = new JButton();
        xyDpadButton = new JLabel();
        jScrollPane1 = new JScrollPane();
        logList = new JList<>();
        jMenuBar1 = new JMenuBar();
        menuKonfiguracja = new JMenu();
        menuItemParametryDrukarki = new JMenuItem();
        menuDruk = new JMenu();
        menuItemZaladujPlik = new JMenuItem();
        menuItemStartDruku = new JMenuItem();
        menuItemPauza = new JMenuItem();
        gcodePreviewPanel = new JPanel();

        extTempCheckBox = new JCheckBox();
        extTempSlider = new JSlider();
        extTempLabel = new JLabel();
        bedTempLabel = new JLabel();
        bedTempSlider = new JSlider();
        bedTempCheckBox = new JCheckBox();
        fanLabel = new JLabel();
        fanSlider = new JSlider();
        fanCheckBox = new JCheckBox();
        jPanel3 = new JPanel();
        idleMoveCheckBox = new JCheckBox();
        firstRangeSpinner = new JSpinner();
        secondRangeSpinner = new JSpinner();
        firstRangeSlider = new JSlider();
        secondRangeSlider = new JSlider();
        oneLayerCheckBox = new JCheckBox();
        rangeLayerCheckBox = new JCheckBox();
        menuItemStop = new JMenuItem();
        menuItemResume = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        BorderLayout gcodePreviewPanelLayout = new BorderLayout();
        gcodePreviewPanel.setLayout(gcodePreviewPanelLayout);


        jPanel1.setBackground(new Color(255, 255, 255));

        connectButton.setText("Połącz");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        commandLine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                commandLineActionPerformed(evt);
            }
        });

        sendCommandButton.setText("Wyślij");
        sendCommandButton.setPreferredSize(new Dimension(200, sendCommandButton.getPreferredSize().height));
        sendCommandButton.setMinimumSize(new Dimension(200, sendCommandButton.getPreferredSize().height));
        sendCommandButton.setMaximumSize(new Dimension(200, sendCommandButton.getPreferredSize().height));

        sendCommandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sendCommandButtonActionPerformed(evt);
            }
        });

        xyDpadButton.setBackground(new Color(255, 255, 255));
        xyDpadButton.setIcon(new ImageIcon(getClass().getResource("/Images/home_active_png.png"))); // NOI18N
        xyDpadButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                xyDpadButtonMouseClicked(evt);
            }
        });

        JLabel newLabel = new JLabel();
        newLabel.setIcon(new ImageIcon(getClass().getResource("/Images/e_active_png.png"))); // NOI18N


        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);

        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(commandLine)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(sendCommandButton, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(connectButton)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(xyDpadButton)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(newLabel)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(fanCheckBox)
                                        .addComponent(extTempCheckBox)
                                        .addComponent(bedTempCheckBox))
                                .addGap(15, 15, 15)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(fanSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bedTempSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(extTempSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(fanLabel)
                                        .addComponent(bedTempLabel)
                                        .addComponent(extTempLabel))
                                .addContainerGap(49, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(connectButton)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(sendCommandButton)
                                        .addComponent(commandLine, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(28, 28, 28)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(xyDpadButton)
                                        .addComponent(newLabel))
                                .addGap(34, 34, 34)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(extTempCheckBox)
                                        .addComponent(extTempSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(extTempLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(bedTempCheckBox)
                                        .addComponent(bedTempSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bedTempLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(fanCheckBox)
                                        .addComponent(fanSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fanLabel))
                                .addContainerGap(198, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Sterowanie", jPanel1);

        idleMoveCheckBox.setText("Pokaż ruch jałowy.");

        oneLayerCheckBox.setText("Pokaż pojedyńczą warstwę.");

        rangeLayerCheckBox.setText("Pokaż przedział warstw.");

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(oneLayerCheckBox)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(secondRangeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(secondRangeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(firstRangeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(firstRangeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(idleMoveCheckBox)
                                        .addComponent(rangeLayerCheckBox))
                                .addContainerGap(75, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(idleMoveCheckBox)
                                .addGap(21, 21, 21)
                                .addComponent(oneLayerCheckBox)
                                .addGap(4, 4, 4)
                                .addComponent(rangeLayerCheckBox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(firstRangeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(firstRangeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(secondRangeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(secondRangeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(460, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Podgląd gcodu", jPanel3);

        jScrollPane1.setViewportView(logList);

//        gcodePreviewPanel.setBackground(new java.awt.Color(204, 255, 255))


        menuKonfiguracja.setText("Konfiguracja");

        menuItemParametryDrukarki.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        menuItemParametryDrukarki.setText("Parametry drukarki");
        menuItemParametryDrukarki.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemParametryDrukarkiActionPerformed(evt);
            }
        });
        menuKonfiguracja.add(menuItemParametryDrukarki);

        jMenuBar1.add(menuKonfiguracja);

        menuDruk.setText("Druk");

        menuItemZaladujPlik.setText("Załaduj plik");
        menuItemZaladujPlik.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemZaladujPlikActionPerformed(evt);
            }
        });
        menuDruk.add(menuItemZaladujPlik);

        menuItemStartDruku.setText("Drukuj");
        menuItemStartDruku.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemStartDrukuActionPerformed(evt);
            }
        });
        menuDruk.add(menuItemStartDruku);

        menuItemPauza.setText("Pauza");
        menuItemPauza.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemPauzaActionPerformed(evt);
            }
        });
        menuItemResume.setText("Wznów");
        menuItemResume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemResumeActionPerformed(evt);
            }
        });

        menuItemStop.setText("Stop");
        menuItemStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemStopActionPerformed(evt);
            }
        });


        menuDruk.add(menuItemPauza);
        menuDruk.add(menuItemResume);
        menuDruk.add(menuItemStop);

        jMenuBar1.add(menuDruk);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
                                        .addComponent(gcodePreviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(gcodePreviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jTabbedPane1))
                                .addContainerGap())
        );

        extTempCheckBox.setText("Ekstruder");
        extTempCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                extTempCheckBoxActionPerformed(evt);
            }
        });

        extTempLabel.setText("- -");

        bedTempLabel.setText("- -");

        bedTempCheckBox.setText("Stół");
        bedTempCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bedTempCheckBoxActionPerformed(evt);
            }
        });

        fanLabel.setText("- -");

        fanCheckBox.setText("Wiatrak");
        fanCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fanCheckBoxActionPerformed(evt);
            }
        });

        fanSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                setupSliders();
                if(controlPrinter != null)
                    controlPrinter.setFanSpeed(desiredFanSpeed);
            }
        });

        bedTempSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                setupSliders();
                if(controlPrinter != null)
                    controlPrinter.setDesiredBedTemp(desiredTempBed);
            }
        });

        extTempSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                setupSliders();
                if(controlPrinter != null)
                    controlPrinter.setDesiredExtrTemp(desiredTempExt);
            }
        });

        pack();
    }// </editor-fold>

    private void menuItemParametryDrukarkiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemParametryDrukarkiActionPerformed
        ParametryDrukarkiForm parametryDrukarkiForm = new ParametryDrukarkiForm(settings -> {
            // TODO Handle the chosen settings profile here
            Logger.getLogger(MainGui.class.getName()).info("Printer settings changed: " + settings);
            printerSettings = settings;
            setupSliders();
        });
        parametryDrukarkiForm.setVisible(true);
    }//GEN-LAST:event_menuItemParametryDrukarkiActionPerformed

    private void addMenuActions(){
        menuActions = new javax.swing.JMenu();
        menuActions.setText("Ruch");

        menuItemHomeAll = new javax.swing.JMenuItem();
        menuItemHomeAll.setText("Zaparkuj wszystkie osie");
        menuItemHomeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (controlPrinter != null) {
                    controlPrinter.homeAllAxis();
                }
            }
        });
        menuActions.add(menuItemHomeAll);

        menuItemHomeX = new javax.swing.JMenuItem();
        menuItemHomeX.setText("Zaparkuj oś X");
        menuItemHomeX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (controlPrinter != null) {
                    controlPrinter.homeAxis(ControlPrinter.PrinterAxis.X);
                }
            }
        });
        menuActions.add(menuItemHomeX);

        menuItemHomeY = new javax.swing.JMenuItem();
        menuItemHomeY.setText("Zaparkuj oś Y");
        menuItemHomeY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (controlPrinter != null) {
                    controlPrinter.homeAxis(ControlPrinter.PrinterAxis.Y);
                }
            }
        });
        menuActions.add(menuItemHomeY);

        menuItemHomeZ = new javax.swing.JMenuItem();
        menuItemHomeZ.setText("Zaparkuj oś Z");
        menuItemHomeZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (controlPrinter != null) {
                    controlPrinter.homeAxis(ControlPrinter.PrinterAxis.Z);
                }
            }
        });
        menuActions.add(menuItemHomeZ);

        menuItemSendM84 = new javax.swing.JMenuItem();
        menuItemSendM84.setText("Wyłącz silniki");
        menuItemSendM84.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (controlPrinter != null) {
                    controlPrinter.releaseMotors();
                }
            }
        });
        menuActions.add(menuItemSendM84);

// Add the new menu to the menu bar
        jMenuBar1.add(menuActions);
        //make all the menu items not active
        menuItemHomeAll.setEnabled(false);
        menuItemHomeX.setEnabled(false);
        menuItemHomeY.setEnabled(false);
        menuItemHomeZ.setEnabled(false);
        menuItemSendM84.setEnabled(false);

    }

    private void commandLineActionPerformed(java.awt.event.ActionEvent evt) {
        // if enter is pressed - send command
        sendCommandButtonActionPerformed(evt);

    }

    private void idleMoveCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extTempCheckBoxActionPerformed
        updateVisibilityOnConnection();
        if(idleMoveCheckBox.isSelected())
            renderer.setIdleMoves(true);
        else
            renderer.setIdleMoves(false);

    }//GEN-LAST:event_extTempCheckBoxActionPerformed

    private void extTempCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extTempCheckBoxActionPerformed
        updateVisibilityOnConnection();
        if(extTempCheckBox.isSelected())
            controlPrinter.setDesiredExtrTemp(desiredTempExt);
        else
            controlPrinter.setDesiredExtrTemp(0);
    }//GEN-LAST:event_extTempCheckBoxActionPerformed

    private void bedTempCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bedTempCheckBoxActionPerformed
        updateVisibilityOnConnection();
        if (bedTempCheckBox.isSelected())
            controlPrinter.setDesiredBedTemp(desiredTempBed);
        else
            controlPrinter.setDesiredBedTemp(0);

    }//GEN-LAST:event_bedTempCheckBoxActionPerformed

    private void fanCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fanCheckBoxActionPerformed
        updateVisibilityOnConnection();
        if(fanCheckBox.isSelected())
            controlPrinter.setFanSpeed(desiredFanSpeed);
        else
            controlPrinter.setFanSpeed(0);
    }//GEN-LAST:event_fanCheckBoxActionPerformed

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (connectButton.isSelected()) {
            try {
                if (baseTransmHandler != null) {
                    baseTransmHandler.disconnect();
                }
                if (controlPrinter != null) {
                    controlPrinter.stopTemperatureThread();
                }
                baseTransmHandler = new BaseTransmHandler(printerSettings);
                controlPrinter = new ControlPrinter(baseTransmHandler, printerSettings);
                controlPrinter.startTemperatureThread(
                        temp -> {
                            actualTempExt = temp;
                            updatedTempLabels();
                        },
                        temp -> {
                            actualTempBed = temp;
                            updatedTempLabels();
                        }
                );
                baseTransmHandler.getResponseList().setCallback(() -> {
                    jScrollPane1.getVerticalScrollBar().setValue(jScrollPane1.getVerticalScrollBar().getMaximum() + 1);
                    return null;
                });
                logList.setModel(baseTransmHandler.getResponseList());
                logList.setAutoscrolls(true);
                logList.ensureIndexIsVisible(logList.getModel().getSize() - 1);
            } catch (Exception e) {
                e.printStackTrace();
                connectButton.setSelected(false);
            }
        } else {
            try {
                if (baseTransmHandler != null) {
                    baseTransmHandler.disconnect();
                }
                if (controlPrinter != null) {
                    controlPrinter.stopTemperatureThread();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            baseTransmHandler = null;
            controlPrinter = null;
        }
        updateVisibilityOnConnection();
    }

    private void xyDpadButtonMouseClicked(java.awt.event.MouseEvent evt) {
        int [] labelSizes = {xyDpadButton.getWidth(), xyDpadButton.getHeight()};
        int [] mousePos = {evt.getX(), evt.getY()};
        int [] labelPos = {xyDpadButton.getX(), xyDpadButton.getY()};
        int [] labelCenter = {labelSizes[0]/2, labelSizes[1]/2};
        System.out.println("Mouse center: " + labelCenter[0] + " " + labelCenter[1]);
        System.out.println("Mouse pos: " + mousePos[0] + " " + mousePos[1]);
        try {
            if (mousePos[0] > labelSizes[0] / 3 && mousePos[0] < 2 * labelSizes[0] / 3 && mousePos[1] < labelSizes[1] / 3)
                controlPrinter.moveAxisRelatively(ControlPrinter.PrinterAxis.Y, 10);
            if (mousePos[0] > labelSizes[0] / 3 && mousePos[0] < 2 * labelSizes[0] / 3 && mousePos[1] > 2 * labelSizes[1] / 3)
                controlPrinter.moveAxisRelatively(ControlPrinter.PrinterAxis.Y, -10);
            if (mousePos[0] < labelSizes[0] / 3 && mousePos[1] > labelSizes[1] / 3 && mousePos[1] < 2 * labelSizes[1] / 3)
                controlPrinter.moveAxisRelatively(ControlPrinter.PrinterAxis.X, -10);
            if (mousePos[0] > 2 * labelSizes[0] / 3 && mousePos[1] > labelSizes[1] / 3 && mousePos[1] < 2 * labelSizes[1] / 3)
                controlPrinter.moveAxisRelatively(ControlPrinter.PrinterAxis.X, 10);
        } catch (NullPointerException e) {
            System.out.println("Printer not connected");
        }
    }

    private void sendCommandButtonActionPerformed(java.awt.event.ActionEvent evt) {
        baseTransmHandler.queueCommand(GcodeObject.prepareCommand(commandLine.getText(), true, null));
    }

    private void menuItemZaladujPlikActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();

        // Set the file selection mode (files only)
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                // Allow directories or files with .gcode extension
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".gcode");
            }

            @Override
            public String getDescription() {
                return "G-code Files (*.gcode)";
            }
        });

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(this);

        // Check if a file was selected
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File selectedFile = fileChooser.getSelectedFile();
            try {
                gcodeFileReader = new GcodeFileReader(selectedFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (renderer != null) {
                gcodePreviewPanel.remove(glCanvas);
                glCanvas.removeGLEventListener(renderer);
                if (animator != null) {
                    animator.stop();
                    animator.remove(glCanvas);
                }
            }
            renderer = new SimpleGLCanvas(gcodeFileReader);
            renderer.setEye(gcodeFileReader.getCenter());
            glCanvas.addGLEventListener(renderer);
            gcodePreviewPanel.add(glCanvas, java.awt.BorderLayout.CENTER);
            animator = new FPSAnimator(glCanvas, 60);
            animator.start();
            gcodePreviewPanel.setVisible(true);

            focus();
            glCanvas.display();
            gcodePreviewPanel.revalidate();
            gcodePreviewPanel.repaint();
            secondRangeSlider.setMaximum(gcodeFileReader.getLayerAmount());
            firstRangeSlider.setMaximum(gcodeFileReader.getLayerAmount());
            updateVisibilityOnFileLoad();
        }
    }

    private void setupSliders() {
        if(printerSettings != null){
            extTempSlider.setMaximum(printerSettings.getMaxTempExt());
            bedTempSlider.setMaximum(printerSettings.getMaxTempBed());
            fanSlider.setMaximum(100);

            desiredTempExt = extTempSlider.getValue();
            desiredTempBed = bedTempSlider.getValue();
            desiredFanSpeed = fanSlider.getValue();
            updatedTempLabels();
        }
    }

    private void updatedTempLabels(){
        extTempLabel.setText((actualTempExt != null ? actualTempExt.toString() : "-- ")  + "/" + (desiredTempExt != null ? desiredTempExt.toString() : "--"));
        bedTempLabel.setText((actualTempBed != null ? actualTempBed.toString() : "-- " ) + "/"+ (desiredTempBed != null ? desiredTempBed.toString() : "--"));
        fanLabel.setText((actualFanSpeed != null ? actualFanSpeed.toString()   : "-- " ) + "/" + (desiredFanSpeed != null ? desiredFanSpeed.toString() : "--"));
    }


    private void menuItemStartDrukuActionPerformed(java.awt.event.ActionEvent evt) {
        if (controlPrinter != null && gcodeFileReader != null) {
            controlPrinter.streamFile(gcodeFileReader);
        }
        else {
            System.out.println("Printer not connected or file not loaded");
        }
    }

    private void menuItemPauzaActionPerformed(java.awt.event.ActionEvent evt) {
        if (baseTransmHandler != null) {
            baseTransmHandler.pauseSending();
        }

    }

    private void menuItemStopActionPerformed(java.awt.event.ActionEvent evt) {

    }

    private void menuItemResumeActionPerformed(java.awt.event.ActionEvent evt) {
        if (baseTransmHandler != null) {
            baseTransmHandler.resumeSending();
        }

    }

    private void oneLayerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        if(rangeLayerCheckBox.isSelected()){
            rangeLayerCheckBox.setSelected(false);
        }
        if(oneLayerCheckBox.isSelected()){
            rangeLayerCheckBox.setSelected(false);
            firstRangeSpinner.setEnabled(true);
            secondRangeSpinner.setEnabled(false);
        }
    }

    private void rangeLayerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        if(oneLayerCheckBox.isSelected()){
            oneLayerCheckBox.setSelected(false);
        }
        if(rangeLayerCheckBox.isSelected()){
            oneLayerCheckBox.setSelected(false);
            firstRangeSpinner.setEnabled(true);
            secondRangeSpinner.setEnabled(true);
        }

    }


    private void updateVisibilityOnConnection(){
        boolean isConnected = baseTransmHandler != null;
        connectButton.setSelected(isConnected);
        menuItemHomeAll.setEnabled(isConnected);
        menuItemHomeX.setEnabled(isConnected);
        menuItemHomeY.setEnabled(isConnected);
        menuItemHomeZ.setEnabled(isConnected);
        menuItemSendM84.setEnabled(isConnected);
        extTempCheckBox.setEnabled(isConnected);
        bedTempCheckBox.setEnabled(isConnected);
        fanCheckBox.setEnabled(isConnected);
        extTempSlider.setEnabled(extTempCheckBox.isSelected());
        bedTempSlider.setEnabled(bedTempCheckBox.isSelected());
        fanSlider.setEnabled(fanCheckBox.isSelected());
        menuItemPauza.setEnabled(isConnected);
        menuItemStartDruku.setEnabled(isConnected);
        menuItemStop.setEnabled(isConnected);
        menuItemResume.setEnabled(isConnected);
        focus();
    }

    private void updateVisibilityOnFileLoad(){
        boolean isFileLoaded = gcodeFileReader != null;
        System.out.println("File loaded: " + isFileLoaded);
        menuItemStartDruku.setEnabled(isFileLoaded);
        oneLayerCheckBox.setEnabled(isFileLoaded);
        rangeLayerCheckBox.setEnabled(isFileLoaded);
        idleMoveCheckBox.setEnabled(isFileLoaded);
        idleMoveCheckBox.setSelected(false);
        //set rotation to 0
        if(renderer != null)
            renderer.setRotation(new Point(0, -90, -90));
        focus();
    }

    private void focus()   {
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    // Variables declaration - do not modify
    private javax.swing.JToggleButton connectButton;
    private javax.swing.JPanel gcodePreviewPanel;
    private javax.swing.JLabel xyDpadButton;
    private javax.swing.JList<String> logList;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField commandLine;
    private javax.swing.JMenu menuDruk;
    private javax.swing.JMenuItem menuItemParametryDrukarki;
    private javax.swing.JMenuItem menuItemPauza;
    private javax.swing.JMenuItem menuItemStartDruku;
    private javax.swing.JMenuItem menuItemZaladujPlik;
    private javax.swing.JMenu menuKonfiguracja;
    private javax.swing.JButton sendCommandButton;

    private javax.swing.JCheckBox bedTempCheckBox;
    private javax.swing.JLabel bedTempLabel;
    private javax.swing.JSlider bedTempSlider;
    private javax.swing.JCheckBox extTempCheckBox;
    private javax.swing.JLabel extTempLabel;
    private javax.swing.JSlider extTempSlider;
    private javax.swing.JCheckBox fanCheckBox;
    private javax.swing.JLabel fanLabel;
    private javax.swing.JSlider fanSlider;
    private javax.swing.JCheckBox oneLayerCheckBox;
    private javax.swing.JCheckBox rangeLayerCheckBox;
    private javax.swing.JSlider secondRangeSlider;
    private javax.swing.JSpinner secondRangeSpinner;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSlider firstRangeSlider;
    private javax.swing.JSpinner firstRangeSpinner;
    private javax.swing.JCheckBox idleMoveCheckBox;
    // End of variables declaration

    private javax.swing.JMenu menuActions;
    private javax.swing.JMenuItem menuItemHomeAll;
    private javax.swing.JMenuItem menuItemHomeX;
    private javax.swing.JMenuItem menuItemHomeY;
    private javax.swing.JMenuItem menuItemHomeZ;
    private javax.swing.JMenuItem menuItemSendM84;
    private javax.swing.JMenuItem menuItemStop;
    private javax.swing.JMenuItem menuItemResume;


    public static void main(String[] args) {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        java.awt.EventQueue.invokeLater(() -> {
            new MainGui().setVisible(true);
        });
        //delete
    }
}
