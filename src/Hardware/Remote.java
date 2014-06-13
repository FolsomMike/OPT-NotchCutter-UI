/******************************************************************************
* Title: Remote.java
* Author: Mike Schoonover, Hunter Schoonover
* Date: 5/7/09
*
* Purpose:
*
* This class is the parent class for those which handle various remote devices
* via Ethernet.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package Hardware;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.IniFile;
import view.Log;
import view.ThreadSafeLogger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class InstallFirmwareSettings
//
// This class is used to pass in all necessary settings to the
// installNewRabbitFirmware function.
//

class InstallFirmwareSettings extends Object{

    public byte loadFirmwareCmd;
    public byte noAction;
    public byte error;
    public byte sendDataCmd;
    public byte dataCmd;
    public byte exitCmd;

}//end of class InstallFirmwareSettings
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Remote
//
//

public abstract class Remote extends Object{

    String simulationDataSourceFilePath = "";
    
    short rabbitControlFlags = 0;
    boolean enabled = false;
    int type;

    int controlFlags = 0;
    String configFilename;
    IniFile configFile;
    ThreadSafeLogger tsLog;

    boolean setupComplete = false; //set true if set was completed
    boolean ready = false; //set true if board is successfully setup

    boolean simulate;

    public InetAddress ipAddr;
    String ipAddrS;

    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    byte[] inBuffer;
    byte[] outBuffer;
    DataOutputStream byteOut = null;
    DataInputStream byteIn = null;

    int TIMEOUT = 50;
    int timeOutProcess = 0; //use this one in the packet process functions


//-----------------------------------------------------------------------------
// Remote::Remote (constructor)
//

public Remote(ThreadSafeLogger pTSLog)
{

    tsLog = pTSLog;

}//end of Remote::Remote (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::configure
//
// Loads configuration settings from the configuration.ini file.
//

void configure(IniFile pConfigFile)
{


}//end of Remote::configure
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::sendRabbitControlFlags
//
// Sends the rabbitControlFlags value to the remotes. These flags control
// the functionality of the remotes.
//
// The paramater pCommand is the command specific to the subclass for its
// Rabbit remote.
//

public void sendRabbitControlFlags(final byte pCommand)
{

    sendBytes(pCommand,
                (byte) ((rabbitControlFlags >> 8) & 0xff),
                (byte) (rabbitControlFlags & 0xff)
                );

}//end of Remote::sendRabbitControlFlags
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::isEnabled
//
// Returns true if the channel is enabled, false otherwise.
//

public boolean isEnabled()
{

    return(enabled);

}//end of Remote::isEnabled
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::setIPAddr
//
// Sets the IP address for this board.
//

public void setIPAddr(InetAddress pIPAddr)
{

    ipAddr = pIPAddr;

    ipAddrS = pIPAddr.toString();

}//end of Remote::setIPAddr
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::sendBytes
//
// Sends a variable number of bytes (one or more) to the remote device,
// prepending a valid header and appending the appropriate checksum.
//

void sendBytes(byte... pBytes)
{

    int checksum = 0;

    sendHeader(); //send the packet header

    for(int i=0; i<pBytes.length; i++){
        outBuffer[i] = pBytes[i];
        checksum += pBytes[i];
    }

    //calculate checksum and put at end of buffer
    outBuffer[pBytes.length] = (byte)(0x100 - (byte)(checksum & 0xff));

    //send packet to remote
    if (byteOut != null) {
        try{
              byteOut.write(outBuffer, 0 /*offset*/, pBytes.length + 1);
              byteOut.flush();
        }
        catch (IOException e) {
            logSevere(e.getMessage() + " - Error: 422");
        }
    }

}//end of Remote::sendBytes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::sendHeader
//
// Sends a valid packet header without flushing.
//

void sendHeader()
{

    outBuffer[0] = (byte)0xaa; outBuffer[1] = (byte)0x55;
    outBuffer[2] = (byte)0xbb; outBuffer[3] = (byte)0x66;

    //send packet to remote
    if (byteOut != null) {
        try{
            byteOut.write(outBuffer, 0 /*offset*/, 4);
        }
        catch (IOException e){
            logSevere(e.getMessage() + " - Error: 569");
        }
    }

}//end of Remote::sendHeader
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::waitForNumberOfBytes
//
// Waits until pNumBytes number of data bytes are available in the socket.
//
// Returns true if the bytes become available before timing out, false
// otherwise.
//

boolean waitForNumberOfBytes(int pNumBytes)
{

    try{
        timeOutProcess = 0;
        while(timeOutProcess++ < TIMEOUT){
            if (byteIn.available() >= pNumBytes) {return(true);}
            waitSleep(10);
        }
    }// try
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 595");
        return(false);
    }

    return(false);

}//end of Remote::waitForNumberOfBytes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::readBytes
//
// Retrieves pNumBytes number of data bytes from the packet and stores them
// in inBuffer.
//
// Returns number of bytes retrieved from the socket.
//
// If the attempt times out, returns 0.
//

