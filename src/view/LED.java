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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class LED
//

public class LED {
    
    Rectangle2D.Double LED;
    
    Rectangle2D.Double LEDOutline;
    
    int x, y;
    
    Color fillColor;
    Color onColor;
    Color offColor;
    
//-----------------------------------------------------------------------------
// LED::LED (constructor)
//

public LED(int pX, int pY, Color pOnColor, Color pOffColor)
{
    
    x = pX;
    y = pY;
    
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
      
}// end of LED::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LED::turnOn
//
// Changes the LED's fill color to it's on color.
//

public void turnOn()
{
    
      fillColor = onColor;
      
}// end of LED::turnOn
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LED::turnOff
//
// Changes the LED's fill color to it's off color.
//

public void turnOff()
{
    
      fillColor = offColor;
      
}// end of LED::turnOff
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LED::paint
//
// Paints the necessary parts to draw an LED.
//

public void paint(Graphics2D pG2)
{
    
    pG2.setColor(Color.BLACK);
    
    pG2.draw(LEDOutline = new Rectangle2D.Double(x, y, 10, 10));
    
    pG2.setColor(fillColor);
    
    pG2.fill(LED = new Rectangle2D.Double(x + 1, y + 1, 9, 9));
    
}// end of LED::paint
//-----------------------------------------------------------------------------
    
}//end of class LED
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------