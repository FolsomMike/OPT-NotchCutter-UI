/******************************************************************************
* Title: Notcher.java
* Author: Mike Schoonover, Hunter Schoonover
* Date: 6/12/14
*
* Purpose:
*
* This class interfaces with a NotchCutter unit via Ethernet.
*
* Packet Format
* 
* All packets sent and received are of format:
* 
* 0xaa
* 0x55
* 0xbb
* 0x66
* command byte
* data byte 0
* data byte 1
* data byte 2
* ...
* checksum (of command and data bytes)
*
* Communication Flow 
*
* The data to and from the remotes is piped through sockets. The sockets have
* buffering and will store multiple data packets before being read. They must
* be read fast enough to prevent buffer overflow.
* 
* When using synchronous communication, the host sends a command packet and
* then waits a specified time for the return packet. If the timeout occurs
* before the expected packet arrives, the host can either ignore the failure
* or resend the packet depending on the criticality of the return packet.
* 
* One problem that occurs is that the remote might return the packet after the
* the timeout but the host has already moved on to other processing. The
* late packet will be stuck in the buffer. Therefore, for synchronous
* processing, the processDataPacketsUntilSpecifiedType method is called after
* sending a command packet as it will clear out any leftover packets in the
* buffer to reach the expected return packet.
* 
* The packets are processed by different methods specific to each packet type.
* When leftover packets are being processed, their data might be stored in
* persistent variables for later collection by the host. When the
* processDataPacketsUntilSpecifiedType method is called, the host will not get
* a chance to collect that data until later as the method does not return
* until all packets have been processed or the target packet type has been
* reached.
* 
* If multiple packets of the same type are received before the host can
* collect the data, data from earlier packets will be overwritten by later
* packets. If the data is merely for informational display, it often does not
* matter if some data is skipped.
* 
* In other cases, the handling method can store peak data such as the max
* and/or min. Thus, the important nature of the data is not lost as it is
* typical that displaying or detecting the max or min is adequate. One case is
* where the system should trigger an alarm if the value goes above or below a
* certain threshold -- the min/max values stored can be used later to check
* for the violation.
* 
* In cases where every single data point is important, the handling method can
* store the variables in an array or Collection object and the host can process
* all the values collected when it has time.
* 
* It is up to the programmer to decide which data can be skipped, which must
* have max/mins recorded, and which must be buffered so that none is lost.
* 
*  Synchronous Method
* 
* Using the synchronous method, the host sends a command packet and then waits
* a specified time for the return packet by calling 
* 
*   processDataPacketsUntilSpecifiedType
* 
* As noted above, this will force all packets in the buffer to be processed
* until the target packet type is reached.
* 
* If the remote does not respond in time, then the calling method may ignore the
* failure or may attempt to resend the command, depending on the criticality
* of success.
* 
* If constantly updating a display repeatedly from values received from the
* remote, resending may not be necessary as the update will be refreshed
* properly on the next cycle -- the old values will simply be displayed for
* one extra cycle through the loop.
* 
* On the other hand, sending a control value to the remote may be very critical
* as the value may affect operation. Thus, if the remote does not return with
* an ACK packet, it may be necessary to resend the control value. Due to the
* robust error correcting nature of TCP/IP, it may be safe to ignore the ACK
* packet anyway and just assume that communication was successful. Note that
* the ACK packet must still be waited for so that it will be cleaned from the
* buffer.
* 
*  Asynchronous Method
* 
* Using the asynchronous method, the host sends the command packet but does
* not call processOneDataPacket immediately to wait for the return packet.
* Instead, the program goes on about other business.
* 
* The processDataPackets method is called at some later time, usually by a
* thread or timer. All waiting return packets are processed at one time and
* their values stored and the appropriate "data ready" flags set so that the
* program can detect that new values are ready for use. At some point, the
* program uses the new data values for display, saving, or control. At that
* time, the "data ready" flags are cleared so the program knows not to use the
* data again until new data has been received and the flags are set again.
* 
* Which Method to Use
* 
* In general, the synchronous method is easiest to use but may not be the most
* efficient as the host must wait for the return packet each time when it
* could be performing other tasks.
* 
* Both methods may be used in the same program. Setting values in the remote
* might work best synchronously as there is usually no time issue when simply
* sending setup values. During time critical operations, the data packets
* can be requested and processed asynchronously so the host can perform other
* tasks between the request and the receive.
* 
* Adding New Remote Commands
* 
* Two different sample code sets have been included in this and the
* NotcherSimulator class to serve as examples for adding commands:
* 
*   TEST_SET_VALUE_CMD
*   TEST_PACKET_CMD
* 
* Search this class (Notcher) and NotcherSimulator for "TEST_SET_VALUE_CMD"
* to find all code related to this command. Like wise, search for
* "TEST_PACKET_CMD" for that code set.
* 
* To add a new command, make a new command send method and packet handling
* method similar to those for the appropriate example code set. Then add
* the command switch statement to processOneDataPacket to call the handler
* when the packet type is received. (Search for the phrase 
* "add more commands here" to find location to add the switch statement.)
* 
* NOTE: The process of adding a command must be repeated in NotcherSimulator,
* but with the opposite logic. The message handler handles command packets and
* returns response packets. Searching for the "TEST_SET_VALUE_CMD" and
* "TEST_PACKET_CMD" phrases in that class will also reveal the necessary code
* elements required.
*
*   TEST_SET_VALUE_CMD Code Set
* 
* The TEST_SET_VALUE_CMD is an example of an synchronous communication in
* which a value(s) are sent to the remote.
* 
* See notes at the top of sendTestSetValueCmd and handleACKPkt in this class
* for more explanation. Also see notes at the top of handleTestSetValuePkt in
* class NotcherSimulator.
* 
* Note that many similar commands may return ACK packets -- the return packet's
* command value will be set to ACK_CMD and the first data byte will be set to
* the command id of the original packet sent by the host and which is being
* acknowledged by the ACK packet. Thus the host can match the ACK packet with
* the packet which is being acknowledged.
* 
* Before calling sendTestSetValueCmd, set breakpoints at the top of 
* Notcher.processOneDataPacket and NotcherSimulator.processDataPacketsHelper to
* track the program flow. The second method (in NotcherSimulator class) will
* be called first as the simulator receives the command packet. The first
* method (Notcher.processOneDataPacket) should be called by some thread or timer
* on a regular basis so that it can handle the return packet when it arrives.
*
* Multiple Threads
* 
* If only a timer event is used in the program, then thread conflicts will not
* be a problem. If another thread is created to execute the packet handling
* code (such as by calling processDataPacketsUntilSpecifiedType), then the
* synchronize keyword must be added to some of the methods to prevent
* thread interference.
* 
* Since the new thread is collecting data and storing it in class member
* variables when the handling code is called and the Event Dispatch Thread (EDT)
* is calling getter methods for those same variables, the methods must be
* tagged with the synchronize keyword.
* 
* For simplest code, synchronize can be applied to the 
*   processDataPacketsUntilSpecifiedType method and all the getters used to
* access the data variables can be declared as synchronized.
* 
* For slightly more efficient operation, each packet handler method which
* saves data from the packet into the member variables can be declared
* synchronized. This allows the EDT to use a getter when the other thread is in
* the processDataPacketsUntilSpecifiedType method. Only when the other thread
* is actually in a packet handler method will the getters be blocked.
*
* 
* Overview of Adding and Testing New Commands
* 
*    Adding a Command to Send Values to the Remote
* 
*       1) search Notcher and Notcher Simulator for the phrase "TEST_PACKET_CMD"
*           to find all code sections applicable
* 
*       2) copy and modify all applicable code sections for the new command;
*           in these instructions, your new method which mimics
*           Notcher.sendTestSetValueCmd is referred to as send***Cmd while your
*           new method which mimics NotcherSimulator.handleTestSetValuePacket is
*           referred to as handle***Packet; the actual name you use should
*           describe the packet being sent, such as "sendResetCmd"
* 
*       3) somewhere in your program, add three consecutive calls to your new
*           Notcher.send***Cmd method (the calls can be made any time after
*           Notcher.connect has completed -- in your button event code, in a
*           timer, etc.); the three calls will check to make sure the handling
*           code is cleaning the packets from the socket properly else the
*           breakpoints in the resync methods will be triggered -- REMOVE THE
*           EXTRA TWO CALLS AFTER DEBUGGING COMPLETE -- the multiple calls are
*           used only to make sure the packets are being cleaned up, if they
*           are then you will not get halts at the resync breakpoints caused
*           by leftover packet fragments
* 
*       4) place breakpoints at the top of both resync methods in the Remote
*           and Simulator classes
* 
*       5) place breakpoints in Notcher.send***Cmd, 
*           NotcherSimulator.handle***Packet and Notcher.handleACKPacket
* 
*       6) run the program
* 
*       7) the program should halt at the Notcher.send***Cmd breakpoint first;
*           the program will stop multiple times at that point, once for each
*           packet sent; step through the new method and verify its operation
*
*       8) a short time later, the program should halt at the breakpoint in
*           NotcherSimulator.handle***Packet, once for each packet sent; step
*           through the new method and verify its operation
*
*       9) the program will at some point halt at the breakpoint in
*           Notcher.handleACKPacket as Notcher object receive ACK packets from
*           the NotcherSimulator objects
* 
*       10) the breakpoints in the resync methods should NEVER halt -- if so
*           then an error has occurred with the last packet; this is usually
*           caused by reading too many or few bytes from the socket and thus
*           corrupting the data order; a resync occurs when the header of a new
*           packet is expected and is not found
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
import model.IniFile;
import view.ThreadSafeLogger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Notcher
//
// This class creates and handles an interface to a Notcher Unit board.
//

public class Notcher extends Remote{

    int index;
    
    byte[] monitorBuffer;
    byte[] allEncoderValuesBuf;

    boolean udpResponseFlag = false;
    
    int packetRequestTimer = 0;

    int runtimePacketSize;

    //misc constants
    public static final byte OFF = 0;
    public static final byte ON = 1;
    
    //Commands for Notcher units
    //These should match the values in the code for those boards.

    static final byte NO_ACTION = 0;
    static final byte STOP_MODE_CMD = 1;
    static final byte CUT_MODE_CMD = 2;
    static final byte ZERO_DEPTH_CMD = 3;
    static final byte ZERO_TARGET_DEPTH_CMD = 4;
    static final byte GET_RUN_PACKET_CMD = 5;
    static final byte ELECTRODE_SUPPLY_ON_OFF_CMD = 6;

    // add more commands here -- do not remove this comment
    
    static final byte ACK_CMD = 122;
    static final byte TEST_SET_VALUE_CMD = 123;
    static final byte TEST_PACKET_CMD = 124;
    static final byte ERROR = 125;
    static final byte DEBUG_CMD = 126;
    static final byte EXIT_CMD = 127;
    
    //Status Codes for Notcher units
    //These should match the values in the code for those boards.

    static byte NO_STATUS = 0;

    static int MONITOR_PACKET_SIZE = 25;
    static int ALL_ENCODERS_PACKET_SIZE = 24;    
    static int RUNTIME_PACKET_SIZE = 2048;

//-----------------------------------------------------------------------------
// Notcher::Notcher (constructor)
//

public Notcher(int pIndex, int pRuntimePacketSize, boolean pSimulate,
                                                       ThreadSafeLogger pTSLog)
{

    super(pTSLog);

    index = pIndex;
    runtimePacketSize = pRuntimePacketSize;
    simulate = pSimulate;

}//end of Notcher::Notcher (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::init
//
// Initializes new objects. Should be called immediately after instantiation.
//

public void init()
{

    monitorBuffer = new byte[MONITOR_PACKET_SIZE];
    
    allEncoderValuesBuf = new byte[ALL_ENCODERS_PACKET_SIZE];
    
    //read the configuration file and create/setup the charting/control elements
    configure(configFile);

}//end of Notcher::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::configure
//
// Loads configuration settings from pConfigFile.
// The various child objects are then created as specified by the config data.
//

@Override
void configure(IniFile pConfigFile)
{

    super.configure(pConfigFile);
        
    inBuffer = new byte[RUNTIME_PACKET_SIZE];
    outBuffer = new byte[RUNTIME_PACKET_SIZE];
    outBufScratch = new byte[RUNTIME_PACKET_SIZE];

}//end of Notcher::configure
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::connect
//
// Opens a TCP/IP connection with the Notcher unit.
//

public void connect()
{

    if (ipAddrS == null || ipAddr == null){
        tsLog.appendLine(
                "Notcher Unit #" + index + " never responded to "
                + "roll call and cannot be contacted.");
        return;
    }

    tsLog.appendLine("Opening connection with Notcher...");

    try {

        tsLog.appendLine("Notcher Unit IP Address: " + ipAddr.toString());

        if (!simulate) {
            socket = new Socket(ipAddr, 23);
        }
        else {

            NotcherSimulator notcherSimulator = 
                                            new NotcherSimulator( ipAddr, 23);
            notcherSimulator.init();
            
            socket = notcherSimulator;
            
        }

        //set amount of time in milliseconds that a read from the socket will
        //wait for data - this prevents program lock up when no data is ready
        socket.setSoTimeout(250);

        out = new PrintWriter(socket.getOutputStream(), true);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        byteOut = new DataOutputStream(socket.getOutputStream());
        byteIn =  new DataInputStream(socket.getInputStream());

    }//try
    catch (IOException e) {
        logSevere(e.getMessage() + " - Error: 238");
        tsLog.appendLine("Couldn't get I/O for " + ipAddrS);
        return;
    }

    try {
        //display the greeting message sent by the remote
        tsLog.appendLine(ipAddrS + " says " + in.readLine());
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 248");
    }

    //flag that board setup has been completed - whether it failed or not
    setupComplete = true;

    //flag that setup was successful and board is ready for use
    ready = true;


    tsLog.appendLine("Notcher " + ipAddrS + " is ready.");

}//end of Notcher::connect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher:initialize
//
// Sets up various settings on the board.
//

public void initialize()
{

}//end of Notcher::initialize
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

    if (byteIn == null) {
        return -1;
    }  //do nothing if the port is closed

    try{

        int timeOutWFP = 0;
        while(byteIn.available() < 5 && timeOutWFP++ < pTimeOut){
            waitSleep(10);
        }

        //wait until 5 bytes are available - this should be the 4 header bytes,
        //and the packet identifier
        if (byteIn.available() < 5) { return -1; }

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

        if (!reSynced) {
            //look for the 0xaa byte unless buffer just resynced
            byteIn.read(inBuffer, 0, 1);
            if (inBuffer[0] != (byte)0xaa) {reSync(); return 0;}
        }
        else { reSynced = false; }

        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x55) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0xbb) {reSync(); return 0;}
        byteIn.read(inBuffer, 0, 1);
        if (inBuffer[0] != (byte)0x66) {reSync(); return 0;}

        //read in the packet identifier
        byteIn.read(inBuffer, 0, 1);

        //store the last packet type handled
        lastPacketTypeHandled = inBuffer[0];
        
        //store the ID of the packet (the packet type)
        pktID = inBuffer[0];

        if (pktID == ACK_CMD) {return handleACKPacket();}
        else
        if (pktID == GET_RUN_PACKET_CMD) {return readBytes(2);}  //wip hss -- call function to handle this instead of the return
        else
        if (pktID == CUT_MODE_CMD){return readBytes(2);} //wip hss -- call function to handle this instead of the return

        // add more commands here -- do not remove this comment

    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 799");
    }

    return 0;

}//end of Notcher::processOneDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::various get/set functions
//


//end of Notcher::various get/set functions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::getDataPacket
//
// This method sends a request to the remote unit for a data packet, waits
// for it, and then returns a reference to the data array.
//

public byte[] getDataPacket()
{

    return(null);
    
}//end of Notcher::getDataPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::invokeCutMode
//
// Sends "Cut" command to the remote.
//

public void invokeCutMode()
{
    
    sendBytes(CUT_MODE_CMD, (byte) 0);
    
}//end of Notcher::invokeCutMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::invokeStopMode
//
// Sends "Stop" command to the remote.
//

public void invokeStopMode()
{

    sendBytes(STOP_MODE_CMD, (byte) 0);

}//end of Notcher::invokeStopMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::zeroDepthCount
//
// Sends "Zero Depth Count" command to the remote.
//

public void zeroDepthCount()
{

    sendBytes(ZERO_DEPTH_CMD, (byte) 0);
    
}//end of Notcher::zeroDepthCount
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::zeroTargetDepth
//
// Sends "Zero Target Depth " command to the remote.
//

public void zeroTargetDepth()
{

    sendBytes(ZERO_TARGET_DEPTH_CMD, (byte) 0);
    
}//end of Notcher::zeroDepthCount
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::handleACKPacket
//
// Handles ACK packets received from the remote.
//
// Returns the number of bytes read from the socket.
//

public int handleACKPacket()
{

    int status = readBlockAndVerify(1, ACK_CMD);

    //on error reading and verifying, return the error code
    if (status == -1){ return(status); }
    
    //store the packet type to which this ACK is responding
    lastPacketTypeAcked = inBuffer[0];
    
    return(status);
        
}//end of Notcher::handleACKPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::sendTestSetValueCmd
//
// Example code set for TEST_SET_VALUE_CMD command.
//      DO NOT DELETE THIS METHOD
//
// Search this class and NotcherSimulator class for "TEST_SET_VALUE_CMD" for
// all related code and info.
//
// Sends a byte and an integer to the remote waits for the response packet.
//
// Returns true on success, false on failure.
//

public boolean sendTestSetValueCmd(byte pByte, int pIntValue)
{
    
    //the values are unpacked into bytes and stored in outBufScratch
    //outBufScrIndex is used to load the array, start at position 0
    
    outBufScrIndex = 0;
    
    outBufScratch[outBufScrIndex++] = TEST_SET_VALUE_CMD;
    
    //the byte is placed right here in this method
    outBufScratch[outBufScrIndex++] = pByte;
    
    //use method to unpack the integer into the scratch buffer
    unpackInt(pIntValue, outBufScratch);
    
    //send header, the data, and checksum
    sendByteArray(outBufScrIndex, outBufScratch);

    //reset so we can check ACK to see if it was for this packet
    lastPacketTypeAcked = NO_ACTION;
    
    //process all packets until ACK packet found, waiting up to 1 sec
    processDataPacketsUntilSpecifiedType(ACK_CMD, 100);
    
    if (lastPacketTypeAcked != TEST_SET_VALUE_CMD){
     
        //ACK packet for this command not received handle error here
        //calling function can loop until this method returns true or the
        //error can be ignored
  
        return(false);
        
    }
    
    return(true);
    
}//end of Notcher::sendTestSetValueCmd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::sendElectrodePowerOnOffCmd
//
// Sends an electorde power ON or OFF command to the notcher.
//
// Sends a byte to the remote waits for the response packet.
//
// Returns true on success, false on failure.
//

public boolean sendElectrodePowerOnOffCmd(byte pState)
{
    
    //outBufScrIndex is used to load the array, start at position 0
    outBufScrIndex = 0;
    
    outBufScratch[outBufScrIndex++] = ELECTRODE_SUPPLY_ON_OFF_CMD ;
    
    //the byte is placed right here in this method
    outBufScratch[outBufScrIndex++] = pState;
    
    //send header, the data, and checksum
    sendByteArray(outBufScrIndex, outBufScratch);

    //reset so we can check ACK to see if it was for this packet
    lastPacketTypeAcked = ELECTRODE_SUPPLY_ON_OFF_CMD;
    
    //process all packets until ACK packet found, waiting up to 1 sec
    processDataPacketsUntilSpecifiedType(ACK_CMD, 100);
    
    if (lastPacketTypeAcked != ELECTRODE_SUPPLY_ON_OFF_CMD){
     
        //ACK packet for this command not received handle error here
        //calling function can loop until this method returns true or the
        //error can be ignored
  
        return(false);
        
    }
    
    return(true);
    
}//end of Notcher::sendElectrodePowerOnOffCmd
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::loadCalFile
//
// This loads the file used for storing calibration information such as cut
// depth, cut speed, cut aggression, etc.
//
// Each child object is passed a pointer to the file so that they may load their
// own data.
//

public void loadCalFile(model.IniFile pCalFile)
{

}//end of Notcher::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::saveCalFile
//
// This saves the file used for storing calibration information such as cut
// depth, cut speed, cut aggression, etc.
//
// Each child object is passed a pointer to the file so that they may load their
// own data.
//

public void saveCalFile(model.IniFile pCalFile)
{

}//end of Notcher::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Notcher::shutDown
//
// This function should be called before exiting the program.  Overriding the
// "finalize" method does not work as it does not get called reliably upon
// program exit.
//

protected void shutDown()
{

    //close everything - the order of closing may be important

    try{

        if (byteOut != null) {byteOut.close();}
        if (byteIn != null) {byteIn.close();}
        if (out != null) {out.close();}
        if (in != null) {in.close();}
        if (socket != null) {socket.close();}

    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 1009");
    }

}//end of Notcher::shutDown
//-----------------------------------------------------------------------------

}//end of class Notcher
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
