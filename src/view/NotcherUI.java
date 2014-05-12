/******************************************************************************
* Title: NotcherUI.java
* Author: Hunter Schoonover
* Date: 2/3/14
*
* Purpose:
*
* This class creates a visual interface for a notch cutter.
* 
* All GUI control events for each Notcher interface, including Timer events are 
* caught by this object and passed on to the "NotcherController" object pointed
* by the class member "eventHandler" for final handling.
*
*/

//-----------------------------------------------------------------------------

package view;

//-----------------------------------------------------------------------------

import controller.EventHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class NotcherUI
//

public class NotcherUI extends JPanel implements ActionListener, ChangeListener,
        WindowListener {
    
    private final EventHandler eventHandler;
    private final NotcherSettings notcherSettings;
    private final JFrame mainFrame;
    
    private JPanel headGuiPanel;
    
    private JDialog changeNameDialog;
    
    private JTextField dataVersionTField;
    private JTextField dataTArea1;
    private JTextField dataTArea2;
    private JTextField changeNameTextField;
    
    private JLabel nameValueLabel;
    
    private JButton sendTargetDepthBtn;
    
    // hss wip -- set to private and create a getter
    public LEDGroup voltageLeds, currentLeds;
    private LEDGroup powerOKLed, shortLed;
    
    // hsswip -- name should be set elswhere
    private String notcherName = "NotcherUI";
    
    private final int indexNumber;
    private final int width, height;
    
//-----------------------------------------------------------------------------
// NotcherUI::NotcherUI (constructor)
//

public NotcherUI(int pWidth, int pHeight, int pIndexNumber, JFrame pMainFrame,
                    NotcherSettings pNotcherSettings, 
                    EventHandler pEventHandler)
{
    
    width = pWidth;
    height = pHeight;
    indexNumber = pIndexNumber;
    mainFrame = pMainFrame;
    notcherSettings = pNotcherSettings;
    eventHandler = pEventHandler;
    
}//end of NotcherUI::NotcherUI (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    Tools.setSizes(this, width, height);
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    this.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.black),
                            "NotcherUI"));
    
    //create user interface: buttons, displays, etc.
    setupGui();
    
    initiateNotcherSettings();

}//end of NotcherUI::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::setupGui
//
// Sets up the user interface on the mainPanel: buttons, displays, etc.
//

public void setupGui()
{
    
    JPanel outerPanel = new JPanel();
    JPanel panel;
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    panel.add(createHeaderPanel());
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    //vertical spacer
    outerPanel.add(Box.createRigidArea(new Dimension(0,10)));
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    panel.add(createStatusPanel());
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    //vertical spacer
    outerPanel.add(Box.createRigidArea(new Dimension(0,20)));
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    panel.add(createHeadPanel());
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    add(outerPanel);

}// end of NotcherUI::setupGui
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createHeaderPanel
//
// Creates objects that give basic information about the notcher and adds them 
// to a returned panel.
//

public JPanel createHeaderPanel()
{
    
    JPanel outerPanel = new JPanel();
    JPanel panel;
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(createNamePanel());
    outerPanel.add(panel);
    
    //horizontal spacer
    outerPanel.add(Box.createRigidArea(new Dimension(40, 0)));
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(createSaveButton());
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(5, 0)));
    panel.add(createSettingsButton());
    outerPanel.add(panel);
    
    return outerPanel;
    
}// end of NotcherUI::createHeaderPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createNamePanel
//
// Creates objects used for the notcher name and adds them to a returned panel.
//

public JPanel createNamePanel()
{
    
    JPanel outerPanel = new JPanel();
    JPanel panel;
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(createNameAndNameValueLabels());
    outerPanel.add(panel);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(45, 0)));
    //add a button
    JButton changeNameBtn = new JButton("Change");
    changeNameBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    changeNameBtn.setActionCommand("Change name of unit");
    changeNameBtn.addActionListener(this);
    changeNameBtn.setToolTipText("Change the name of this unit");
    Tools.setSizes(changeNameBtn, 80, 20);
    panel.add(changeNameBtn);
    outerPanel.add(panel);
    
    return outerPanel;
    
}// end of NotcherUI::createNamePanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createNameAndNameValueLabels
//
// Creates the labels for the name label ("Name:") and the value of the name
//

