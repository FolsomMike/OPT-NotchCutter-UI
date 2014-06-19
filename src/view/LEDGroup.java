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
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;


//-----------------------------------------------------------------------------
// class LEDGroup
//

public class LEDGroup extends JPanel{
    
    private LED[] ledArray;
    
    private final Color onColor;
    private final Color offColor;
    
    private final String title;
    
    private final int displayType;
    //ADD DISPLAY TYPES HERE
    public static final int BORDER_TITLE = 1;
    public static final int SIDE_BY_SIDE = 2;
    
    private static final int Y_OFFSET = 20;
    private static final int Y_PADDING = 10;
    
    private final int xOffset;
    private final int xPadding;
    private final int xGap;
    
    private final int ledArrayLength;
    private final int ledWidth, ledHeight;
    private int highestLitLedIndex;
    
    private double stepValue;
    private double inputValue;
    private double minValue, maxValue;
    private double range;
    
//-----------------------------------------------------------------------------
// LEDGroup::LEDGroup (constructor)
//

public LEDGroup(String pTitle, int pDisplayType, int pLedArrayLength, 
                    int pLedWidth, int pLedHeight, int pXOffset, int pXPadding, 
                    int pXGap, Color pOnColor, Color pOffColor)
{
    
    title = pTitle;
    displayType = pDisplayType;
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
    
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    setUpDisplayType();
    
}// end of LEDGroup::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::setUpDisplayType
//
// Determines in what way to display the LEDGroup depending on the displayType.
//
// For each display type you add, you need to search for 
// "ADD DISPLAY TYPES HERE" and add a constant with an appropriate variable 
// name -- following the format in which the others were created. Then, you 
// need to add another case (to the switch in this function) that executes the
// proper code when your display type was passed in through the constructor.
//

public void setUpDisplayType()
{
    
    switch (displayType) {
        case BORDER_TITLE:
            doActionsForBorderTitleDisplayType();
            break;
            
        case SIDE_BY_SIDE:
            doActionsForSideBySideDisplayType();
            break;
    }
    
}// end of LEDGroup::setUpDisplayType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::doActionsForBorderTitleDisplayType
//
// Performs necessary for the BORDER_TITLE displayType.
//

public void doActionsForBorderTitleDisplayType()
{
    
    this.add(createLedsPanel());
    
    //vertical spacer
    this.add(Box.createRigidArea(new Dimension(0, 3)));
    
    createTitledBorder();
    
}// end of LEDGroup::doActionsForBorderTitleDisplayType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::createTitledBorder
//
// Creates a border for the object (JPanel).
//

public void createTitledBorder()
{
    
    this.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.black), 
                            title));
    
}// end of LEDGroup::createTitledBorder
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::doActionsForSideBySideDisplayType
//
// Performs necessary for the SIDE_BY_SIDE displayType.
//

public void doActionsForSideBySideDisplayType()
{
    
    // add a containing JPanel
    JPanel panel = new JPanel();
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    panel.add(createTitleLabel());
    
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(5, 0)));
    
    panel.add(createLedsPanel());
    
    this.add(panel);
    
}// end of LEDGroup::doActionsForSideBySideDisplayType
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::createTitleLabel
//
// Creates the title label and adds it the the panel.
//

public JLabel createTitleLabel()
{
    
    JLabel titleLabel = new JLabel(title);
    
    return titleLabel;
    
}// end of LEDGroup::createTitleLabel
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// LEDGroup::createLedsPanel
//
// Creates an LED object for each index in the ledArray and adds it to a 
// returned panel.
// Horizontal spacers are used for the offset and the space between the leds.
//

public JPanel createLedsPanel()
{
    
    // add a containing JPanel
    JPanel panel = new JPanel();
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    //the xGap is subtracted because it is added to the panel before the first
    //led -- counteracts extra spacing
    int leftSpacerWidth = xOffset + xPadding/2 - xGap;
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(leftSpacerWidth, 0)));
    
    ledArray = new LED[ledArrayLength];
    
    for (int i=0; i<ledArray.length; i++) {
        
        //horizontal spacer
        panel.add(Box.createRigidArea(new Dimension(xGap, 0)));
        
        //debug hss//
        //led = new LED(ledWidth, ledHeight, onColor, offColor);
        ledArray[i] = new LED(ledWidth, ledHeight, onColor, offColor);
        
        ledArray[i].init();
        
        panel.add(ledArray[i]);
        
    }
    
    int rightSpacerWidth = xPadding/2;
    //horizontal spacer
    panel.add(Box.createRigidArea(new Dimension(rightSpacerWidth, 0)));
    
    return(panel);
    
}// end of LEDGroup::createLedsPanel
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
// LEDGroup::setAllLedsState
//
// Sets all of the leds in the ledArray to either their "ON" or  their "OFF" 
// state depending on the passed in parameter.
//

public void setAllLedsState(int pState)
{
    
    for (LED led : ledArray) { led.setState(pState); }
    
}// end of LEDGroup::setAllLedsState
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
    
    for (LED led : ledArray) {
        led.setState(LED.OFF);
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

}//end of class LEDGroup
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
