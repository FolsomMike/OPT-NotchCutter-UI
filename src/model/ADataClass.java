/******************************************************************************
* Title: ADataClass.java - Main Source File for Temperature Monitor
* Author: Mike Schoonover
* Date: 9/30/13
*
* Purpose:
*
* This is a sample "model" class which handles some data: loads, saves, etc.
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
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

//-----------------------------------------------------------------------------
// class ADataClass

public class ADataClass extends Object{

    private String dataVersion = "1.0";

    private String [] data;

    static final int DATA_SIZE = 2;

    private FileInputStream fileInputStream = null;
    private InputStreamReader inputStreamReader = null;
    private BufferedReader in = null;

    private FileOutputStream fileOutputStream = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedWriter out = null;


//-----------------------------------------------------------------------------
// ADataClass::ADataClass (constructor)
//
//

public ADataClass()
{

}//end of ADataClass::ADataClass (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::init
//
// Initializes new objects. Should be called immediately after instantiation.
//

public void init()
{

    //allocate the array and fill it with strings

    data = new String[DATA_SIZE];

    for (int i=0; i<DATA_SIZE; i++){

        data[i] = new String();
        data[i] = "";

    }

}//end of ADataClass::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::getDataVersion
//
// Returns dataVersion.
//

public String getDataVersion()
{

    return dataVersion;

}//end of ADataClass::getDataVersion
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::getDataItem
//
// Returns item indexed by pIndex from array data.
//

public String getDataItem(int pIndex)
{

    return data[pIndex];

}//end of ADataClass::getDataItem
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::setDataVersion
//
// Sets dataVersion to pValue.
//

public void setDataVersion(String pValue)
{

    dataVersion = pValue;

}//end of ADataClass::setDataVersion
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::setDataItem
//
// Sets data array element indexed by pIndex to pValue.
//

public void setDataItem(int pIndex, String pValue)
{

    data[pIndex] = pValue;

}//end of ADataClass::setDataItem
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::loadFromTextFile
//
// Loads all data from a text file.
//

public void loadFromTextFile()
{

    try{

        openTextInFile("Sample Data.txt");

        readDataFromTextFile();

    }
    catch (IOException e){

        //display an error message and/or log the message

    }
    finally{

        closeTextInFile();

    }

}//end of ADataClass::loadFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::openTextInFile
//
// Opens text file pFilename for reading.
//

private void openTextInFile(String pFilename) throws IOException
{

    fileInputStream = new FileInputStream(pFilename);
    inputStreamReader = new InputStreamReader(fileInputStream);
    in = new BufferedReader(inputStreamReader);

}//end of ADataClass::openTextInFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::readDataFromTextFile
//
// Reads the data from the text file.
//

private void readDataFromTextFile() throws IOException
{

    //read each data line or until end of file reached

    String line;

    if ((line = in.readLine()) != null){

        dataVersion = line;

    }

    for (int i=0; i<DATA_SIZE; i++){

        if ((line = in.readLine()) == null) { break; }

        data[i] = line;

    }

}//end of ADataClass::readDataFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::closeTextInFile
//
// Closes the text input file.
//

private void closeTextInFile()
{

    try{

        if (in != null) {in.close();}
        if (inputStreamReader != null) {inputStreamReader.close();}
        if (fileInputStream != null) {fileInputStream.close();}

    }
    catch(IOException e){

        //ignore error while trying to close the file
        //could log the error message in the future

    }

}//end of ADataClass::closeTextInFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::saveToTextFile
//
// Saves all data to a text file.
//

public void saveToTextFile()
{

    try{

        openTextOutFile("Sample Data.txt");

        saveDataToTextFile();

    }
    catch (IOException e){

        //display an error message and/or log the message

    }
    finally{

        closeTextOutFile();

    }

}//end of ADataClass::saveToTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::openTextOutFile
//
// Opens text file pFilename for writing.
//

private void openTextOutFile(String pFilename) throws IOException
{

    fileOutputStream = new FileOutputStream(pFilename);
    outputStreamWriter = new OutputStreamWriter(fileOutputStream);
    out = new BufferedWriter(outputStreamWriter);

}//end of ADataClass::openTextOutFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::saveDataToTextFile
//
// Saves the data to the text file.
//

private void saveDataToTextFile() throws IOException
{

    //write each data line

    String line;

    out.write(dataVersion);
    out.newLine();

    for (int i=0; i<DATA_SIZE; i++){

        out.write(data[i]);
        out.newLine();

    }

}//end of ADataClass::readDataFromTextFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// ADataClass::closeTextOutFile
//
// Closes the text output file.
//

private void closeTextOutFile()
{

    try{

        if (out != null) {out.close();}
        if (outputStreamWriter != null) {outputStreamWriter.close();}
        if (fileOutputStream != null) {fileOutputStream.close();}

    }
    catch(IOException e){

        //ignore error while trying to close the file
        //could log the error message in the future

    }

}//end of ADataClass::closeTextOutFile
//-----------------------------------------------------------------------------


}//end of ADataClass
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
