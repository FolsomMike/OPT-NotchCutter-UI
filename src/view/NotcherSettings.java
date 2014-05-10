/******************************************************************************
* Title: NotcherSettings.java
* Author: Hunter Schoonover
* Date: 2/3/14
*
* Purpose:
*
* This class creates a window for the notcher unit settings.
* 
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class NotcherSettings
//

public class NotcherSettings extends JFrame implements ActionListener, 
        ChangeListener, WindowListener {
    
    private final EventHandler eventHandler;
    
    private JPanel headGuiPanel;
    
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
    
    // hsswip -- name should be set elswhere
    private String notcherName = "NotcherUI";
    
//-----------------------------------------------------------------------------
// NotcherSettings::NotcherSettings (constructor)
//

public NotcherSettings(NotcherUI pNotcherUI, EventHandler pEventHandler) {
    
    notcherUI = pNotcherUI;
    eventHandler = pEventHandler;
    
}//end of NotcherSettings::NotcherSettings (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    setupMainFrame();
    
    //create user interface: buttons, displays, etc.
    setupGui();

}//end of NotcherSettings::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSettings::setupMainFrame
//
// Sets various options and styles for the main frame.
//

public void setupMainFrame()
{

    JFrame mainFrame = new JFrame(notcherName + " Settings");

    //add a JPanel to the frame to provide a familiar container
    JPanel mainPanel = new JPanel();
    mainFrame.setContentPane(mainPanel);

    //set the min/max/preferred sizes of the panel to set the size of the frame
    Tools.setSizes(mainPanel, 600, 600);

    mainFrame.addWindowListener(this);

    //turn off default bold for Metal look and feel
    UIManager.put("swing.boldMetal", Boolean.FALSE);

    //force "look and feel" to Java style
    try {
        UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        }
    catch (ClassNotFoundException | InstantiationException |
            IllegalAccessException | UnsupportedLookAndFeelException e) {
        System.out.println("Could not set Look and Feel");
        }

    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    centerJFrame(mainFrame);

}// end of NotcherSettings::setupMainFrame
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
    
    // add a containing JPanel
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
    // create a filler to "push" everything in this panel to the center -- only
    // works to "push" to the center if another glue is used
    panel.add(Box.createHorizontalGlue());
    outerPanel.add(panel);
    
    add(outerPanel);

}// end of NotcherSettings::setupGui
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
    outerPanel.add(Box.createRigidArea(new Dimension(100, 0)));
    
    // add a containing JPanel
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(createSaveButton());
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(10, 0)));
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
    JLabel nameValueLabel = new JLabel(notcherName);
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
    Tools.setSizes(panel, 65, 20);
    
    //add a button
    JButton saveBtn = new JButton("Save");
    saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    saveBtn.setActionCommand("Save Data To File");
    saveBtn.addActionListener(this);
    saveBtn.setToolTipText("Save data to file.");
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
// NotcherUI::centerJFrame
//
// Centers a passed in Jframe according to the screen size and JFrame's size.
//

public void centerJFrame(JFrame pFrame)
{
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    pFrame.setLocation((int)screenSize.getWidth()/2 - 
                            (int)pFrame.getWidth()/2, 
                            (int)screenSize.getHeight()/2 - 
                            (int)pFrame.getHeight()/2);

}// end of NotcherUI::centerJFrame
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