public JPanel createNameAndNameValueLabels()
{
    
    JPanel panel;
    Font font;
    Font newFont;
    
    // add a containing JPanel to place the labels next to each other
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    Tools.setSizes(panel, 250, 20);
    
    // create a label to display the name of the notcher
    JLabel nameLabel = new JLabel("Name: ");
    nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // gets the current font of the label
    font = nameLabel.getFont();
    // creates a new font with the same font type and size as the label already 
    // has but with bold
    newFont = new Font(font.getFontName(), Font.BOLD, 15);
    nameLabel.setFont(newFont);
    nameLabel.setToolTipText("This notcher is named: " + notcherName);
    panel.add(nameLabel);
    
    // create a label to display the name of the notcher
    nameValueLabel = new JLabel(notcherName);
    nameValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // gets the current font of the label
    font = nameValueLabel.getFont();
    // creates a new font with the same font type and style but with increased
    // size
    newFont = new Font (font.getFontName(), Font.PLAIN, 15);
    nameValueLabel.setFont(newFont);
    nameValueLabel.setToolTipText("This notcher is named: " + notcherName);
    panel.add(nameValueLabel);
    
    return panel;
    
}// end of NotcherUI::createNameAndNameValueLabels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createSaveButton
//
// Creates buttons and adds them to a returned panel.
//

public JPanel createSaveButton()
{
    
    JPanel panel;
    
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //create an icon with the specified path
    ImageIcon saveIcon = View.createImageIcon("images/saveButton.png");
    ImageIcon saveIconRollover = View.createImageIcon
                                            ("images/saveButtonRollover.png");
    //create and add a button to panel
    JButton saveBtn = new JButton(saveIcon);
    Tools.setSizes(saveBtn, 20, 20);
    saveBtn.setRolloverIcon(saveIconRollover);
    saveBtn.setRolloverEnabled(true);
    saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    saveBtn.setActionCommand("Save Data To File");
    saveBtn.addActionListener(this);
    saveBtn.setToolTipText("Save data to file.");
    saveBtn.setBorderPainted(false); 
    saveBtn.setContentAreaFilled(false); 
    saveBtn.setFocusPainted(false); 
    saveBtn.setOpaque(false);
    panel.add(saveBtn);
    
    return panel;
    
}// end of NotcherUI::createSaveButton
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createSettingsButton
//
// Creates a button (gear shaped) and adds it to a returned panel.
//
// hss wip

public JPanel createSettingsButton()
{
    
    JPanel panel;
    
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //create an icon with the specified path
    ImageIcon gearIcon = View.createImageIcon("images/settingsGear.png");
    ImageIcon gearIconRollover = View.createImageIcon
                                            ("images/settingsGearRollover.png");
    //create and add a button to panel
    JButton settingsBtn = new JButton(gearIcon);
    Tools.setSizes(settingsBtn, 20, 20);
    settingsBtn.setRolloverIcon(gearIconRollover);
    settingsBtn.setRolloverEnabled(true);
    settingsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    settingsBtn.setActionCommand
                            ("Change the tools and settings for this device");
    settingsBtn.addActionListener(this);
    settingsBtn.setToolTipText("Change the tools and settings for this device");
    settingsBtn.setBorderPainted(false); 
    settingsBtn.setContentAreaFilled(false); 
    settingsBtn.setFocusPainted(false); 
    settingsBtn.setOpaque(false);
    panel.add(settingsBtn);
    
    return panel;
    
}// end of NotcherUI::createSettingsButton
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createStatusPanel
//
// Creates a panel for status information such as: power status, current,
// voltage, etc.
//

