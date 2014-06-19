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


import controller.EventProcessor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class NotcherUI
//

public class NotcherUI extends JPanel implements ActionListener, ChangeListener,
        WindowListener {
    
    private final EventProcessor eventHandler;
    private final JFrame mainFrame;
    
    private NotcherSettings notcherSettings;
    
    private JPanel headGuiPanel;
    
    private JDialog changeNameDialog;
    
    private JTextField dataVersionTField;
    private JTextField dataTArea1;
    private JTextField dataTArea2;
    private JTextField changeNameTextField;
    
    private JLabel nameValueLabel;
    
    private JButton sendTargetDepthBtn;
    
    private JToggleButton electrodePowerOnOffBtn;
    
    // hss wip -- set to private and create a getter
    public LEDGroup voltageLeds, currentLeds;
    private LEDGroup powerLed, shortLed;
    
    // hsswip -- name should be set elswhere
    private String notcherName = "NotcherUI";
    private String previousNotcherName;
    
    private final int indexNumber;
    private final int width, height;
    
//-----------------------------------------------------------------------------
// NotcherUI::NotcherUI (constructor)
//

public NotcherUI(int pWidth, int pHeight, int pIndexNumber, JFrame pMainFrame,
                    EventProcessor pEventHandler)
{
    
    width = pWidth;
    height = pHeight;
    indexNumber = pIndexNumber;
    mainFrame = pMainFrame;
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
    
    createNotcherSettings();

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
    // creates a new font with the same font type and size that the label 
    // already has and then makes the font bold
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
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(20, 0)));
    // add power status objects to panel
    panel.add(createPowerStatusPanel());
    outerPanel.add(panel);
    
    //vertical spacer
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
    
    // add an led for the Power status
    powerLed = new LEDGroup("Power", LEDGroup.SIDE_BY_SIDE, 1, 15, 15, 0, 0, 
                                        0, Color.GREEN, panel.getBackground());
    powerLed.init();
    panel.add(powerLed);
    outerPanel.add(panel);
    
    //horizontal spacer
    outerPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    
    // create a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    // add an led for the Short status
    shortLed = new LEDGroup("Short", LEDGroup.SIDE_BY_SIDE, 1, 15, 15, 0, 0, 
                                        0, Color.RED, panel.getBackground());
    shortLed.init();
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
    
    currentLeds = new LEDGroup("Current", LEDGroup.BORDER_TITLE, 10, 20, 10, 0, 
                                    16, 10, Color.RED, panel.getBackground()); 
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
    
    voltageLeds = new LEDGroup("Voltage", LEDGroup.BORDER_TITLE, 10, 20, 10, 0, 
                                    16, 10, Color.GREEN, panel.getBackground()); 
    voltageLeds.init();
    voltageLeds.setRange(0, 10);
    voltageLeds.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(voltageLeds);
    
    //add a button
    electrodePowerOnOffBtn = new JToggleButton("Electrode Power is Off");
    electrodePowerOnOffBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    electrodePowerOnOffBtn.setActionCommand("Change status of electrode power");
    electrodePowerOnOffBtn.addActionListener(this);
    electrodePowerOnOffBtn.setToolTipText("Turn electrode power on");
    Tools.setSizes(electrodePowerOnOffBtn, 160, 20);
    electrodePowerOnOffBtn.setSelected(false);
    panel.add(electrodePowerOnOffBtn);
    
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
    //two panels are used so that a line can be placed in the middle of the
    //spacer -- MatteBorder is used
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(panel, 300, 6);
    panel.setBorder(BorderFactory.createMatteBorder
                                                (0, 0, 1, 0, Color.LIGHT_GRAY));
    outerPanel.add(panel);
    
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(panel, 0, 5);
    outerPanel.add(panel);
    
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
    JLabel cutDepthLbl = new JLabel("Target Cut Depth: ");
    cutDepthLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    cutDepthLbl.setToolTipText("");
    outerPanel.add(cutDepthLbl);
    
    // hss wip -- some of these values should be passed in and determined
    // from elsewhere
    MFloatSpinner cutDepthInputSpinner = new MFloatSpinner(5.5, 1.1, 9.9, 0.1, 
                                                            "##0.0", 60, 20);
    cutDepthInputSpinner.addChangeListener(this);
    cutDepthInputSpinner.setName("Cut Depth Input Spinner -- target depth");
    cutDepthInputSpinner.setToolTipText
                                ("Set the target cut depth of the notcher");
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
// NotcherUI::createNotcherSettings
//
// Creates and initializes a notcherSettings for this unit.
//

public void createNotcherSettings()
{
    
    notcherSettings = new NotcherSettings(notcherName, mainFrame, eventHandler, 
                                                                    this, this);
    notcherSettings.init();
    notcherSettings.setVisible(false);

}// end of NotcherUI::createNotcherSettings
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::ActivateNotcherSettings
//
// Activate the Settings given to this notcherUI by making the JDialog
// visible.
//

public void ActivateNotcherSettings()
{
    
    notcherSettings.setVisibleState(true);

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
    
    changeNameDialog = new JDialog(mainFrame);
    Tools.setSizes(changeNameDialog, 300, 100);
    
    changeNameDialog.setTitle("Change the Name of: " + notcherName);
    
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    Tools.setSizes(mainPanel, 300, 100);
    changeNameDialog.setContentPane(mainPanel);
    
    //vertical spacer
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    
    mainPanel.add(createChangeNameTextField());
    
    mainPanel.add(createChangeNameApplyAndCancelButtons());;
    
    centerJDialog(changeNameDialog, mainFrame);
    
    changeNameDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    
    changeNameDialog.setVisible(true);
    
}// end of NotcherUI::createChangeNameDialog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createChangeNameTextField
//
// Creates a JTextField (used for the user input of a new notcherName) and adds 
// it to a returned panel.
//

