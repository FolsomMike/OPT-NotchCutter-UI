/******************************************************************************
* Title: LEDGroup.java
* Author: Hunter Schoonover
* Date: 1/12/14
*
* Purpose:
*
* This class creates an array of LEDs. The number of LEDs is determined from the
* variables passed in from the constructor.
* 
*
*/

//-----------------------------------------------------------------------------

package view;

//-----------------------------------------------------------------------------

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class LEDGroup
//

public class LEDGroup extends JPanel{
    
    private LED[] ledArray;
    
    private Color onColor;
    private Color offColor;
    
    private static final int X_OFFSET = 10;
    private static final int X_SPACING = 15;
    private static final int Y_OFFSET = 0;
    
    private int ledArrayLength;
    
//-----------------------------------------------------------------------------
// LEDGroup::LEDGroup (constructor)
//

public LEDGroup(int pLedArrayLength, Color pOnColor, Color pOffColor)
{
    
    ledArrayLength = pLedArrayLength;
    onColor = pOnColor;
    offColor = pOffColor;
    
    
}//end of LEDGroup::LEDGroup (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    Tools.setSizes(this, 156, 40);
    
    createLeds();
    
}// end of LEDGroup::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::determineAndSetSize
//
// Determines the size the component needs to be to show all of the leds.
//
// The size is based on:
//      the number of leds
//      size of the leds
//      amount of space between the leds
//      the size of the left and right offsets
//      room for the border
//

public void determineAndSetSize()
{
    
    // hss whip
    
    
    // Tools.setSizes(this, 40, 40);
    
}// end of LEDGroup::determineAndSetSize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::createLeds
//
// Creates an LED object for each index in the ledArray.
//

public void createLeds()
{

    ledArray = new LED[ledArrayLength];
    
    int ledXValue;
    
    for (int i = 0; i < ledArray.length; i++) {
        
        ledXValue = X_OFFSET + i * X_SPACING;
        
        ledArray[i] = new LED(ledXValue, Y_OFFSET, onColor, offColor);
        
        ledArray[i].init();
        
        ledArray[i].turnOn();
        
    }
    
}// end of LEDGroup::createLeds
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::paintComponent
//
// Initializes the object.  Must be called immediately after instantiation.
//

@Override

public void paintComponent (Graphics g)
{

        // let the parent class do it's painting, such as the background, border, etc.
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // paint all our children

        for (int i = 0; i < ledArray.length; i++) {
        
            ledArray[i].paint(g2);
        
        }

}// end of LEDGroup::paintComponent
//-----------------------------------------------------------------------------
    
}//end of class LEDGroup
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