public JPanel createStatusPanel()
{
    
    JPanel outerPanel = new JPanel();
    JPanel panel;
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    // add power status objects to panel
    panel.add(createPowerStatusPanel());
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    //horizontal spacer
    outerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    // add voltage and current panels
    panel.add(createCurrentPanel());
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    // add voltage and current panels
    panel.add(createVoltagePanel());
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    //horizontal spacer
    outerPanel.add(Box.createRigidArea(new Dimension(10,0)));
    
    return outerPanel;
    
}// end of NotcherUI::createStatusPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createPowerStatusPanel
//
// Creates objects used for the PowerOK status and for the Short status and adds
// them to a returned panel.
//

public JPanel createPowerStatusPanel()
{
    
    JPanel outerPanel;
    JPanel panel;
    
    outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // create a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // add an led for the PowerOK status
    powerOKLed = new LEDGroup("PowerOK", 1, 15, 15, 0, 55, 10, Color.GREEN, 
                   panel.getBackground());
    powerOKLed.init();
    powerOKLed.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(powerOKLed);
    outerPanel.add(panel);
    
    //horizontal spacer
    outerPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    
    // create a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // add an led for the Short status
    shortLed = new LEDGroup("Short", 1, 15, 15, 0, 30, 10, Color.RED, 
                   panel.getBackground());
    shortLed.init();
    shortLed.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(shortLed);
    outerPanel.add(panel);
    
    return outerPanel;
    
}// end of NotcherUI::createPowerStatusPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createCurrentPanel
//
// Goes through the process of creating an LEDGroup for current and adding it
// to the panel.
//

public JPanel createCurrentPanel()
{
    
    JPanel panel;
    
    // create a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    currentLeds = new LEDGroup("Current", 10, 20, 10, 0, 16, 10, Color.RED, 
                                getBackground()); 
    currentLeds.init();
    currentLeds.setRange(0, 10);
    currentLeds.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(currentLeds);
    
    return panel;
    
}// end of NotcherUI::createCurrentPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createVoltagePanel
//
// Goes through the process of creating an LEDGroup for votlage and adding it to
// the panel.
//

public JPanel createVoltagePanel()
{
 
    JPanel panel;
    
    // create a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    voltageLeds = new LEDGroup("Voltage", 10, 20, 10, 0, 16, 10, Color.GREEN, 
                                getBackground()); 
    voltageLeds.init();
    voltageLeds.setRange(0, 10);
    voltageLeds.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(voltageLeds);
    
    //add a button
    JButton voltageStatusBtn = new JButton("Voltage is on" );
    voltageStatusBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    voltageStatusBtn.setActionCommand("Change status of voltage");
    voltageStatusBtn.addActionListener(this);
    voltageStatusBtn.setToolTipText("Turn voltage off");
    Tools.setSizes(voltageStatusBtn, 105, 20);
    panel.add(voltageStatusBtn);
    
    return panel;
    
}// end of NotcherUI::createVoltagePanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createHeadPanel
//
// Creates a panel to contain things pertaining to the "Head".
//

public JPanel createHeadPanel()
{
    
    JPanel outerPanel;
    JPanel panel;
    
    // add a containing JPanel
    outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(outerPanel, 305, 75);
    outerPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.black), 
                            "Head"));

    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(panel, 290, 20);
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    //create a label to display the head's current cutting position
    // hss wip value for label should be passed in from elsewhere
    JLabel positionLbl = new JLabel("Postion: " + "0.01");
    positionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    positionLbl.setToolTipText("");
    panel.add(positionLbl);
    
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(10,0)));
    
    //create a label to display the head's current cutting position
    panel.add(createCuttingHeadPositionSpinner());
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    outerPanel.add(panel);
    
    //vertical spacer
    outerPanel.add(Box.createRigidArea(new Dimension(0,10)));
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(panel, 290, 20);
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    panel.add(createCutDepthInputSpinner());
    
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(5,0)));
    
    panel.add(createApplyButton());
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    outerPanel.add(panel);
    
    return outerPanel;
    
}// end of NotcherUI::createHeadPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createCuttingHeadPositionSpinner
//
// Creates an MFloatSpinner object and returns it.
//