public JPanel createChangeNameTextField()
{
    
    // add a containing JPanel
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    //add text field
    changeNameTextField = new JTextField("Enter new name...");
    changeNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    changeNameTextField.selectAll();
    Tools.setSizes(changeNameTextField, 175, 24);
    //text fields don't have action commands or action listeners
    changeNameTextField.setToolTipText("Enter a new name for this unit.");
    panel.add(changeNameTextField);
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    return panel;

}// end of NotcherUI::createChangeNameTextField
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createChangeNameApplyAndCancelButtons
//
// Creates buttons (used for applying and canceling the process of changing
// the notcherName) and adds them to a returned panel.
//

public JPanel createChangeNameApplyAndCancelButtons()
{
    
    // add a containing JPanel
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    //add a button
    JButton applyNewNameBtn = new JButton("Ok"); 
    Tools.setSizes(applyNewNameBtn, 65, 20);
    applyNewNameBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    applyNewNameBtn.setActionCommand("Apply this name to the unit");
    //hsswipp// use keyEvent so that you don't need to press 'alt'
    applyNewNameBtn.setMnemonic(KeyEvent.VK_ENTER);
    applyNewNameBtn.addActionListener(this);
    applyNewNameBtn.setToolTipText("Apply this name to " + notcherName);
    panel.add(applyNewNameBtn);
    
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(5, 0)));
    
    //add a button
    JButton cancelBtn = new JButton("Cancel"); 
    Tools.setSizes(cancelBtn, 75, 20);
    cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    cancelBtn.setActionCommand
                        ("Cancel the process of changing the name of the unit");
    cancelBtn.addActionListener(this);
    cancelBtn.setToolTipText
                ("Cancel the process of changing the name of " + notcherName);
    panel.add(cancelBtn);
    
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    
    return panel;

}// end of NotcherUI::createChangeNameApplyAndCancelButtons
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::changeNotcherName
//
// Changes the name of the notcher unit to the value of the changeNameTextField.
// Changes various strings and resets various objects that use notcherName.
//

public void changeNotcherName()
{
    
    previousNotcherName = notcherName;
    
    notcherName = changeNameTextField.getText();
    
    //reset various items that use the notcherName
    changeNameDialog.setTitle("Change the Name of: " + notcherName);
    nameValueLabel.setText(notcherName);
    notcherSettings.setNames(notcherName);
    
    String message = "The name of '" + previousNotcherName + "'" +
                        " has been changed to '" + notcherName + "'.";
    JOptionPane.showMessageDialog(mainFrame, message);
    
    disposeChangeNameDialog();

}// end of NotcherUI::changeNotcherName
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::changeElectrodePowerButtonLabelAndTip
//
// Changes the electrode power button and tool tip text according to the state 
// of the button.
//

public void changeElectrodePowerButtonLabelAndTip(){
    
    if (getElectrodePowerBtnState()) {
        electrodePowerOnOffBtn.setText("Electrode Power is On");
        electrodePowerOnOffBtn.setToolTipText("Turn electrode power off");
    }
    else {
        electrodePowerOnOffBtn.setText("Electrode Power is Off");
        electrodePowerOnOffBtn.setToolTipText("Turn electrode power on");
    }
        
}// end of NotcherUI::changeElectrodePowerButtonLabelAndTip
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::getElectrodePowerBtnState

public boolean getElectrodePowerBtnState() {
    
    if (electrodePowerOnOffBtn.isSelected()) { return (true); }
    else { return (false); }   

}// end of NotcherUI::getElectrodePowerBtnState
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::disposeChangeNameDialog
//
// Disposes of the changeNameDialog.
// Releases all of the native screen resources used by the dialog, its 
// subcomponents, and all of its owned children.
//

public void disposeChangeNameDialog()
{
    
    changeNameDialog.dispose();

}// end of NotcherUI::disposeChangeNameDialog
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
// NotcherUI::centerJDialog
//
// Centers a passed in JDialog according to the location and size of the passed
// in parent frame and the JDialog's size.
//

public void centerJDialog(JDialog pDialog, JFrame pParentFrame)
{

    int parentFrameXPos = (int)pParentFrame.getX();
    int parentFrameHalfWidth = (int)pParentFrame.getWidth()/2;
    
    int parentFrameYPos = (int)pParentFrame.getY();
    int parentFrameHalfHeight = (int)pParentFrame.getHeight()/2;
    
    int parentFrameXCenter = parentFrameXPos + parentFrameHalfWidth;
    int parentFrameYCenter = parentFrameYPos + parentFrameHalfHeight;
    
    int dialogWidthCenter = (int)pDialog.getWidth()/2;
    int dialogHeightCenter = (int)pDialog.getHeight()/2;
    
    int xPosition = parentFrameXCenter - dialogWidthCenter;
    int yPosition = parentFrameYCenter - dialogHeightCenter;
    
    pDialog.setLocation(xPosition, yPosition);

}// end of NotcherUI::centerJDialog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

public void displayErrorMessage(String pMessage)
{

    JOptionPane.showMessageDialog(mainFrame, pMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE);

}//end of NotcherUI::displayErrorMessage
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