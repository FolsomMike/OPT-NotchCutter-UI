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

    byte testSetByte;   //used for example -- DO NOT DELETE
    int testSetInt;     //used for example -- DO NOT DELETE
    
    byte electrodeSupplyOnOffByte;
    
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
// Notcher::processOneDataPacket
//
// This function processes a single data packet if it is available.
//
// The amount of time the function is to wait for a packet is specified by
// pTimeOut.  Each count of pTimeOut equals 10 ms.
//
// Waits for a packet for at least the specified pTimeOut. For no waiting,
// pass pTimeOut to 0...if no packet is available will return immediately.
//
// This function should be called often to allow processing of data packets
// received from the remotes and stored in the socket buffer.
//
// All packets received from the remote devices should begin with
// 0xaa, 0x55, 0xbb, 0x66, followed by the packet identifier (usually the
// command used by the host to request the packet).
//
// Returns number of bytes retrieved from the socket, not including the
// 4 header bytes, the packet ID.
//
// Thus, if a non-zero value is returned, a packet was processed.  If zero
// is returned, some bytes may have been read but a packet was not successfully
// processed due to missing bytes or header corruption.
//
// A return value of -1 means that the buffer does not contain a packet and a
// timeout has occurred.
//

@Override
public int processOneDataPacket(int pTimeOut)
{

    if (byteIn == null) {return -1;}  //do nothing if the port is closed

    try{

        int timeOutWFP = 0;
        while(byteIn.available() < 5 && timeOutWFP++ < pTimeOut){
            waitSleep(10);
        }

        //wait until 5 bytes are available - this should be the 4 header bytes,
        //and the packet identifier
        if (byteIn.available() < 5) {return -1;}

        //read the bytes in one at a time so that if an invalid byte is
        //encountered it won't corrupt the next valid sequence in the case
        //where it occurs within 3 bytes of the invalid byte

        //check each byte to see if the first four create a valid header
        //if not, jump to resync which deletes bytes until a valid first header
        //byte is reached

        //if the reSynced flag is true, the buffer has been resynced and an 0xaa
        //byte has already been read from the buffer so it shouldn't be read
        //again

        //after a resync, the function exits without processing any packets

        if (!reSynced){
            //look for the 0xaa byte unless buffer just resynced
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] != (byte)0xaa) {reSync(); return 0;}
        }
        else {reSynced = false;}

        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x55) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0xbb) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x66) {reSync(); return 0;}

        //read in the packet identifier
        byteIn.read(inBuffer, 0, 1);

        lastPacketTypeHandled = inBuffer[0];
        
        //store the ID of the packet (the packet type)
        pktID = inBuffer[0];

        if (pktID == Notcher.GET_RUN_PACKET_CMD) {return(0); /*return readBytes(2);*/}
        else
        if (pktID == Notcher.CUT_MODE_CMD){return(0); /*return readBytes(2);*/}
        else 
        if (pktID == Notcher.TEST_SET_VALUE_CMD){
            return (handleTestSetValuePacket());
        }
        else 
        if (pktID == Notcher.ELECTRODE_SUPPLY_ON_OFF_CMD){
            return (handleElectrodeSupplyOnOffCmdPacket());
        }
        
        
        // add more commands here -- do not remove this comment

    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 799");
    }

    return 0;

}//end of Notcher::processOneDataPacket
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
// NotcherSimulator::sendACKPacket
//
// Sends a packet with the command type set to ACK_CMD. The packet type of
// the packet type just read from the socket for which this packet is a response
// is returned as a data byte so the host can match ACK packets to their
// associated command packets.
//

private void sendACKPacket()
{
    
    //the values are unpacked into bytes and stored in outBufScratch
    //outBufScrIndex is used to load the array, start at position 0
    
    outBufScrIndex = 0;
    
    outBufScratch[outBufScrIndex++] = Notcher.ACK_CMD;
    
    //the byte is placed right here in this method
    outBufScratch[outBufScrIndex++] = lastPacketTypeHandled;
        
    //send header, the data, and checksum
    sendByteArray(outBufScrIndex, outBufScratch);
    
}//end of NotcherSimulator::sendACKPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::handleTestSetValuePacket
//
// Handles ACK packets received from the remote.
//
// Returns the number of bytes read from the socket.
//
// If the the bytes in the packet could not be read or were not validated by
// the checksum, return -1.
//

private int handleTestSetValuePacket()
{
    
    int dataSize = 5; //one byte plus one integer (4 bytes)
    
    //read remainder of packet from socket and verify against the checksum
    int lStatus = readBlockAndVerify(dataSize, Notcher.TEST_SET_VALUE_CMD);

    //on error reading and verifying, return the error code
    if (lStatus == -1){ return(status); }
  
    //only store the values if there was no error -- errors cause return above

    outBufScrIndex = 0; //start with byte 0 in array
    
    testSetByte = inBuffer[outBufScrIndex];
    
    testSetInt = extractInt(inBuffer);
    
    sendACKPacket();
    
    return(lStatus);
    
}//end of NotcherSimulator::handleTestSetValuePacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::handleElectrodeSupplyOnOffCmdPacket
//
// Handles ACK packets received from the remote for the electrodeSupplyOnOffCmd.
//
// Returns the number of bytes read from the socket.
//
// If the the bytes in the packet could not be read or were not validated by
// the checksum, return -1.
//

private int handleElectrodeSupplyOnOffCmdPacket()
{
    
    int dataSize = 1; //one byte
    
    //read remainder of packet from socket and verify against the checksum
    int lStatus = readBlockAndVerify(dataSize, 
                                        Notcher.ELECTRODE_SUPPLY_ON_OFF_CMD);

    //on error reading and verifying, return the error code
    if (lStatus == -1){ return(status); }
  
    //only store the values if there was no error -- errors cause return above

    outBufScrIndex = 0; //start with byte 0 in array
    
    electrodeSupplyOnOffByte = inBuffer[outBufScrIndex];
    
    sendACKPacket();
    
    return(lStatus);
    
}//end of NotcherSimulator::handleElectrodeSupplyOnOffCmdPacket
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
// NotcherSimulator::driveSimulation
//
// This watches for packets from the host and returns data.
//

private void driveSimulation() {

    //process all data packets currently in the socket from the host
    //set timeout to 0 -- no need to wait on packets
    processDataPackets(0);
    
}//end of NotcherSimulator::driveSimulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherSimulator::run
//
// This is the thread run code and is used to drive the simulation.
//

@Override
public void run() {

    while(true){
        
        driveSimulation();

        waitSleep(5);
    }
    
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
