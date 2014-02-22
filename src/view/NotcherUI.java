/******************************************************************************
* Title: NotcherUI.java
* Author: Hunter Schoonover
* Date: 2/3/14
*
* Purpose:
*
* This class creates a visual interface for a notch cutter.
*
*/

//-----------------------------------------------------------------------------

package view;

//-----------------------------------------------------------------------------

import controller.EventHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class NotcherUI
//

public class NotcherUI extends JPanel implements ActionListener {
    
    private EventHandler eventHandler;
    
    private JTextField dataVersionTField;
    private JTextField dataTArea1;
    private JTextField dataTArea2;
    
    private JLabel statusLabel, infoLabel;
    private JLabel progressLabel;
    
    // hss wip -- set to private and create a getter
    public LEDGroup voltageLeds;
    public LEDGroup currentLeds;
    
    private int indexNumber;
    
//-----------------------------------------------------------------------------
// NotcherUI::NotcherUI (constructor)
//

public NotcherUI(EventHandler pEventHandler, int pIndexNumber)
{

    eventHandler = pEventHandler;
    indexNumber = pIndexNumber;

}//end of NotcherUI::NotcherUI (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    Tools.setSizes(this, 300, 400);
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    this.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.black), 
                            "NotcherUI")); // debug hss
    
    //create user interface: buttons, displays, etc.
    setupGui();

}//end of NotcherUI::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::setupGui
//
// Sets up the user interface on the mainPanel: buttons, displays, etc.
//

public void setupGui()
{
    
    createCurrentPanel();
    
    createVoltagePanel();
    
    add(Box.createRigidArea(new Dimension(0,20))); //vertical spacer

    //create a label to display good/warning/bad system status
    statusLabel = new JLabel("Status");
    statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(statusLabel);

    add(Box.createRigidArea(new Dimension(0,20))); //vertical spacer

    //create a label to display miscellaneous info
    infoLabel = new JLabel("Info");
    infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(infoLabel);

    add(Box.createRigidArea(new Dimension(0,20))); //vertical spacer

    //add text field
    dataVersionTField = new JTextField("unknown");
    dataVersionTField.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(dataVersionTField, 100, 24);
    //text fields don't have action commands or action listeners
    dataVersionTField.setToolTipText("The data format version.");
    add(dataVersionTField);

    add(Box.createRigidArea(new Dimension(0,3))); //vertical spacer

    //add text field
    dataTArea1 = new JTextField("");
    dataTArea1.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(dataTArea1, 100, 24);
    //text fields don't have action commands or action listeners
    dataTArea1.setToolTipText("A data entry.");
    add(dataTArea1);

    add(Box.createRigidArea(new Dimension(0,3))); //vertical spacer

    //add text field
    dataTArea2 = new JTextField("");
    dataTArea2.setAlignmentX(Component.LEFT_ALIGNMENT);
    Tools.setSizes(dataTArea2, 100, 24);
    //text fields don't have action commands or action listeners
    dataTArea2.setToolTipText("A data entry.");
    add(dataTArea2);

    add(Box.createRigidArea(new Dimension(0,20))); //vertical spacer

    //add button
    JButton loadBtn = new JButton("Load");
    loadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    loadBtn.setActionCommand("Load Data From File");
    loadBtn.addActionListener(this);
    loadBtn.setToolTipText("Load data from file.");
    add(loadBtn);

    add(Box.createRigidArea(new Dimension(0,10))); //vertical spacer

    //add a button
    JButton saveBtn = new JButton("Save");
    saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    saveBtn.setActionCommand("Save Data To File");
    saveBtn.addActionListener(this);
    saveBtn.setToolTipText("Save data to file.");
    add(saveBtn);

    add(Box.createRigidArea(new Dimension(0,10))); //vertical spacer

    progressLabel = new JLabel("Progress");
    progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(progressLabel);

    add(Box.createRigidArea(new Dimension(0,10))); //vertical spacer

}// end of NotcherUI::setupGui
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createCurrentPanel
//
// Goes through the process of creating an LEDGroup for current and adding it
// to the panel.
//

public void createCurrentPanel()
{
    
    currentLeds = new LEDGroup("Current", 10, 20, 10, Color.RED, 
                                getBackground()); 
    currentLeds.init();
    currentLeds.setRange(0, 10);
    currentLeds.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(currentLeds);
    
}// end of NotcherUI::createCurrentPanel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherUI::createVoltagePanel
//
// Goes through the process of creating an LEDGroup for votlage and adding it to
// the panel.
//

public void createVoltagePanel()
{
    
    voltageLeds = new LEDGroup("Voltage", 10, 20, 10, Color.GREEN, 
                                getBackground()); 
    voltageLeds.init();
    voltageLeds.setRange(0, 10);
    voltageLeds.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(voltageLeds);
    
}// end of NotcherUI::createVoltagePanel
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
    
}//end of class NotcherUI
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------