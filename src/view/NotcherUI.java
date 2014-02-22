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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class NotcherUI
//

public class NotcherUI extends JPanel implements ActionListener {
    
    private EventHandler eventHandler;
    
    // hss wip -- set to private and create a getter
    public LEDGroup voltageLeds;
    public LEDGroup currentLeds;
    
//-----------------------------------------------------------------------------
// NotcherUI::NotcherUI (constructor)
//

public NotcherUI(EventHandler pEventHandler)
{

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

    Tools.setSizes(this, 300, 350);
    
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
    
    voltageLeds = new LEDGroup("Voltage", 10, 20, 10, Color.RED, 
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