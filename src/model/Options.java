/******************************************************************************
* Title: Options.java - Main Source File for Temperature Monitor
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class stores options which are needed by various parts of the program.
* Some of the options are selected directly by the user, others might be
* set due to programmatic situations.
*
* The class can store and retrieve those options which must be non-volatile.
*
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package model;

//-----------------------------------------------------------------------------

import java.io.IOException;

//-----------------------------------------------------------------------------
// class Options
//
// This class loads, saves, and encapsulates option settings used by the
// program.
//

public class Options extends Object{

    public static String SOFTWARE_VERSION = "1.0";
    
    private int xPositionMainWindow;
    public int getXPositionMainWindow(){return (xPositionMainWindow);}
    
    private int yPositionMainWindow;
    public int getYPositionMainWindow(){return (yPositionMainWindow);}    

//-----------------------------------------------------------------------------
// Options::Options (constructor)
//
//

public Options()
{

}//end of Options::Options (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Options::init
//
// Initializes new objects. Should be called immediately after instantiation.
//

public void init()
{
    
    IniFile generalFile = new IniFile("General Settings.ini", "UTF-8");
    try {
        generalFile.init();
    } catch(IOException e) {
        return;
    }
    
    xPositionMainWindow = generalFile.readInt
        ("General", "X Position of Main Window", Integer.MIN_VALUE);
    yPositionMainWindow = generalFile.readInt
        ("General", "Y Position of Main Window", Integer.MIN_VALUE);

    
}//end of Options::init
//-----------------------------------------------------------------------------


}//end of class Options
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
