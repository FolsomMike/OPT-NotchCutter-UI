/******************************************************************************
* Title: LED.java
* Author: Hunter Schoonover
* Date: 1/12/14
*
* Purpose:
*
* This class creates an LED rectangle.
* 
*
*/

//-----------------------------------------------------------------------------

package view;

//-----------------------------------------------------------------------------

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


//-----------------------------------------------------------------------------
// class LED
//

public class LED extends Object {
    
    Rectangle2D.Double LED;
    
    Rectangle2D.Double LEDOutline;
    
    public static final int OFF = 0, ON = 1;
    
    int ledState;
    int x, y;
    int ledWidth, ledHeight;
    
    Color fillColor;
    Color onColor;
    Color offColor;
    
//-----------------------------------------------------------------------------
// LED::LED (constructor)
//

public LED(int pX, int pY, int pLedWidth, int pLedHeight, Color pOnColor, 
                Color pOffColor)
{
    
    x = pX;
    y = pY;
    ledWidth = pLedWidth;
    ledHeight = pLedHeight;
    onColor = pOnColor;
    offColor = pOffColor;
    
}//end of LED::LED (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LED::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{
    
    fillColor = offColor;
      
}// end of LED::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LED::setState
//
// Changes the LED's fill color to match the state passed in as a parameter.
//

public void setState(int pState)
{
    ledState = pState;
    
    if (ledState == ON) {
        fillColor = onColor;
    }
    
    else if (ledState == OFF) {
        fillColor = offColor;
    } 
      
}// end of LED::setState
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LED::paint
//
// Paints the necessary parts to draw an LED.
//

public void paint(Graphics2D pG2)
{
    
    pG2.setColor(Color.BLACK);
    
    pG2.draw(LEDOutline = new Rectangle2D.Double(x, y, ledWidth, ledHeight));
    
    pG2.setColor(fillColor);
    
    pG2.fill(LED = new Rectangle2D.Double(x + 1, y + 1, ledWidth - 1, 
                                            ledHeight - 1));
    
}// end of LED::paint
//-----------------------------------------------------------------------------
    
}//end of class LED
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------