public JPanel createCuttingHeadPositionSpinner()
{
    
    JPanel outerPanel;
    
    // add a containing JPanel
    outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //create a label
    JLabel cuttingHeadPositionLbl = new JLabel("Cutting Head Position: ");
    cuttingHeadPositionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    cuttingHeadPositionLbl.setToolTipText("");
    outerPanel.add(cuttingHeadPositionLbl);
    
    // hss wip -- some of these values should be passed in and determined
    // from elsewhere
    MFloatSpinner cuttingHeadPositionSpinner = new MFloatSpinner(5.5, 1.1, 9.9, 
                                                                0.1, "##0.0", 
                                                                60, 20);
    cuttingHeadPositionSpinner.addChangeListener(this);
    cuttingHeadPositionSpinner.setName("Cutting Head Position Spinner");
    cuttingHeadPositionSpinner.setToolTipText("Cutting head position");
    cuttingHeadPositionSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    outerPanel.add(cuttingHeadPositionSpinner);
    
    return outerPanel;
    
}// end of NotcherUI::createCuttingHeadPositionSpinner
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createCutDepthInputSpinner
//
// Creates an MFloatSpinner object and returns it.
//

public JPanel createCutDepthInputSpinner()
{
    
    JPanel outerPanel;
    
    // add a containing JPanel
    outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //create a label
    JLabel cutDepthLbl = new JLabel("Cut Depth: ");
    cutDepthLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    cutDepthLbl.setToolTipText("");
    outerPanel.add(cutDepthLbl);
    
    // hss wip -- some of these values should be passed in and determined
    // from elsewhere
    MFloatSpinner cutDepthInputSpinner = new MFloatSpinner(5.5, 1.1, 9.9, 0.1, 
                                                            "##0.0", 60, 20);
    cutDepthInputSpinner.addChangeListener(this);
    cutDepthInputSpinner.setName("Cut Depth Input Spinner");
    cutDepthInputSpinner.setToolTipText("Use this to edit the cut depth");
    cutDepthInputSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    outerPanel.add(cutDepthInputSpinner);
    
    return outerPanel;
    
}// end of NotcherUI::createCutDepthInputSpinner
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createApplyButton
//
// Creates a JPanel that contains the apply button.
//

public JPanel createApplyButton()
{
    
    JPanel outerPanel;
    
    // add a containing JPanel
    outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //add a button
    JButton applyTargetDepthBtn = new JButton("Apply"); 
    Tools.setSizes(applyTargetDepthBtn, 65, 20);
    applyTargetDepthBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    applyTargetDepthBtn.setActionCommand("Send target depth to device");
    applyTargetDepthBtn.addActionListener(this);
    applyTargetDepthBtn.setToolTipText("Send target depth to device");
    outerPanel.add(applyTargetDepthBtn);
    
    return outerPanel;
    
}// end of NotcherUI::createApplyButton
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::initiateNotcherSettings
//
// Initiates the Settings given to this notcherUI and makes the JDialog
// invisible.
//

public void initiateNotcherSettings()
{
    
    notcherSettings.init(notcherName, eventHandler, this, this);
    notcherSettings.setVisible(false);

}// end of NotcherUI::initiateNotcherSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::ActivateNotcherSettings
//
// Activate the Settings given to this notcherUI by making the JDialog
// visible.
//

public void ActivateNotcherSettings()
{
    
    notcherSettings.setVisible(true);

}// end of NotcherUI::ActivateNotcherSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createChangeNameDialog
//
// Creates a JDialog for changing the name of this unit.
// The value given to the JTextFielw is used as the new name.
//

