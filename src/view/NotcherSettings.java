/******************************************************************************
* Title: NotcherSettings.java
* Author: Hunter Schoonover
* Date: 2/3/14
*
* Purpose:
*
* This class creates a JDialog for the notcher unit settings.
* 
*
*/

//-----------------------------------------------------------------------------

package view;

//-----------------------------------------------------------------------------

import controller.EventHandler;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class NotcherSettings
//

public class NotcherSettings extends JDialog {
    
    private EventHandler eventHandler;
    private WindowListener windowListener;
    private ActionListener actionListener;
    
    private final JFrame parentFrame;
    
    private JPanel mainPanel;
    
    private JTextField dataVersionTField;
    private JTextField dataTArea1;
    private JTextField dataTArea2;
    
    private JLabel statusLabel, infoLabel;
    private JLabel progressLabel;
    private JLabel currentCuttingHeadPosition;
    
    private JButton sendTargetDepthBtn;
    
    private NotcherUI notcherUI;
    
    // hss wip -- set to private and create a getter
    public LEDGroup voltageLeds, currentLeds;
    private LEDGroup powerOKLed, shortLed;
    
    private String notcherName;
    private String dialogName;
    
//-----------------------------------------------------------------------------
// NotcherSettings::NotcherSettings (constructor)
//

public NotcherSettings(String pNotcherName, JFrame pParentFrame, 
                        EventHandler pEventHandler, 
                        WindowListener pWindowListener, 
                        ActionListener pActionListener) {
    
    super(pParentFrame);
    
    parentFrame = pParentFrame;
    notcherName = pNotcherName;
    eventHandler = pEventHandler;
    windowListener = pWindowListener;
    actionListener = pActionListener;
    
}//end of NotcherSettings::NotcherSettings (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    setNames(notcherName);
    
    setupContentPane();
    
    //create user interface: buttons, displays, etc.
    setupGui();
    
    //arrange all the GUI items
    pack();

}//end of NotcherSettings::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::setNames
//
// Sets the notcherName and the dialogueName according to the passed in string.
//

public void setNames(String pNotcherName)
{

    notcherName = pNotcherName;
    dialogName = "Tools & Settings :: " + pNotcherName;
    //set the title of the dialogue
    setTitle(dialogName);

}//end of NotcherSettings::setDialogName
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::setupContentPane
//
// Sets various options and styles for the main frame (used for content pane).
//

public void setupContentPane()
{

    //add a JPanel to the frame to provide a familiar container
    mainPanel = new JPanel();
    //set the min/max/preferred sizes of the panel to set the size of the frame
    Tools.setSizes(mainPanel, 300, 90);
    setContentPane(mainPanel);

}// end of NotcherSettings::setupContentPane
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::setupGui
//
// Sets up the user interface on the mainPanel: buttons, displays, etc.
//

public void setupGui()
{
    
    JPanel outerPanel = new JPanel();
    JPanel panel;
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(outerPanel, 300, 90);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(createModeSelectionPanel());
    
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(20,0)));
    
    panel.add(createStartCycleTestButton());
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
    panel.add(createLockToolsAndSettingsButton());
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    add(outerPanel);

}// end of NotcherSettings::setupGui
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::createModeSelectionPanel
//
// Creates two radio buttons for the selection of "notch mode" or "wall mode"
// and add them to a returned panel.
//

