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
    
    private static final int Y_OFFSET = 20;
    private static final int Y_PADDING = 10;
    
    private int xOffset;
    private int xPadding;
    private int xGap;
    
    private int ledArrayLength;
    private int ledWidth, ledHeight;
    private int highestLitLedIndex;
    
    private double stepValue;
    private double inputValue;
    private double minValue, maxValue;
    private double range;
    
//-----------------------------------------------------------------------------
// LEDGroup::LEDGroup (constructor)
//

public LEDGroup(String pTitle, int pLedArrayLength, int pLedWidth,
                    int pLedHeight, int pXOffset, int pXPadding, int pXGap, 
                    Color pOnColor, Color pOffColor)
{
    
    title = pTitle;
    ledArrayLength = pLedArrayLength;
    ledWidth = pLedWidth;
    ledHeight = pLedHeight;
    xOffset = pXOffset;
    xPadding = pXPadding;
    xGap = pXGap;
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
//      X and Y padding
//

public void determineAndSetSize()
{
    
    // hss whip
    
    int width = xOffset + xPadding + xGap * (ledArrayLength - 1) + 
                    ledWidth * ledArrayLength;
    
    int height = Y_OFFSET + Y_PADDING + ledHeight;
    
    Tools.setSizes(this, width, height);
    
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
        
        ledXValue = xOffset + xPadding/2 + i * (ledWidth + xGap);
        
        ledArray[i] = new LED(ledXValue, Y_OFFSET, ledWidth, ledHeight, 
                                    onColor, offColor);
        
        ledArray[i].init();
        
    }// end of for (int i = 0; i < ledArray.length; i++)
    
}// end of LEDGroup::createLeds
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::setRange
//
// Sets the minimum and maximum input values that will be used.
// Determines the step size of each led; sets the value at which an led should
// be turned on.
//

public void setRange(double pMinValue, double pMaxValue)
{
    
    minValue = pMinValue;
    maxValue = pMaxValue;
    
    range = maxValue - minValue;
    
    stepValue = range / ledArrayLength;
    
}// end of LEDGroup::setRange
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::setValue
//
// Sets the input value and determines the index of the highest led to turn on.
//

public void setValue(double pInputValue)
{
    
    inputValue = pInputValue;
    
    if (inputValue < minValue) {
        inputValue = minValue;
    }
    
    if (inputValue > maxValue) {
        inputValue = maxValue;
    }
    
    highestLitLedIndex = (int)((inputValue - minValue) / stepValue) - 1;
    
    if (highestLitLedIndex < 0) {
        highestLitLedIndex = -1;
    }
    
    if (highestLitLedIndex >= ledArray.length) {
        highestLitLedIndex = ledArray.length - 1;
    }
    
    setAllLedStatesToRepresentInputValue();
    
}// end of LEDGroup::setValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::setAllLedStatesToRepresentInputValue
//
// Sets the states of all of the leds in the led array to on or off
// corresponding to the input value.
//

public void setAllLedStatesToRepresentInputValue()
{
    
    for (int i = 0; i < ledArray.length; i++) {
            ledArray[i].setState(LED.OFF);
    }
    
    // if input value is less than lowest step, no leds are lit
    if (highestLitLedIndex == -1) {
        return;
    }
    
    for (int i = 0; i < ledArray.length; i++) {
        
        if (i <= highestLitLedIndex) {
            ledArray[i].setState(LED.ON);
        }
        
    }// end of for (int i = 0; i < ledArray.length; i++)
    
}// end of LEDGroup::setAllLedStatesToRepresentInputValue
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::setBorder
//
// Creates a border for the panel.
//

public void setBorder()
{

    // A "!NO_BORDER!" for a title signifies that there should be no border.
    if ("!NO_BORDER!".equals(title)){
        return;
    }
    
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
    for (LED ledArray1 : ledArray) {
        ledArray1.paint(g2);
    }

}// end of LEDGroup::paintComponent
//-----------------------------------------------------------------------------
    
}//end of class LEDGroup
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
