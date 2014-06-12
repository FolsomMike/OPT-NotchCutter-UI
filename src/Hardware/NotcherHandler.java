/******************************************************************************
* Title: NotcherHandler.java
* Author: Mike Schoonover, Hunter Schoonover
* Date: 4/23/09
*
* Purpose:
*
* This class handles the Notcher communications.
*
* 
*
* wip hss --
* 
* wip hss end
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
import javax.swing.*;
import model.IniFile;
import view.Log;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class NotcherHandler
//
// This class creates and handles the hardware interface.
//

public class NotcherHandler extends Object{

    boolean controlBoardsReady = false;

    boolean simulateControlBoards;
    
    Notcher[] controlBoards;
    int numberOfControlBoards;

    boolean logEnabled = true;

    Log log;

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

public NotcherHandler(Log pLog)

{

    log = pLog;

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

}//end of NotcherHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::configure
//
// Loads configuration settings from the configuration.ini file.  These set
// the number and style of channels, gates, etc.
// The various child objects are then created as specified by the config data.
//

private void configure(IniFile pConfigFile)
{

    simulateControlBoards =
       pConfigFile.readBoolean("Hardware", "Simulate Control Boards", false);
    
    numberOfControlBoards =
                 pConfigFile.readInt("Hardware", "Number of Control Boards", 1);
    
    //create and setup the Control boards
    configureControlBoards();

}//end of NotcherHandler::configure
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

    connectControlBoards(iFace);

}//end of NotcherHandler::connect
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::connectControlBoards
//
// Opens a TCP/IP connection with the Control Board.
//
// To find the boards, first makes a UDP connection via the network interface
// pNetworkInterface. If there are multiple interfaces in the system, such as
// an Internet connection, the UDP broadcasts will fail unless tied to the
// interface connected to the remotes.
//

public void connectControlBoards(NetworkInterface pNetworkInterface)
{

    log.appendLine("Broadcasting greeting to all Control boards...");

    MulticastSocket socket;

    try{
        if (!simulateControlBoards) {
            socket = new MulticastSocket(4445);
            if (pNetworkInterface != null) {
                try{
                    socket.setNetworkInterface(pNetworkInterface);
                }catch (SocketException e) {}//let system bind to default interface
            }
        }
        else {socket = new UDPSimulator(4445, "Control Board present...");}

    }
    catch (IOException e) {
        logSevere(e.getMessage() + " - Error: 204");
        log.appendLine("Couldn't create Control broadcast socket.");
        return;
    }

    int loopCount = 0;
    String castMsg = "Control Board Roll Call";
    byte[] outBuf;
    outBuf = castMsg.getBytes();
    InetAddress group;
    DatagramPacket outPacket;
    byte[] inBuf = new byte[256];
    DatagramPacket inPacket;
    inPacket = new DatagramPacket(inBuf, inBuf.length);
    int responseCount = 0;
    String response;

    try{
        group = InetAddress.getByName("230.0.0.1");
    }
    catch (UnknownHostException e){
        logSevere(e.getMessage() + " - Error: 224");
        socket.close();
        return;
    }

    outPacket = new DatagramPacket(outBuf, outBuf.length, group, 4446);

    //force socket.receive to return if no packet available within 1 millisec
    try{
        socket.setSoTimeout(1000);
    }
    catch(SocketException e){
        logSevere(e.getMessage() + " - Error: 236");
    }

    //broadcast the roll call greeting several times - bail out when expected
    //number of different Control boards have responded
    while(loopCount++ < 5 && responseCount < numberOfControlBoards){

        try {socket.send(outPacket);}
        catch(IOException e) {
            logSevere(e.getMessage() + " - Error: 245");
            socket.close();
            return;
        }

        waitSleep(1000); //sleep to delay between broadcasts

        //check for response packets from the remotes
        try{
            //read response packets until a timeout error exception occurs or
            //until expected number of different Control boards have responded
            while(responseCount < numberOfControlBoards){

                socket.receive(inPacket);

                //store each new ip address in a Control board object
                for (int i = 0; i < numberOfControlBoards; i++){

                    //if a ut board already has the same ip, don't save it
                    //this might occur if a board responds more than once as the
                    //host repeatedly broadcasts the greeting
                    //since the first utBoard objects in the array are filled
                    //first -- this will catch duplicates

                    if (controlBoards[i].ipAddr != null &&
                            controlBoards[i].ipAddr == inPacket.getAddress()){
                        break;
                    }

                    //only boards which haven't been already seen make it here

                    //if an unused board reached, store ip there
                    if (controlBoards[i].ipAddr == null){

                        //store the ip address in the unused object
                        controlBoards[i].setIPAddr(inPacket.getAddress());

                        //count unique IP address responses
                        responseCount++;

                        //convert the response packet to a string
                        response = new String(
                                inPacket.getData(), 0, inPacket.getLength());

                        //display the greeting string from the remote
                        log.appendLine(
                             controlBoards[i].ipAddrS + "  " + response);

                        break;
                    }
                }//for (int i = 0; i < numberOfControlBoards; i++)
            }//while(true)
        }//try
        catch(IOException e){
            //this reached if receive times out -- take no action
        }
    }// while(loopCount...

    socket.close();

    //bail out if no boards responded
    if (responseCount == 0) {return;}

    // allow each unit to connect to the remote

    if (responseCount > 0){
        for (int i = 0; i < numberOfControlBoards; i++){
            controlBoards[i].connect();
        }
    }//if (responseCount > 0)

    log.appendLine("All Control boards ready.");

    //initialize each Control board
    initializeControlBoards();

}//end of NotcherHandler::connectControlBoards
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

public NetworkInterface findNetworkInterface()
{

    log.appendLine("");

    NetworkInterface iFace = null;

    try{
        log.appendLine("Full list of Network Interfaces:" + "\n");
        for (Enumeration<NetworkInterface> en =
              NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

            NetworkInterface intf = en.nextElement();
            log.appendLine("    " + intf.getName() + " " +
                                                intf.getDisplayName() + "\n");

            for (Enumeration<InetAddress> enumIpAddr =
                     intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {

                String ipAddr = enumIpAddr.nextElement().toString();

                log.appendLine("        " + ipAddr + "\n");

                if(ipAddr.startsWith("/169.254")){
                    iFace = intf;
                    log.appendLine("==>> Binding to above adapter...\n");
                }
            }
        }
    }
    catch (SocketException e) {
        log.appendLine(" (error retrieving network interface list)");
    }

    log.appendLine("");
    
    return(iFace);

}//end of NotcherHandler::findNetworkInterface
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::initializeControlBoards
//
// Sets up each Control board with various settings.
//

public void initializeControlBoards()
{

    for (int i = 0; i < numberOfControlBoards; i++) {

        if (controlBoards[i] != null) { controlBoards[i].initialize(); }
    }

}//end of NotcherHandler::initializeControlBoards
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::driveSimulation
//
// Drive any simulation functions if they are active.  This function is usually
// called from a thread.
//

public void driveSimulation()
{

    if (simulateControlBoards) {
        for (int i = 0; i < numberOfControlBoards; i++) {
            controlBoards[i].driveSimulation();
        }
    }

}//end of NotcherHandler::driveSimulation
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::logStatus
//
// Writes various status and error messages to the log window.
//

public void logStatus(Log pLogWindow)
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

    for (int i = 0; i < numberOfControlBoards; i++) {
        if (controlBoards[i]!= null) {
            controlBoards[i].shutDown();
        }
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

public void loadCalFile(IniFile pCalFile)
{

    // load any settings which apply to all Notchers here
    
    // NO COMMON DATA CURRENTLY LOADED
    
    
    // call each Notcher to load its own data
    
    for (int i = 0; i < numberOfControlBoards; i++) {
        controlBoards[i].loadCalFile(pCalFile);
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

public void saveCalFile(IniFile pCalFile)
{

    // load any settings which apply to all Notchers here
    
    // NO COMMON DATA CURRENTLY SAVED
        
    // call each Notcher to save its data
    
    for (int i = 0; i < numberOfControlBoards; i++) {
        controlBoards[i].saveCalFile(pCalFile);
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

public void displayMessages()
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

public void doTasks()
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
// NotcherHandler::configureControlBoards
//
// Loads configuration settings from the configuration.ini file relating to
// the boards and creates/sets them up.
//

private void configureControlBoards()
{

    //create an array of boards per the config file setting
    if (numberOfControlBoards > 0){

        controlBoards = new Notcher[numberOfControlBoards];

        //pass the config filename instead of the configFile already opened
        //because the UTBoards have to create their own iniFile objects to read
        //the config file because they each have threads and iniFile is not
        //threadsafe

        for (int i = 0; i < numberOfControlBoards; i++) {
            controlBoards[i] = new Notcher("Control " + (i+1),
                    i, RUNTIME_PACKET_SIZE, simulateControlBoards, log);
            controlBoards[i].init();

        }

    }//if (numberOfControlBoards > 0)
    
}//end of NotcherHandler::configureControlBoards
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::findUnitByIP
//
// Finds the Notcher object in the array which has IP address of pIP.
//
// Returns reference to the notcher or null if no match found.
//

Notcher findUnitByIP(String pIP)
{
    
    for (int i = 0; i < numberOfControlBoards; i++) {
        if (controlBoards[i] != null && controlBoards[i].ready) {
            if (pIP.equals(controlBoards[i].ipAddrS)) {
                return (controlBoards[i]);
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
    
    return (simulateControlBoards);

}//end of NotcherHandler::getSimulate
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::waitSleep
//
// Sleeps for pTime milliseconds.
//

void waitSleep(int pTime)
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

void setUDPResponseFlag(String pIPAddress)
{

    //set the flag in the utBoard with the matching IP address
    for (int i = 0; i < numberOfControlBoards; i++) {
        if (controlBoards[i].ipAddrS.equals(pIPAddress)) {
            controlBoards[i].udpResponseFlag = true;
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

void displayUnresponsiveIPAddresses()
{

    //set the flag in the utBoard with the matching IP address
    for (int i = 0; i < numberOfControlBoards; i++) {
        if (!controlBoards[i].udpResponseFlag) {
            log.appendLine("Notcher " + controlBoards[i].ipAddrS);
        }
    }

}//end of NotcherHandler::displayUnresponsiveIPAddresses
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherHandler::sendByteUDP
//
// Sends pByte via the UDP socket pSocket using pOutPacket.
//

void sendByteUDP(DatagramSocket pSocket, DatagramPacket pOutPacket, byte pByte)
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

void logSevere(String pMessage)
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

void logStackTrace(String pMessage, Exception pE)
{

    Logger.getLogger(getClass().getName()).log(Level.SEVERE, pMessage, pE);

}//end of NotcherHandler::logStackTrace
//-----------------------------------------------------------------------------

}//end of class NotcherHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
