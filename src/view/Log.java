/******************************************************************************
* Title: Log.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class displays a window for displaying information.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package view;

import java.awt.*;
import javax.swing.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Log
//
// This class displays a text area in a window.
//

public class Log extends JDialog{

    private final JTextArea textArea;
    public JTextArea getTextArea() { return(textArea); }

    static public final String newline = "\n";

//-----------------------------------------------------------------------------
// Log::Log (constructor)
//

public Log(JFrame frame)
{

    super(frame, "Log");

    int panelWidth = 400;
    int panelHeight = 500;

    setMinimumSize(new Dimension(panelWidth, panelHeight));
    setPreferredSize(new Dimension(panelWidth, panelHeight));
    setMaximumSize(new Dimension(panelWidth, panelHeight));

    textArea = new JTextArea();

    JScrollPane areaScrollPane = new JScrollPane(textArea);

    add(areaScrollPane);

    setVisible(true);

}//end of Log::Log (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

private void displayErrorMessage(String pMessage)
{

    JOptionPane.showMessageDialog(null, pMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE);

}//end of Log::displayErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::toHex4String
//
// Converts an integer to a 4 character hex string.
//

static String toHex4String(int pValue)
{

    String s = Integer.toString(pValue, 16);

    //force length to be four characters

    if (s.length() == 0) {return "0000" + s;}
    else
    if (s.length() == 1) {return "000" + s;}
    else
    if (s.length() == 2) {return "00" + s;}
    else
    if (s.length() == 3) {return "0" + s;}
    else{
        return s;
    }

}//end of Log::toHex4String
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::toHex8String
//
// Converts an integer to an 8 character hex string.
//

static String toHex8String(int pValue)
{

    String s = Integer.toString(pValue, 16);

    //force length to be eight characters

    if (s.length() == 0) {return "00000000" + s;}
    else
    if (s.length() == 1) {return "0000000" + s;}
    else
    if (s.length() == 2) {return "000000" + s;}
    else
    if (s.length() == 3) {return "00000" + s;}
    else
    if (s.length() == 4) {return "0000" + s;}
    else
    if (s.length() == 5) {return "000" + s;}
    else
    if (s.length() == 6) {return "00" + s;}
    else
    if (s.length() == 7) {return "0" + s;}
    else{
        return s;
    }

}//end of Log::toHex8String
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Log::toUnsignedHex8String
//
// Converts an unsigned integer to an 8 character hex string.
//
// Since Java does not implement unsigned variables, the unsigned integer is
// transferred as a long value which is large enough to contain the full
// positive value of an unsigned integer
//

static String toUnsignedHex8String(long pValue)
{

    String s = Long.toString(pValue, 16);

    //force length to be eight characters

    if (s.length() == 0) {return "00000000" + s;}
    else
    if (s.length() == 1) {return "0000000" + s;}
    else
    if (s.length() == 2) {return "000000" + s;}
    else
    if (s.length() == 3) {return "00000" + s;}
    else
    if (s.length() == 4) {return "0000" + s;}
    else
    if (s.length() == 5) {return "000" + s;}
    else
    if (s.length() == 6) {return "00" + s;}
    else
    if (s.length() == 7) {return "0" + s;}
    else{
        return s;
    }

}//end of Log::toUnsignedHex8String
//-----------------------------------------------------------------------------

}//end of class Log
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
