/******************************************************************************
* Title: NotcherSimulator.java
* Author: Mike Schoonover, Hunter Schoonover
* Date: 6/12/14
*
* Purpose:
*
* This class simulates a TCP/IP connection between the host and Notcher units.
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

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class NotcherSimulator
//
// This class simulates data from a TCP/IP connection between the host computer
// and Notcher units.
//

public class NotcherSimulator extends Simulator{

    //default constructor - not used
    public NotcherSimulator() throws SocketException{};

    //simulates the default size of a socket created for ethernet access
    // NOTE: If the pipe size is too small, the outside object can fill the
    // buffer and have to wait until the thread on this side catches up.  If the
    // outside object has a timeout, then data will be lost because it will
    // continue on without writing if the timeout occurs.
    // In the future, it would be best if Notcher object used some flow
    // control to limit overflow in case the default socket size ends up being
    // too small.

    public static int notcherCounter = 0;
    int notcherUnitNumber;

    byte controlFlags = 0, portE = 0;
    
//-----------------------------------------------------------------------------
// NotcherSimulator::NotcherSimulator (constructor)
//

public NotcherSimulator(InetAddress pIPAddress, int pPort)
                                                        throws SocketException
{

    //call the parent class constructor
    super(pIPAddress, pPort);

    //create an out writer from this class - will be input for some other class
    //this writer is only used to send the greeting back to the host

    PrintWriter out = new PrintWriter(localOutStream, true);
    out.println("Hello from Notcher Simulator!");

}//end of NotcherSimulator::NotcherSimulator (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//

public void init()
{
    
    //give each board a unique number so it can load data from the
    //simulation files and such
    
    notcherUnitNumber = notcherCounter++;

    super.init(notcherUnitNumber);

    //start the simulation thread
    new Thread(this).start();
    
}//end of NotcherSimulator::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::processDataPackets
//
// See processDataPacketsHelper notes for more info.
//

public int processDataPackets(boolean pWaitForPkt)
{

    int x = 0;

    //process packets until there is no more data available

    // if pWaitForPkt is true, only call once or an infinite loop will occur
    // because the subsequent call will still have the flag set but no data
    // will ever be coming because this same thread which is now blocked is
    // sometimes the one requesting data

    if (pWaitForPkt) {
        return processDataPacketsHelper(pWaitForPkt);
    }
    else {
        while ((x = processDataPacketsHelper(pWaitForPkt)) != -1){}
    }

    return x;

}//end of NotcherSimulator::processDataPackets
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::processDataPacketsHelper
//
// Drive the simulation functions.  This function is usually called from a
// thread.
//

public int processDataPacketsHelper(boolean pWaitForPkt)
{

    if (byteIn == null) {return 0;}  //do nothing if the port is closed

    try{

        int x;

        //wait until 5 bytes are available - this should be the 4 header bytes,
        //and the packet identifier/command
        if ((x = byteIn.available()) < 5) {return -1;}

        //read the bytes in one at a time so that if an invalid byte is
        //encountered it won't corrupt the next valid sequence in the case
        //where it occurs within 3 bytes of the invalid byte

        //check each byte to see if the first four create a valid header
        //if not, jump to resync which deletes bytes until a valid first header
        //byte is reached

        //if the reSynced flag is true, the buffer has been resynced and an 0xaa
        //byte has already been read from buffer so it shouldn't be read again

        //after a resync, the function exits without processing any packets

        if (!reSynced){
            //look for the 0xaa byte unless buffer just resynced
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] != (byte)0xaa) {reSync(); return 0;}
        }
        else {
            reSynced = false;
        }

        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x55) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0xbb) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x66) {reSync(); return 0;}

        //read the packet ID
        byteIn.read(inBuffer, 0, 1);

        byte pktID = inBuffer[0];

        if (pktID == Notcher.GET_DATA_PACKET_CMD)
            { return sendDataPacket();}
        else
        if (pktID == Notcher.CUT_MODE_CMD){return invokeCutMode();}

        return 0;

    }//try
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 221");
    }

    return 0;

}//end of NotcherSimulator::processDataPacketsHelper
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::sendDataPacket
//
// Sends a data packet to the host.
//

public int sendDataPacket()
{
    
    return(0);

}//end of NotcherSimulator::sendDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::invokeCutMode
//
// Sets mode to "Cut".
//

public int invokeCutMode()
{
    
    return(0);

}//end of NotcherSimulator::invokeCutMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::readBlockAndVerify
//
// Reads pNumberOfBytes from byteIn into inBuffer. The bytes (including the last
// one which is the checksum) are summed with pPktID and then compared with
// 0x00.
//
// The value pNumberOfBytes should be equal to the number of data bytes
// remaining in the packet plus one for the checksum.
//
// Returns the number of bytes read if specified number of bytes were read and
// the checksum verified. Returns -1 otherwise.
//

int readBlockAndVerify(int pNumberOfBytes, byte pPktID)
{

    int bytesRead;

    try{
        bytesRead = byteIn.read(inBuffer, 0, pNumberOfBytes);
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 299");
        return(-1);
    }

    if (bytesRead == pNumberOfBytes){

        byte sum = 0;
        for(int i = 0; i < pNumberOfBytes; i++) {sum += inBuffer[i];}

        //calculate checksum to check validity of the packet
        if ( (pPktID + sum & (byte)0xff) != 0) {return(-1);}
    }
    else{
        //error -- not enough bytes could be read
        return(-1);
    }

    return(bytesRead);

}//end of NotcherSimulator::readBlockAndVerify
//-----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// NotcherSimulator::sendPacketHeader
//
// Sends via the socket: 0xaa, 0x55, 0xaa, 0x55, packet identifier.
//
// Does not flush.
//

void sendPacketHeader(byte pPacketID)
{

    outBuffer[0] = (byte)0xaa; outBuffer[1] = (byte)0x55;
    outBuffer[2] = (byte)0xbb; outBuffer[3] = (byte)0x66;
    outBuffer[4] = (byte)pPacketID;

    //send packet to remote
    if (byteOut != null) {
        try{
            byteOut.write(outBuffer, 0 /*offset*/, 5);
        }
        catch (IOException e) {
            logSevere(e.getMessage() + " - Error: 573");
        }
    }

}//end of NotcherSimulator::sendPacketHeader
//----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::driveSimulation
//
// This watches for packets from the host and returns data.
//

private void driveSimulation() {
    
    
}//end of NotcherSimulator::driveSimulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::NotcherSimulator
//
// This is the thread run code and is used to drive the simulation.
//

@Override
public void run() {
    
    driveSimulation();
    
    waitSleep(300);
    
}//end of NotcherSimulator::run
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::waitSleep
//
// Sleeps for pTime milliseconds.
//

public void waitSleep(int pTime)
{

    try {Thread.sleep(pTime);} catch (InterruptedException e) { }

}//end of NotcherSimulator::waitSleep
//-----------------------------------------------------------------------------


}//end of class NotcherSimulator
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