public void createChangeNameDialog()
{
    
    JPanel panel;
    
    changeNameDialog = new JDialog(mainFrame);
    Tools.setSizes(changeNameDialog, 300, 100);
    
    changeNameDialog.setTitle("Change the Name of: " + notcherName);
    
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    Tools.setSizes(mainPanel, 300, 100);
    changeNameDialog.setContentPane(mainPanel);
    
    //vertical spacer
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    //add text field
    changeNameTextField = new JTextField("Enter new name...");
    changeNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    changeNameTextField.selectAll();
    Tools.setSizes(changeNameTextField, 150, 24);
    //text fields don't have action commands or action listeners
    changeNameTextField.setToolTipText("Enter a new name for this unit.");
    panel.add(changeNameTextField);
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    mainPanel.add(panel);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    //add a button
    JButton applyNewNameBtn = new JButton("Apply"); 
    Tools.setSizes(applyNewNameBtn, 65, 20);
    applyNewNameBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    applyNewNameBtn.setActionCommand("Apply this name to the unit");
    applyNewNameBtn.addActionListener(this);
    applyNewNameBtn.setToolTipText("Apply this name to the unit");
    panel.add(applyNewNameBtn);
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    mainPanel.add(panel);
    
    centerJDialog(changeNameDialog);
    
    changeNameDialog.setVisible(true);
    
}// end of NotcherUI::createChangeNameDialog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::changeNotcherName
//
// Changes the name of the notcher unit to the value of the changeNameTextField.
//

public void changeNotcherName()
{

    notcherName = changeNameTextField.getText();
    
    //reset various items that use the notcherName
    changeNameDialog.setTitle("Change the Name of: " + notcherName);
    nameValueLabel.setText(notcherName);
    notcherSettings.setTitle(notcherName + " Settings");

}// end of NotcherUI::changeNotcherName
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::setTextForDataTArea1
//
// Sets the text value for text box.
//

public void setTextForDataTArea1(String pText)
{

    dataTArea1.setText(pText);

}// end of NotcherUI::setTextForDataTArea1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::setTextForDataTArea2
//
// Sets the text value for text box.
//

public void setTextForDataTArea2(String pText)
{

    dataTArea2.setText(pText);

}// end of NotcherUI::setTextForDataTArea2
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::centerJDialog
//
// Centers a passed in JDialog according to the screen size and JDialog's size.
//

public void centerJDialog(JDialog pDialog)
{
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    pDialog.setLocation((int)screenSize.getWidth()/2 - 
                            (int)pDialog.getWidth()/2, 
                            (int)screenSize.getHeight()/2 - 
                            (int)pDialog.getHeight()/2);

}// end of NotcherSettings::centerJDialog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::actionPerformed
//
// Responds to events and passes them on to the "Controller" (MVC Concept)
// objects.
//

@Override
public void actionPerformed(ActionEvent e)
{

    eventHandler.actionPerformed(e);

}//end of NotcherUI::actionPerformed
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::stateChanged
//

@Override
public void stateChanged(ChangeEvent ce) {
   
    eventHandler.stateChanged(ce);

}//end of NotcherUI::stateChanged
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::windowClosing
//
// Handles actions necessary when the window is closing
//

@Override
public void windowClosing(WindowEvent e)
{

    eventHandler.windowClosing(e);

}//end of NotcherUI::windowClosing
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::(various window listener functions)
//
// These functions are implemented per requirements of interface WindowListener
// but do nothing at the present time.  As code is added to each function, it
// should be moved from this section and formatted properly.
//

@Override
public void windowActivated(WindowEvent e){}
@Override
public void windowDeactivated(WindowEvent e){}
@Override
public void windowOpened(WindowEvent e){}
//@Override
//public void windowClosing(WindowEvent e){}
@Override
public void windowClosed(WindowEvent e){}
@Override
public void windowIconified(WindowEvent e){}
@Override
public void windowDeiconified(WindowEvent e){}

//end of NotcherUI::(various window listener functions)
//-----------------------------------------------------------------------------
    
}//end of class NotcherUI
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------