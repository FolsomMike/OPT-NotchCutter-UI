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
import javax.swing.JPanel;
import toolkit.Tools;


//-----------------------------------------------------------------------------
// class LED
//

public class LED extends JPanel {
    
    Rectangle2D.Double LED;
    
    Rectangle2D.Double LEDOutline;
    
    public static final int OFF = 0, ON = 1;
    
    int ledState;
    int ledWidth, ledHeight;
    int x = 0, y = 0;
    
    Color fillColor;
    Color onColor;
    Color offColor;
    
//-----------------------------------------------------------------------------
// LED::LED (constructor)
//

public LED(int pLedWidth, int pLedHeight, Color pOnColor, 
                Color pOffColor)
{
    
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
    
    Tools.setSizes(this, ledWidth + 1, ledHeight + 1);
    
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

@Override

public void paintComponent (Graphics g) {
    
    //let the parent class do it's painting, such as the background, border, 
    //etc.
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;
    
    g2.setColor(Color.BLACK);
    
    g2.draw(LEDOutline = new Rectangle2D.Double(x, y, ledWidth, ledHeight));
    
    g2.setColor(fillColor);
    
    g2.fill(LED = new Rectangle2D.Double(x + 1, y + 1, ledWidth - 1, 
                                            ledHeight - 1));
    
}// end of LED::paint
//-----------------------------------------------------------------------------
    
}//end of class LED
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------