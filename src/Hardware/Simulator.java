/******************************************************************************
* Title: Simulator.java
* Author: Mike Schoonover
* Date: 5/24/09
*
* Purpose:
*
* This is the super class for various simulator classes which simulate a TCP/IP
* connection between the host and various types of hardware.
*
* This is a subclass of Socket and can be substituted for an instance
* of that class when simulated data is needed.
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
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Simulator
//

public class Simulator extends Socket implements Runnable{

public Simulator() throws SocketException{}; //default constructor - not used

    public InetAddress ipAddr;
    int port;

    boolean enabled = false;
    int type;

    byte lastPacketTypeHandled;    
    
    int index;

    int currentDataSetIndex = 1;
    
    final DecimalFormat dataSetIndexFormat = new DecimalFormat("0000000");
    
    int pktID;
    boolean reSynced;
    int reSyncCount = 0;

    public static int instanceCounter = 0;

    byte status = 0;

    //simulates the default size of a socket created for ethernet access
    // NOTE: If the pipe size is too small, the outside object can fill the
    // buffer and have to wait until the thread on this side catches up.  If
    // the outside object has a timeout, then data will be lost because it will
    // continue on without writing if the timeout occurs.
    // In the future, it would be best if UTBoard object used some flow control
    // to limit overflow in case the default socket size ends up being too
    // small.

    static int PIPE_SIZE = 8192;

    PipedOutputStream outStream;
    PipedInputStream  localInStream;

    PipedInputStream  inStream;
    PipedOutputStream localOutStream;

    DataOutputStream byteOut = null;
    DataInputStream byteIn = null;

    int IN_BUFFER_SIZE = 512;
    byte[] inBuffer;

    int OUT_BUFFER_SIZE = 512;
    byte[] outBuffer;
    byte[] outBufScratch;
    int outBufScrIndex;

//-----------------------------------------------------------------------------
// Simulator::Simulator (constructor)
//

public Simulator(InetAddress pIPAddress, int pPort) throws SocketException
{

    port = pPort; ipAddr = pIPAddress;
        
    //give each instance of the class a unique number
    //this can be used to provide a unique simulated IP address
    index = instanceCounter++;

    //create an input and output stream to simulate those attached to a real
    //Socket connected to a hardware board

    // four steams are used - two connected pairs
    // an ouptut and an input stream are created to hand to the outside object
    // (outStream & inStream) - the outside object writes to outStream and reads
    // from inStream
    // an input stream is then created using the outStream as it's connection -
    // this object reads from that input stream to receive bytes sent by the
    // external object via the attached outStream
    // an output stream is then created using the inStream as it's connection -
    // this object writes to that output stream to send bytes to be read by the
    // external object via the attached inStream

    //this end goes to the external object
    outStream = new PipedOutputStream();
    //create an input stream (localInStream) attached to outStream to read the
    //data sent by the external object
    try{localInStream = new PipedInputStream(outStream, PIPE_SIZE);}
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 112");
    }

    //this end goes to the external object
    inStream = new PipedInputStream(PIPE_SIZE);
    //create an output stream (localOutStream) attached to inStream to read the
    //data sent by the external object
    try{localOutStream = new PipedOutputStream(inStream);}
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 121");
    }

    inBuffer = new byte[IN_BUFFER_SIZE];
    outBuffer = new byte[OUT_BUFFER_SIZE];
    outBufScratch = new byte[OUT_BUFFER_SIZE];
    
    //create an output and input byte stream
    //out for this class is in for the outside classes and vice versa

    byteOut = new DataOutputStream(localOutStream);
    byteIn = new DataInputStream(localInStream);

}//end of Simulator::Simulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//

public void init(int pBoardNumber)
{

}//end of Simulator::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::reSync
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

    //track the number of time this function is called, even if a resync is not
    //successful - this will track the number of sync errors
    reSyncCount++;

    try{
        while (byteIn.available() > 0) {
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] == (byte)0xaa) {reSynced = true; break;}
            }
        }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 169");
    }

}//end of Simulator::reSync
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
            logSevere(e.getMessage() + " - Error: 295");
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
            logSevere(e.getMessage() + " - Error: 331");
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
            logSevere(e.getMessage() + " - Error: 371");
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
            logSevere(e.getMessage() + " - Error: 427");
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
// Simulator::getInputStream()
//
// Returns an input stream for the calling object - it is an input to that
// object.
//

@Override
public InputStream getInputStream()
{

    return (inStream);

}//end of Simulator::getInputStream
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::getOutputStream()
//
// Returns an output stream for the calling object - it is an output from that
// object.
//

@Override
public OutputStream getOutputStream()
{

    return (outStream);

}//end of Simulator::getOutputStream
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::getReceiveBufferSize
//
// Returns the buffer size for the pipe(s) being used to simulate a socket
// connection.  The size of the input stream defines the size for both itself
// and the attached output stream.
//

@Override
public int getReceiveBufferSize()
{

    return (PIPE_SIZE);

}//end of Simulator::getReceiveBufferSize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::getSendBufferSize
//
// Returns the buffer size for the pipe(s) being used to simulate a socket
// connection.  The size of the input stream defines the size for both itself
// and the attached output stream.
//

@Override
public int getSendBufferSize()
{

    return (PIPE_SIZE);

}//end of Simulator::getSendBufferSize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::extractSignedShort
//
// Converts the next two bytes (MSB first) in pArray to an integer, starting
// at position specified by class member variable outBufScrIndex.
//
// The original value's sign will be preserved.
//
// On exit, outBufScrIndex will point to the array position after the unpacked
// bytes.
//
// Use this if the original value was signed.
//

public int extractSignedShort(byte[] pArray)
{

    return (short)((pArray[outBufScrIndex++]<<8) & 0xff00)
                                        + (pArray[outBufScrIndex++] & 0xff);
    
}//end of NotcherHandler::extractSignedShort
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::extractUnsignedShort
//
// Converts the next two bytes (MSB first) in pArray to an integer, starting
// at position specified by class member variable outBufScrIndex.
//
// On exit, outBufScrIndex will point to the array position after the unpacked
// bytes.
//
// Use this if the original value was unsigned.
//

public int extractUnsignedShort(byte[] pArray)
{

    return (int)((pArray[outBufScrIndex++]<<8) & 0xff00)
                                    + (pArray[outBufScrIndex++] & 0xff);
    
}//end of NotcherHandler::extractUnsignedShort
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::extractInt
//
// Converts the next four bytes (MSB first) in pArray to an integer, starting
// at position specified by class member variable outBufScrIndex.
//
// The original value's sign will be preserved.
//
// On exit, outBufScrIndex will point to the array position after the unpacked
// bytes.
//
// Use this if the original value was signed.
//

public int extractInt(byte[] pArray)
{

    return((pArray[outBufScrIndex++]<<24) & 0xff000000) +
          ((pArray[outBufScrIndex++]<<16) & 0xff0000) +
          ((pArray[outBufScrIndex++]<<8) & 0xff00) +
          (pArray[outBufScrIndex++] & 0xff);

}//end of NotcherHandler::extractInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::run
//
// This is the thread run code. Should be overridden by child classes and
// used to drive the simulation.
//

@Override
public void run() {

}//end of Simulator::run
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

final void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of Simulator::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Simulator::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of Simulator::logStackTrace
//-----------------------------------------------------------------------------

}//end of class Simulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
