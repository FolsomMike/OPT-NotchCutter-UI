/******************************************************************************
* Title: FileHandler.java - Main file handler for the program
* Author: Hunter Schoonover
* Date: 06/10/14
*
* Purpose:
*
* This class reads and writes to different files.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

//-----------------------------------------------------------------------------
// class FileHandler
//
// This class loads, saves, and encapsulates option settings used by the
// program.
//

public class FileHandler extends Object{
    
    BufferedReader fileReader;
    Map<String, String> mapCollection;
    
    Path file = Paths.get("test.txt");//hss wip

//-----------------------------------------------------------------------------
// FileHandler::FileHandler (constructor)
//
//

public FileHandler()
{
    
}//end of FileHandler::FileHandler (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileHandler::init
//
// Initializes new objects. Should be called immediately after instantiation.
//

public void init()
{
    
    createBufferedReader();
    readFileIntoMapCollection();

}//end of FileHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileHandler::createBufferedReader
//
// Creates a newBufferedReader for use with the file.
//

public void createBufferedReader()
{
    
    try { 
        
        fileReader = Files.newBufferedReader(file);
        
    } catch (IOException | SecurityException e) {
        
        System.err.format("IOException: %s%n", e);
        
        try {
            if (fileReader != null) { fileReader.close(); }
        } catch (IOException x) {}
        
    }
    
}//end of FileHandler::createBufferedReader
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileHandler::readFileIntoMapCollection
//
// Read the file into a Map Collection.
//

public void readFileIntoMapCollection()
{
    
    String line;
    
    try {
        
        while ((line = fileReader.readLine()) != null) {
            //parses for the key and value pair and puts
            //them into a map collection
            parseKeyValuePair(line);
        }
        
    } catch (IOException e) {}
    
}//end of FileHandler::readFileIntoMapCollection
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileHandler::parseKeyValuePair
//
// Parses the passed in string for the key and value pair and puts them into
// a map collection.
//

public void parseKeyValuePair(String pLine)
{
    
    mapCollection = new LinkedHashMap<>();
    
    int pLineLength = pLine.length();
    int posOfEqualsSign = pLine.indexOf("=");
    
    String key = pLine.substring(0, posOfEqualsSign);
    String value = pLine.substring(posOfEqualsSign+1, pLineLength);
    
    mapCollection.put(key, value);

    for (Map.Entry<String, String> entry : mapCollection.entrySet()) {
        //debug hss
        System.out.println(entry.getKey() + "=" + entry.getValue());
    }
    
}//end of FileHandler::parseKeyValuePair
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// FileHandler::saveDataToFile
//
// Saves everything in the Map Collection to the file; creates new or 
// overwrites pre-existing.
//

public void saveDataToFile()
{

}//end of FileHandler::saveDataToFile
//-----------------------------------------------------------------------------

}//end of class FileHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