public JPanel createModeSelectionPanel()
{
    
    JPanel outerPanel = new JPanel();
    JPanel panel;
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //Create the radio buttons.
    //hsswip -- more info on modes
    JRadioButton notchModeRadBtn = new JRadioButton("notch mode");
    notchModeRadBtn.setSelected(true);
    notchModeRadBtn.setActionCommand("Switch to notch mode");
    notchModeRadBtn.addActionListener(actionListener);
    notchModeRadBtn.setToolTipText("Switch to notch mode");
    panel.add(notchModeRadBtn);
    
    JRadioButton wallModeRadBtn = new JRadioButton("wall mode");
    wallModeRadBtn.setActionCommand("Switch to wall mode");
    wallModeRadBtn.addActionListener(actionListener);
    wallModeRadBtn.setToolTipText("Switch to wall mode");
    panel.add(wallModeRadBtn);
    
    //Group the radio buttons.
    ButtonGroup radioButtonGroup = new ButtonGroup();
    radioButtonGroup.add(notchModeRadBtn);
    radioButtonGroup.add(wallModeRadBtn);
    
    outerPanel.add(panel);
    
    return outerPanel;
    
}// end of NotcherSettings::createModeSelectionPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::createStartCycleTestButton
//
// Creates a button used to start/stop the cycle test and adds it to a returned
// panel.
//

public JPanel createStartCycleTestButton()
{
    
    JPanel outerPanel = new JPanel();
    JPanel panel;
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // hsswip -- need more information about the effects
    //add a button
    JButton cycleTestStartAndStopBtn = new JButton("Start cycle test");
    cycleTestStartAndStopBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    cycleTestStartAndStopBtn.setActionCommand("Change name of unit");
    cycleTestStartAndStopBtn.addActionListener(actionListener);
    cycleTestStartAndStopBtn.setToolTipText("Change the name of this unit");
    Tools.setSizes(cycleTestStartAndStopBtn, 150, 20);
    panel.add(cycleTestStartAndStopBtn);
    outerPanel.add(panel);
    
    return outerPanel;
    
}// end of NotcherSettings::createStartCycleTestButton
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::createLockToolsAndSettingsButton
//
// Creates a JCheckBox for (un)locking the tools and settings for this device
// and adds it to a returned panel.
//

public JPanel createLockToolsAndSettingsButton()
{
    
    JPanel outerPanel;
    
    outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
    outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    //create and add a button to outerPanel
    JCheckBox lockToolsAndSettingsButton = new JCheckBox
                                                    ("Lock Tools and Settings");
    lockToolsAndSettingsButton.setActionCommand
                                ("Lock the tools and settings on this device so"
                                        + " that they may not be changed");
    lockToolsAndSettingsButton.addActionListener(actionListener);
    lockToolsAndSettingsButton.setToolTipText
                                ("Lock the tools and settings on this device so"
                                        + "they may not be changed");
    lockToolsAndSettingsButton.setSelected(true);
    outerPanel.add(lockToolsAndSettingsButton);
    
    return outerPanel;
    
}// end of NotcherSettings::createLockToolsAndSettingsButton
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::setVisibleState
//
// Sets this JDialog's visible attribute to the boolean passed in.
//

public void setVisibleState(Boolean pValue)
{
    
    setVisible(pValue);
    
    centerJDialog(this, parentFrame);

}// end of NotcherSettings::setVisibleState
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::centerJDialog
//
// Centers a passed in JDialog according to the location and size of the passed
// in parent frame and the JDialog's size.
//

public void centerJDialog(JDialog pDialog, JFrame pParentFrame)
{

    int parentFrameXPos = (int)parentFrame.getX();
    int parentFrameHalfWidth = (int)parentFrame.getWidth()/2;
    
    int parentFrameYPos = (int)parentFrame.getY();
    int parentFrameHalfHeight = (int)parentFrame.getHeight()/2;
    
    int parentFrameXCenter = parentFrameXPos + parentFrameHalfWidth;
    int parentFrameYCenter = parentFrameYPos + parentFrameHalfHeight;
    
    int dialogWidthCenter = (int)pDialog.getWidth()/2;
    int dialogHeightCenter = (int)pDialog.getHeight()/2;
    
    int xPosition = parentFrameXCenter - dialogWidthCenter;
    int yPosition = parentFrameYCenter - dialogHeightCenter;
    
    pDialog.setLocation(xPosition, yPosition);

}// end of NotcherSettings::centerJDialog
//-----------------------------------------------------------------------------

}//end of class NotcherSettings
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------