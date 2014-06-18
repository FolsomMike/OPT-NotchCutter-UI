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

    byte lastPacketTypeHandled;
    byte lastPacketTypeAcked;

    int pktID;
    boolean reSynced;
    int reSyncCount = 0, reSyncPktID;
     
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
    byte[] outBufScratch;
    int outBufScrIndex;
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
// Remote::reSync
//
// Clears bytes from the socket buffer until 0xaa byte reached which signals
// the *possible* start of a new valid packet header or until the buffer is
// empty.
//
// If an 0xaa byte is found, the flag reSynced is set true to that other
// functions will know that an 0xaa byte has already been removed from the
// stream, signalling the possible start of a new packet header.
//
// There is a special case where a 0xaa is found just before the valid 0xaa
// which starts a new packet - the first 0xaa is the last byte of the previous
// packet (usually the checksum).  In this case, the next packet will be lost
// as well.  This should happen rarely.
//

public void reSync()
{

    reSynced = false;

    //track the number of times this function is called, even if a resync is not
    //successful - this will track the number of sync errors
    reSyncCount++;

    //store info pertaining to what preceded the reSync - these values will be
    //overwritten by the next reSync, so they only reflect the last error
    //NOTE: when a reSync occurs, these values are left over from the PREVIOUS good
    // packet, so they indicate what PRECEDED the sync error.

    reSyncPktID = pktID;

    try{
        while (byteIn.available() > 0) {
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] == (byte)0xaa) {reSynced = true; break;}
        }
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 847");
    }

}//end of Remote::reSync
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
// Remote::sendBytes
//
// Sends an array of bytes to the remote device, prepending a valid header and
// appending the appropriate checksum. pNumbBytes specifies the number of bytes
// in the array to send.
//
// This method accepts a primitive array rather than a variable length
// argument list as it is more efficient when an array is to be passed.
//

void sendByteArray(int pNumBytes, byte[] pBytes)
{

    int checksum = 0;

    sendHeader(); //send the packet header

    for(int i=0; i<pNumBytes; i++){
        outBuffer[i] = pBytes[i];
        checksum += pBytes[i];
    }

    //calculate checksum and put at end of buffer
    outBuffer[pNumBytes] = (byte)(0x100 - (byte)(checksum & 0xff));

    //send packet to remote
    if (byteOut != null) {
        try{
              byteOut.write(outBuffer, 0 /*offset*/, pNumBytes + 1);
              byteOut.flush();
        }
        catch (IOException e) {
            logSevere(e.getMessage() + " - Error: 422");
        }
    }

}//end of Remote::sendBytes
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Remote::sendBytes
//
// Sends a variable number of bytes (one or more) to the remote device,
// prepending a valid header and appending the appropriate checksum.
//
// NOTE: This version handles a list of bytes for arguments or an array. It is
// actually more efficient to use the sendByteArray(byte[] pBytes) version for
// arrays as Java doesn't have to create the Byte objects. Likewise, it is
//  more efficient to use the sendBytes(Byte... pBytes) version for a list of
//  values.
//
// Bytes class is used instead of primitive byte for arguments because that
// allows an array of Bytes to be passed in instead of having to always pass in
// seperate bytes. So, this method may be called in either way:
//
//      Byte byte0 = 0, byte1 = 1;
//      sendBytes((byte)1, (byte)2, byte0, byte1);
//      //note that the (byte) values are reboxed as Bytes by Java
// 
//          OR
//
//      Byte[] temp = new Byte[3];
//      temp[0] = 0; temp[1] = 1; temp[2] = 2;
//      sendBytes(temp);
//

void sendBytes(Byte... pBytes)
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
// Simulator::unpackShortInt
//
// Unpacks pShortInt into pArray as two bytes, MSB first, at position specified
// by class member variable outBufScrIndex.
//
// On exit, outBufScrIndex will point to the array position after the unpacked
// bytes.
//

void unpackShortInt(int pShortInt, byte[] pArray)
{

    pArray[outBufScrIndex++] = (byte)((pShortInt >> 8) & 0xff);
    pArray[outBufScrIndex++] = (byte)(pShortInt & 0xff);

}//end of Simulator::sendShortInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::unpackInt
//
// Unpacks pInt into pArray as four bytes, MSB first, at position specified
// by class member variable outBufScrIndex.
//
// On exit, outBufScrIndex will point to the array position after the unpacked
// bytes.
//

void unpackInt(int pInt, byte[] pArray)
{

    pArray[outBufScrIndex++] = (byte)((pInt >> 24) & 0xff);
    pArray[outBufScrIndex++] = (byte)((pInt >> 16) & 0xff);
    pArray[outBufScrIndex++] = (byte)((pInt >> 8) & 0xff);
    pArray[outBufScrIndex++] = (byte)(pInt & 0xff);

}//end of Simulator::unpackInt
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
// Remote::readBlockAndVerify
//
// Reads pNumberOfBytes from byteIn into inBuffer and also reads the checksum
// byte. The bytes (including the last one which is the checksum) are summed
// with pPktID and then compared with 0x00.
//
// The packet ID (packet command) is passed in as it will already have been
// read from the socket but is needed to compute the checksum.
//
// The value pNumberOfBytes should be equal to the number of data bytes...NOT
// including the checksum.
//
// Returns the number of bytes read if specified number of bytes were read and
// the checksum verified. Returns -1 otherwise.
//

int readBlockAndVerify(int pNumberOfBytes, byte pPktID)
{

    int totalNumBytes = pNumberOfBytes + 1; //account for the checksum
    
    int bytesRead;

    try{
        bytesRead = byteIn.read(inBuffer, 0, totalNumBytes);
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 424");
        return(-1);
    }

    if (bytesRead == totalNumBytes){

        byte sum = 0;
        for(int i = 0; i < totalNumBytes; i++) {sum += inBuffer[i];}

        //calculate checksum to check validity of the packet
        if ( (pPktID + sum & (byte)0xff) != 0) {return(-1);}
    }
    else{
        //error -- not enough bytes could be read
        return(-1);
    }

    return(bytesRead);

}//end of Remote::readBlockAndVerify
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
// Remote::processDataPackets
//
// The amount of time the function is to wait for a packet is specified by
// pTimeOut.  Each count of pTimeOut equals 10 ms.
//
// See processOneDataPacket notes for more info.
//
// Waits for a packet for at least the specified pTimeOut. For no waiting,
// pass pTimeOut to 0...if no packet is available will return immediately.
//

public int processDataPackets(int pTimeOut)
{

    int x = 0;

    //process packets until there is no more data available

    while ((x = processOneDataPacket(pTimeOut)) != -1){}

    return (x);

}//end of Remote::processDataPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::processDataPacketsUntilSpecifiedType
//
// Reads packets until packet of target type is reached or until a timeout
// occurs due to no more data available.
//
// Waits for a packet for at least the specified pTimeOut. For no waiting,
// pass pTimeOut to 0...if no packet is available will return immediately.
//

public int processDataPacketsUntilSpecifiedType(
                                         int  pTargetPacketType, int pTimeOut)
{

    int x = 0;

    //process packets until target type reached or no more data available

    while ((x = processOneDataPacket(pTimeOut)) != -1){
    
        if (lastPacketTypeHandled == pTargetPacketType){ break; }
    
    }

    return (x);

}//end of Notcher::processDataPacketsUntilSpecifiedType
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
// Waits for a packet for at least the specified pTimeOut. For no waiting,
// pass pTimeOut to 0...if no packet is available will return immediately.
//

public int processOneDataPacket(int pTimeOut)
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
