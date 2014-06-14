/******************************************************************************
* Title: NotcherHandler.java
* Author: Mike Schoonover, Hunter Schoonover
* Date: 4/23/09
*
* Purpose:
*
* This class handles the Notcher communications.
* 
* It opens a UDP socket and broadcasts a greeting to all devices on the
* network. The devices which answer are added to a list.
* 
* An Ethernet socket is then opened to establish a link to all devices in the
* list.
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
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.IniFile;
import view.Log;
import view.ThreadSafeLogger;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class NotcherHandler
//
// This class creates and handles the hardware interface.
//

public class NotcherHandler extends Object{

    boolean controlBoardsReady = false;

    boolean simulateNotchers;

    private static final int MAX_NUM_NOTCHERS = 10;
    
    private int numberOfNotchers = 0;
    public int getNumberOfNotchers() {return (numberOfNotchers); }
    
    Notcher[] notchers;

    boolean logEnabled = true;

    ThreadSafeLogger tsLog;

    static int RUNTIME_PACKET_SIZE = 50;

    byte[] pktBuffer;
    
    int opMode = STOPPED_MODE;
        
    static final int STOPPED_MODE = 0;
    static final int CUT_MODE = 1;
    
//-----------------------------------------------------------------------------
// NotcherHandler::NotcherHandler (constructor)
//
// The parameter configFile is used to load configuration data.  The IniFile
// should already be opened and ready to access.
//

public NotcherHandler(ThreadSafeLogger pTSLog)

{

    tsLog = pTSLog;

}//end of NotcherHandler::NotcherHandler (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::init
//
// Initializes the object.  MUST be called by sub classes after instantiation.
//

public void init()
{

    pktBuffer = new byte[RUNTIME_PACKET_SIZE];

    configure(); //load settings from config file
    
}//end of NotcherHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::configure
//
// Loads configuration settings from the ini file.

// The various child objects are then created as specified by the config data.
//

private void configure()
{

    IniFile configFile = new IniFile("General Settings.ini", "UTF-8");
    try {
        configFile.init();
    } catch(IOException e) {
        return;
    }
    
    simulateNotchers =
       configFile.readBoolean("Hardware", "Simulate Notchers", false);

    //create and setup the Control boards
    configureNotchers();

}//end of NotcherHandler::configure
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::configureNotchers
//
// Loads configuration settings from the configuration.ini file relating to
// the Notchers and creates/sets them up.
//

private void configureNotchers()
{

    notchers = new Notcher[MAX_NUM_NOTCHERS];
    
}//end of NotcherHandler::configureNotchers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::connect
//
// Establishes a connection with each board.
//
// Notes about the IP Address and Subnet Mask
// When a Windows computer is connected to the local network with only the
// Rabbit modules, it will assign itself an IP Address such as 169.254.56.136
// and a Mask Subnet of 255.255.0.0 because there is no DHCP server to assign
// these values to the hosts on the network.
//
// Each host (Windows computer and Rabbits) uses the Subnet Mask to determine
// if the computer it is connecting to is on the same subnet.  If it is on the
// same subnet, the data is sent directly.  If not, the computer sends it
// through a router (default gateway).  The part of the mask with ones is the
// part which specifies the local subnet - this part should match in all hosts
// on the subnet.  The Subnet Mask should also be the same in all hosts so they
// all understand which computers are on the same subnet.
//
// To use a Windows computer to talk to the Rabbits, you can either manually
// set the IP Address and Subnet Mask to match the Rabbits or set the Rabbits
// to match the Windows computer.  Since the Windows computer may also be used
// on other networks, it is inconvenient to switch back and forth; thus the
// Rabbits in this system use values which match the typical Windows computer.
//
// When the Windows computer is connected without manually setting the
// IP Address and Subnet Mask, a yellow warning sign will be displayed by the
// network icon and the warning "Limited or no connectivity" will be shown.
// This does not affect communication with the Rabbits and the warning may be
// ignored.

public void connect()
{

    NetworkInterface iFace;

    iFace = findNetworkInterface();

    connectNotchers(iFace);

}//end of NotcherHandler::connect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::connectNotchers
//
// Opens a TCP/IP connection with the Notchers.
//
// To find the units, first makes a UDP connection via the network interface
// pNetworkInterface. If there are multiple interfaces in the system, such as
// an Internet connection, the UDP broadcasts will fail unless tied to the
// interface connected to the remotes.
//

@SuppressWarnings("ConvertToTryWithResources")
private void connectNotchers(NetworkInterface pNetworkInterface)
{

    tsLog.appendLine("Broadcasting greeting to all Notchers...");

    //open socket
    MulticastSocket socket = openUDPBroadcastSocket(pNetworkInterface);
    if (socket == null) return;
    
    //set up socket and "Roll Call" datagram packet
    DatagramPacket outPacket = null;
    if (!setupSocketAndRollCallPacket(socket, outPacket)){ return; }
    
    int loopCount = 0;
    byte[] inBuf = new byte[256];
    DatagramPacket inPacket;
    inPacket = new DatagramPacket(inBuf, inBuf.length);
    int responseCount = 0;
    
    //broadcast the roll call greeting several times
    while(loopCount++ < 5 && responseCount < MAX_NUM_NOTCHERS){

        responseCount = sendRollCallAndProcessResponders(
                                   socket, outPacket, inPacket, responseCount);
        
    }// while(loopCount...

    socket.close();

    numberOfNotchers = responseCount;
    
    //bail out if no boards responded
    if (numberOfNotchers == 0) {return;}

    // allow each unit to connect to the remote

    for (int i = 0; i < numberOfNotchers; i++){
        if(notchers[i] != null) { notchers[i].connect(); }
    }

    tsLog.appendLine("\nAll Notchers ready.\n");

    //initialize each Control board
    initializeNotchers();

    //debug mks
    int x = signed2BytesToInt((byte)0x80, (byte)0x00);
    int y = unSigned2BytesToInt((byte)0x80, (byte)0x00);
    int z = signed4BytesToInt((byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00);
    //debug mks
    
}//end of NotcherHandler::connectNotchers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler:openUDPBroadcastSocket
//
// Opens a UPD broadcast socket for sending queries to the remote units.
//
// Returns the socket if successful.
// Returns null on failure.
//

private MulticastSocket openUDPBroadcastSocket(
                                            NetworkInterface pNetworkInterface)
{

    MulticastSocket socket;
    
    try{
        if (!simulateNotchers) {
            socket = new MulticastSocket(4445);
            if (pNetworkInterface != null) {
                try{
                    socket.setNetworkInterface(pNetworkInterface);
                }catch (SocketException e) {return(null);}
            }
        }
        else {socket = new UDPSimulator(4445, "Notcher present...", 4);}

    }
    catch (IOException e) {
        logSevere(e.getMessage() + " - Error: 352");
        tsLog.appendLine("Couldn't create Notcher broadcast socket.");
        return(null);
    }
    
    return(socket);
    
}//end of NotcherHandler::openUDPBroadcastSocket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::setupSocketAndRollCallPacket
//
// Sets up a MulticastSocket and datagram packet for use.
//
// Returns true on success.
// Returns false on failure.
//

private boolean setupSocketAndRollCallPacket(MulticastSocket pSocket,
                                                    DatagramPacket pOutPacket)
{

    String castMsg = "Notcher Roll Call";
    byte[] outBuf;
    outBuf = castMsg.getBytes();
    InetAddress group;

    try{
        group = InetAddress.getByName("230.0.0.1");
    }
    catch (UnknownHostException e){
        logSevere(e.getMessage() + " - Error: 224");
        pSocket.close();
        return(false);
    }

    pOutPacket = new DatagramPacket(outBuf, outBuf.length, group, 4446);

    //force socket.receive to return if no packet available within 1 millisec
    try{
        pSocket.setSoTimeout(1000);
    }
    catch(SocketException e){
        logSevere(e.getMessage() + " - Error: 236");
        return(false);
    }
        
    return(true);
    
}//end of NotcherHandler::setupSocketAndRollCallPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::sendRollCallAndProcessResponders
//
// Sets up a MulticastSocket and datagram packet for use.
//
// Adds number of responding units to pResponseCount and returns the new value.
// Returns -1 on error.
//

private int sendRollCallAndProcessResponders(MulticastSocket pSocket,
       DatagramPacket pOutPacket, DatagramPacket pInPacket, int pResponseCount)
{
    
    String response;    
    int responseCount = pResponseCount;
    
    try {pSocket.send(pOutPacket);}
    catch(IOException e) {
        logSevere(e.getMessage() + " - Error: 245");
        pSocket.close();
        return(-1);
    }

    waitSleep(1000); //sleep to delay between broadcasts

    //check for response packets from the remotes
    try{
        //read response packets until a timeout error exception occurs or
        //until max number of units have responded
        while(responseCount < MAX_NUM_NOTCHERS){

            pSocket.receive(pInPacket);

            //store each new ip address in a Control board object
            for (int i = 0; i < MAX_NUM_NOTCHERS; i++){

                //if a ut board already has the same ip, don't save it
                //this might occur if a board responds more than once as the
                //host repeatedly broadcasts the greeting
                //since the first utBoard objects in the array are filled
                //first -- this will catch duplicates

                if (notchers[i] != null && notchers[i].ipAddr != null &&
                        notchers[i].ipAddr == pInPacket.getAddress()){
                    break;
                }

                //only boards which haven't been already seen make it here

                //if an empty reached, store new unit there
                if (notchers[i] == null){

                    notchers[i] = new Notcher(i,
                            RUNTIME_PACKET_SIZE, simulateNotchers, tsLog);
                    notchers[i].init();

                    //store the ip address in the unused object
                    notchers[i].setIPAddr(pInPacket.getAddress());

                    //count unique IP address responses
                    responseCount++;

                    //convert the response packet to a string
                    response = new String(
                            pInPacket.getData(), 0, pInPacket.getLength());

                    //display the greeting string from the remote
                    tsLog.appendLine(
                                    notchers[i].ipAddrS + "  " + response);

                    break;
                }
            }//for (int i = 0; i < numberOfControlBoards; i++)
        }//while(true)
    }//try
    catch(IOException e){
        //this reached if receive times out -- take no action
    }    

    return(responseCount);
    
}//end of NotcherHandler::sendRollCallAndProcessResponders
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler:findNetworkInterface
//
// Finds the network interface for communication with the remotes. Returns
// null if no suitable interface found.
//
// The first interface which is connected and has an IP address beginning with
// 169.254.*.* is returned.
//
// NOTE: If more than one interface is connected and has a 169.254.*.*
// IP address, the first one in the list will be returned. Will need to add
// code to further differentiate the interfaces if such a set up is to be
// used. Internet connections will typically not have such an IP address, so
// a second interface connected to the Internet will not cause a problem with
// the existing code.
//
// If a network interface is not specified for the connection, Java will
// choose the first one it finds. The TCP/IP protocol seems to work even if
// the wrong interface is chosen. However, the UDP broadcasts for wake up calls
// will not work unless the socket is bound to the appropriate interface.
//
// If multiple interface adapters are present, enabled, and running (such as
// an Internet connection), it can cause the UDP broadcasts to fail.
//

private NetworkInterface findNetworkInterface()
{

    tsLog.appendLine("");

    NetworkInterface iFace = null;

    try{
        tsLog.appendLine("Full list of Network Interfaces:");
        for (Enumeration<NetworkInterface> en =
              NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

            NetworkInterface intf = en.nextElement();
            tsLog.appendLine("    " + intf.getName() + " " +
                                                intf.getDisplayName());

            for (Enumeration<InetAddress> enumIpAddr =
                     intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {

                String ipAddr = enumIpAddr.nextElement().toString();

                tsLog.appendLine("        " + ipAddr);

                if(ipAddr.startsWith("/169.254")){
                    iFace = intf;
                    tsLog.appendLine("==>> Binding to above adapter...");
                }
            }
        }
    }
    catch (SocketException e) {
        tsLog.appendLine(" (error retrieving network interface list)");
    }

    tsLog.appendLine("");
    
    return(iFace);

}//end of NotcherHandler::findNetworkInterface
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::initializeNotchers
//
// Sets up each Notcher with various settings.
//

private void initializeNotchers()
{

    for (int i = 0; i < numberOfNotchers; i++) {
        if (notchers[i] != null) { notchers[i].initialize(); }
    }

}//end of NotcherHandler::initializeNotchers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::signed2BytesToInt
//
// Converts the two bytes of a signed short int to an integer.
//

private int signed2BytesToInt(byte pByte1, byte pByte0)
{

    return (short)((pByte1<<8) & 0xff00) + (pByte0 & 0xff);
    
}//end of NotcherHandler::signed2BytesToInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::unSigned2BytesToInt
//
// Converts the two bytes of an unsigned short to an integer.
//

private int unSigned2BytesToInt(byte pByte1, byte pByte0)
{

    return (int)((pByte1<<8) & 0xff00) + (pByte0 & 0xff);
    
}//end of NotcherHandler::unSigned2BytesToInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::signed4BytesToInt
//
// Converts the four bytes of a signed integer to an integer.
//

private int signed4BytesToInt(
                        byte pByte3, byte pByte2, byte pByte1, byte pByte0)
{
            
    return
         ((pByte3<<24) & 0xff000000) +  ((pByte2<<16) & 0xff0000)
          + ((pByte1<<8) & 0xff00) + (pByte0 & 0xff);

}//end of NotcherHandler::signed4BytesToInt
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::logStatus
//
// Writes various status and error messages to the log window.
//

private void logStatus(Log pLogWindow)
{

}//end of NotcherHandler::logStatus
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::shutDown
//
// This function should be called before exiting the program.  Overriding the
// "finalize" method does not work as it does not get called reliably upon
// program exit.
//

public void shutDown()
{

    for (int i = 0; i < numberOfNotchers; i++) {
        if (notchers[i]!= null) { notchers[i].shutDown(); }
    }

}//end of NotcherHandler::shutDown
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::loadCalFile
//
// This loads the file used for storing calibration information such as cut
// depth, cut speed, cut aggression, etc.
//
// Each child object is passed a pointer to the file so that they may load their
// own data.
//

private void loadCalFile(IniFile pCalFile)
{

    // load any settings which apply to all Notchers here
    
    // NO COMMON DATA CURRENTLY LOADED
    
    
    // call each Notcher to load its own data
    
    for (int i = 0; i < numberOfNotchers; i++) {
        notchers[i].loadCalFile(pCalFile);
    }

}//end of NotcherHandler::loadCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::saveCalFile
//
// This saves the file used for storing calibration information such as cut
// depth, cut speed, cut aggression, etc.
//
// Each child object is passed a pointer to the file so that they may save their
// own data.
//

private void saveCalFile(IniFile pCalFile)
{

    // load any settings which apply to all Notchers here
    
    // NO COMMON DATA CURRENTLY SAVED
        
    // call each Notcher to save its data
    
    for (int i = 0; i < numberOfNotchers; i++) {
        notchers[i].saveCalFile(pCalFile);
    }

}//end of NotcherHandler::saveCalFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::displayMessages
//
// Displays any messages received from the remote.
//
// NOTE: If a message needs to be displayed by a thread other than the main
// Java thread, use threadSafeLog instead.
//

private void displayMessages()
{

}//end of NotcherHandler::displayMessages
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::doTasks
//
// Should be called by a timer so that various tasks can be performed as
// necessary.  Since Java doesn't update the screen during calls to the user
// software, it is necessary to execute tasks in a segmented fashion if it
// is necessary to display status messages along the way.
//

private void doTasks()
{

    displayMessages();

}//end of NotcherHandler::doTasks
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::setMode
//
// Sets the mode to CUT, STOPPED, etc. for unit with IP address pIP.
//

public void setMode(String pIP, int pOpMode)
{

    opMode = pOpMode;

    if (opMode == NotcherHandler.CUT_MODE){

        invokeCutMode(pIP);
        
    }

    if (opMode == NotcherHandler.STOPPED_MODE){

        invokeStopMode(pIP);
            
    }

}//end of NotcherHandler::setMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::invokeCutMode
//
// Puts unit with IP address pIP in Cut mode.
//

private void invokeCutMode(String pIP)
{
    
    Notcher n = findUnitByIP(pIP);
    if (n != null) n.invokeCutMode();

}//end of NotcherHandler::invokeCutMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::invokeStopMode
//
// Puts unit with IP address pIP in Stop mode.
//

private void invokeStopMode(String pIP)
{

    Notcher n = findUnitByIP(pIP);
    if (n != null) n.invokeStopMode();    
        
}//end of NotcherHandler::invokeStopMode
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::getMonitorPacket
//
// Returns monitoring data from unit with IP address pIP.
//

public byte[] getMonitorPacket(String pIP)
{

    Notcher n = findUnitByIP(pIP);
    
    if (n == null){
        return(null);
    }
    else{
        return n.getDataPacket();
    }

}//end of NotcherHandler::getMonitorPacket
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::zeroDepthCount
//
// Sends command to zero the depth count to unit with IP address pIP.
//

public void zeroDepthCount(String pIP)
{

    Notcher n = findUnitByIP(pIP);
    if (n != null) n.zeroDepthCount();

}//end of NotcherHandler::zeroEncoderCount
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::findUnitByIP
//
// Finds the Notcher object in the array which has IP address of pIP.
//
// Returns reference to the notcher or null if no match found.
//

public Notcher findUnitByIP(String pIP)
{
    
    for (int i = 0; i < numberOfNotchers; i++) {
        if (notchers[i] != null && notchers[i].ready) {
            if (pIP.equals(notchers[i].ipAddrS)) {
                return (notchers[i]);
            }
        }
    }
    
    return(null);
    
}//end of NotcherHandler::findUnitByIP

//-----------------------------------------------------------------------------
// NotcherHandler::getSimulate
//
// Returns the simulate flag.  This flag is set if any simulation is being
// performed so that outside classes can adjust accordingly, such as by
// starting a thread to drive the simulation functions.
//

public boolean getSimulate()
{
    
    return (simulateNotchers);

}//end of NotcherHandler::getSimulate
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::waitSleep
//
// Sleeps for pTime milliseconds.
//

private void waitSleep(int pTime)
{

    try{
        Thread.sleep(pTime);
    }
    catch(InterruptedException e){}

}//end of NotcherHandler::waitSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::setUDPResponseFlag
//
// Sets the udpResponseFlag true in the Notcher which has an ipAddress matching
// pIPAddress.
//
// This is useful for determining which unit did not respond.
//

private void setUDPResponseFlag(String pIPAddress)
{

    //set the flag in the utBoard with the matching IP address
    for (int i = 0; i < numberOfNotchers; i++) {
        if (notchers[i].ipAddrS.equals(pIPAddress)) {
            notchers[i].udpResponseFlag = true;
        }
    }

}//end of NotcherHandler::setUDPResponseFlag
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::displayUnresponsiveIPAddresses
//
// Displays a list of units which do not have their udp flag set.
//
// This is useful for determining which unit(s) did not respond.
//

private void displayUnresponsiveIPAddresses()
{

    //set the flag in the utBoard with the matching IP address
    for (int i = 0; i < numberOfNotchers; i++) {
        if (!notchers[i].udpResponseFlag) {
            tsLog.appendLine("Notcher " + notchers[i].ipAddrS);
        }
    }

}//end of NotcherHandler::displayUnresponsiveIPAddresses
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::sendByteUDP
//
// Sends pByte via the UDP socket pSocket using pOutPacket.
//

private void 
    sendByteUDP(DatagramSocket pSocket, DatagramPacket pOutPacket, byte pByte)
{

    pOutPacket.getData()[0] = pByte; //store the byte in the buffer

    pOutPacket.setLength(1); //send one byte

    try {
        pSocket.send(pOutPacket);
    }
    catch(IOException e){
        logSevere(e.getMessage() + " - Error: 1995");
    }

}//end of NotcherHandler::sendByteUDP
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::logSevere
//
// Logs pMessage with level SEVERE using the Java logger.
//

private void logSevere(String pMessage)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage);

}//end of NotcherHandler::logSevere
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::logStackTrace
//
// Logs stack trace info for exception pE with pMessage at level SEVERE using
// the Java logger.
//

private void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of NotcherHandler::logStackTrace
//-----------------------------------------------------------------------------

}//end of class NotcherHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