public int readBytes(int pNumBytes)
{

    try{
        timeOutProcess = 0;
        while(timeOutProcess++ < TIMEOUT){
            if (byteIn.available() >= pNumBytes) {break;}
            waitSleep(10);
        }
        if (byteIn.available() >= pNumBytes){
            return byteIn.read(inBuffer, 0, pNumBytes);
        }
    }// try
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 595");
    }

    return 0;

}//end of Remote::readBytes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::getRemoteData
//
// Retrieves two data bytes from the remote device, using the command specified
// by pCommand.
//
// The first byte is returned.
//
// If pForceProcessDataPackets is true, the processDataPackets function will
// be called.  This is for use when that function is not already being called
// by another thread.
//
// IMPORTANT NOTE: For this function to work, the sub-class must catch
// the return packet type in its processOneDataPacket method and then read in
// the necessary data -- a simple way is to call process2BytePacket after
// catching the return packet.
// Search for GET_STATUS_CMD in UTBoard to see an example.
//

byte getRemoteData(byte pCommand, boolean pForceProcessDataPackets)
{

    if (byteIn == null) {return(0);}

    sendBytes(pCommand); //request the data from the remote

    //force waiting for and processing of receive packets
    if (pForceProcessDataPackets) {processDataPackets(true, TIMEOUT);}

    return inBuffer[0];

}//end of Remote::getRemoteData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::getRemoteAddressedData
//
// Retrieves a data byte from the remote device, using the command specified
// by pCommand and the value pData which can be used as an address or other
// specifier.
//

byte getRemoteAddressedData(byte pCommand, byte pSendData)
{

    if (byteIn == null) {return(0);}

    sendBytes(pCommand, pSendData);

    int IN_BUFFER_SIZE = 2;
    byte[] inBuf;
    inBuf = new byte[IN_BUFFER_SIZE];

    try{
        while(true){

            if (byteIn.available() >= IN_BUFFER_SIZE){

                byteIn.read(inBuf, 0, IN_BUFFER_SIZE);
                break;

            }// if
        }// while...
    }// try
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 668");
    }

    return inBuf[0];

}//end of Remote::getRemoteAddressedData
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::getRemoteDataBlock
//
// Retrieves a data block from the remote device, using the command specified
// by pCommand, a command qualifier specified by pQualifier, and the block
// size specified by pSize.  The data is returned via pBuffer.
//

void getRemoteDataBlock(byte pCommand, byte pQualifier, int pSize,
                                                                 int[] pBuffer)
{

    //debug mks - remove this line - reinsert next block

    /*

    if (byteIn == null) return;

    sendBytes4(pCommand, pQualifier,
              (byte) ((pSize >> 8) & 0xff), (byte) (pSize & 0xff));

    try{
        while(true){

            if (byteIn.available() >= pSize){

                byteIn.read(pktBuffer, 0, pSize);
                break;

                }// if
            }// while...
        }// try
    catch(IOException e){}

    //transfer the bytes to the int array - allow for sign extension
    for (int i=0; i<pSize; i++) pBuffer[i] = (int)pktBuffer[i];

    //use this line to prevent sign extension
    //for (int i=0; i<pSize; i++) pBuffer[i] = ((int)pktBuffer[i]) & 0xff;

    */

}//end of Remote::getRemoteDataBlock
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::processDataPackets
//
// The amount of time the function is to wait for a packet is specified by
// pTimeOut.  Each count of pTimeOut equals 10 ms.
//
// See processOneDataPacket notes for more info.
//

public int processDataPackets(boolean pWaitForPkt, int pTimeOut)
{

    int x = 0;

    //process packets until there is no more data available

    // if pWaitForPkt is true, only call once or an infinite loop will occur
    // because the subsequent call will still have the flag set but no data
    // will ever be coming because this same thread which is now blocked is
    // sometimes the one requesting data

    if (pWaitForPkt) {
        return processOneDataPacket(pWaitForPkt, pTimeOut);
    }
    else {
        while ((x = processOneDataPacket(pWaitForPkt, pTimeOut)) != -1){}
    }

    return x;

}//end of Remote::processDataPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::processOneDataPacket
//
// This function processes a single data packet if it is available.  If
// pWaitForPkt is true, the function will wait until data is available.
//
// This function should be overridden by sub-classes to provide specialized
// functionality.
//

public int processOneDataPacket(boolean pWaitForPkt, int pTimeOut)
{

    return(0);

}//end of Remote::processOneDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::waitSleep
//
// Sleeps for pTime milliseconds.
//

public void waitSleep(int pTime)
{

    try {Thread.sleep(pTime);} catch (InterruptedException e) { }

}//end of Remote::waitSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::logStatus
//
// Writes various status and error messages to the log window.
//

public void logStatus(Log pLogWindow)
{

}//end of Remote::logStatus
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of Remote::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of Remote::logStackTrace
//-----------------------------------------------------------------------------

}//end of class Remote
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
