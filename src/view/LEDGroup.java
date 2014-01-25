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
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class LEDGroup
//

public class LEDGroup extends JPanel{
    
    private LED[] ledArray;
    
    private Color onColor;
    private Color offColor;
    
    private String title;
    
    private static final int X_OFFSET = 10;
    private static final int X_PADDING = 10;
    private static final int X_GAP = 5;
    
    private static final int Y_OFFSET = 20;
    private static final int Y_PADDING = 5;
    
    private int ledArrayLength;
    private int ledWidth;
    private int ledHeight;
    
//-----------------------------------------------------------------------------
// LEDGroup::LEDGroup (constructor)
//

public LEDGroup(String pTitle, int pLedArrayLength, int pLedWidth,
                    int pLedHeight, Color pOnColor, Color pOffColor)
{
    
    title = pTitle;
    ledArrayLength = pLedArrayLength;
    ledWidth = pLedWidth;
    ledHeight = pLedHeight;
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
    
    determineAndSetSize();
    
    createLeds();
    
    setBorder();
    
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
//      X and Y offsets
//      room for the border
//

public void determineAndSetSize()
{
    
    // hss whip
    
    int width = X_OFFSET + X_PADDING + X_GAP * (ledArrayLength - 1) + 
                    ledWidth * ledArrayLength;
    
    Tools.setSizes(this, width, 40);
    
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
        
        ledXValue = X_OFFSET + i * (ledWidth + X_GAP);
        
        ledArray[i] = new LED(ledXValue, Y_OFFSET, ledWidth, ledHeight, 
                                    onColor, offColor);
        
        ledArray[i].init();
        
        ledArray[i].turnOn();
        
    }
    
}// end of LEDGroup::createLeds
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::setBorder
//
// Creates a border for the panel.
//

public void setBorder()
{

    this.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.black), 
                            title));
    
}// end of LEDGroup::setBorder
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
