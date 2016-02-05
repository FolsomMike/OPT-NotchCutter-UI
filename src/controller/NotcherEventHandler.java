/******************************************************************************
* Title: NotcherController.java
* Author: Hunter Schoonover
* Date: 2/17/14
*
* Purpose:
*
* This class is the Notcher Controller. It creates and handles a notcher UI; 
* user input from the UI is handled by this class.
* 
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package controller;

import Hardware.Notcher;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import model.ADataClass;
import model.Options;
import view.MFloatSpinner;
import view.NotcherUI;
import view.View;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class NotcherEventHandler
//

public class NotcherEventHandler implements EventProcessor, Runnable
{

    private ADataClass aDataClass;

    private final View view;

    private final Notcher notcher;
    
    private Options options;
    
    private NotcherUI notcherUI;

    private Boolean blinkStatusLabel = false;

    private String errorMessage;

    private SwingWorker workerThread;

    private DecimalFormat decimalFormat1 = new DecimalFormat("#.0");

    private Font tSafeFont;
    private String tSafeText;

    private int displayUpdateTimer = 0;
    private int indexNumber;
    
    // hss wip -- should remove
    private double voltSimLevel = 5;
    private double currentSimLevel = 0;

    private String XMLPageFromRemote;
    
    private final String newline = "\n";

    private boolean shutDown = false;

    private final JFileChooser fileChooser = new JFileChooser();

//-----------------------------------------------------------------------------
// NotcherEventHandler::NotcherEventHandler (constructor)
//

public NotcherEventHandler(View pView, Notcher pNotcher, int pIndexNmber)
{

    view = pView; notcher = pNotcher; indexNumber = pIndexNmber;
    
}//end of NotcherEventHandler::NotcherEventHandler (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    aDataClass = new ADataClass();
    aDataClass.init();

    //create and load the program options
    options = new Options();

    //start the control thread
    // new Thread(this).start(); //hss wip
    
    notcherUI = createNotcherUI();

}// end of NotcherEventHandler::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::createNotcherUI
//
// Creates a notcher UI and passes this as an event handler.
//

public NotcherUI createNotcherUI()
{
    
    return (view.createNotcherUI(this, indexNumber));

}// end of NotcherEventHandler::createNotcherUI
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::actionPerformed
//
// Responds to events.
//
// This is identical to the method employed by  ActionListener objects. This
// object is not an ActionListener, but uses the same concept for clarity. The
// "NotcherUI" objects catch GUI events and call this method to pass
// those events to this "NotcherEventHandler" object.
//

@Override
public void actionPerformed(ActionEvent e)
{

    if ("Timer".equals(e.getActionCommand())) {doTimerActions();}

    if ("Display About".equals(e.getActionCommand())) {displayAbout();}

    if ("New File".equals(e.getActionCommand())) {doSomething1();}

    if ("Open File".equals(e.getActionCommand())) {
        doSomething2();
    }

    if ("Load Data From File".equals(e.getActionCommand())){
        loadDataFromFile();
    }

    if ("Save Data To File".equals(e.getActionCommand())){
        saveDataToFile();
    }
    
    if ("Change the tools and settings for this device".equals
                                                        (e.getActionCommand())){
        notcherUI.ActivateNotcherSettings();
    }
    
    if ("Change name of unit".equals(e.getActionCommand())){
        notcherUI.createChangeNameDialog();
    }
    
    if ("Apply this name to the unit".equals(e.getActionCommand())){
        notcherUI.changeNotcherName();
    }
    
    if ("Cancel the process of changing the name of the unit".equals
                                                        (e.getActionCommand())){
        notcherUI.disposeChangeNameDialog();
    }
    
    if ("Change status of electrode power".equals(e.getActionCommand())){
        changeElectrodePowerState();
    }
    

}//end of NotcherEventHandler::actionPerformed
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::changeElectrodePowerState
//
// Goes through the process of changing the electrode power state and sending
// the value to the notcher.
// Displays an error message if the sending of the data failed.
//

public void changeElectrodePowerState(){

    boolean success;
    
    if (notcherUI.getElectrodePowerBtnState()) {
        success = notcher.sendElectrodePowerOnOffCmd(Notcher.ON);
    }
    else {
        success = notcher.sendElectrodePowerOnOffCmd(Notcher.OFF);
    }
    
    if (success) { notcherUI.changeElectrodePowerButtonLabelAndTip(); }
    else { notcherUI.displayErrorMessage("The program failed to set the "
                            + "electrode power state of the notcher unit."); }

}//end of NotcherEventHandler::changeElectrodePowerState
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::stateChanged
//
    
@Override
public void stateChanged(ChangeEvent ce)

{
    
    //if for some reason the object which changed state is not a subclass of
    //of Component, do nothing as this code only handles Components
    
    if (!(ce.getSource() instanceof Component)) {
        return;
    }    
    
    //cast the object to a Component so it's methods can be accessed
    Component c = (Component)(ce.getSource());
    
    String name = c.getName();
        
    if (name.startsWith("Cut Depth Input Spinner")){
    
        //Since we know that the Component with the name starting with
        //"Cut Depth Input Spinner" is an MFloatSpinner (because we created it and
        // used that name for it), it can safely be cast to an MFloatSpinner.
        
        //using getText returns the value as a string formatted exactly as that 
        //shown in the spinner's text box and will be rounded off and truncated 
        //in the same manner
        String textValue = ((MFloatSpinner)c).getText();
        
    }
    
    else if (name.startsWith("Cutting Head Position Spinner")){
    
        //Since we know that the Component with the name starting with
        //"Cutting Head Position Spinner" is an MFloatSpinner (because we 
        //created it and used that name for it), it can safely be cast to an 
        //MFloatSpinner.

        //using getText returns the value as a string formatted exactly as that 
        //shown in the spinner's text box and will be rounded off and truncated 
        //in the same manner
        String textValue = ((MFloatSpinner)c).getText();
        
    }
        
}//end of NotcherEventHandler::stateChanged
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::loadDataFromFile
//
// Loads data from a file.
//

public void loadDataFromFile()
{

    aDataClass.loadFromTextFile();

    //notcherUI.updateGUIDataSet1();//hss wip
    
}//end of NotcherEventHandler::loadDataFromFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::saveDataToFile
//
// Saves data to a file.
//

public void saveDataToFile()
{

    //notcherUI.updateModelDataSet1();//hss wip

    aDataClass.saveToTextFile();

}//end of NotcherEventHandler::saveDataToFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::doTimerActions
//
// Performs actions driven by the timer.
//
// Not used for accessing network -- see run function for details.
//

public void doTimerActions()
{
    
}//end of NotcherEventHandler::doTimerActions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::doSimulationTimerActions
//
// Performs simulation actions driven by the timer.
//

public void doSimulationTimerActions()
{

    simulateVoltageAndCurrentLevels();
    
}//end of NotcherEventHandler::doSimulationTimerActions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::simulateVoltageAndCurrentLevels
//
// Simulates values for the voltage and current levels.
// 
// hss wip -- simulation purposes; should remove
//

public void simulateVoltageAndCurrentLevels()
{

    // hss wip
    // for simulation purposes -- should remove
    
    double deltaSim;
    
    // simulate value for voltage leds
    
    deltaSim = (5 * Math.random()) - 2.5;
    
    voltSimLevel += deltaSim;
    
    if (voltSimLevel < 0) {
        voltSimLevel = 0;
    }
    
    if (voltSimLevel > 10) {
        voltSimLevel = 10;
    }
    
    notcherUI.voltageLeds.setValue(voltSimLevel);
    
    notcherUI.voltageLeds.repaint();
    
    
    // simulate value for current leds
    
    deltaSim = (5 * Math.random()) - 2.5;
    
    currentSimLevel += deltaSim;
    
    if (currentSimLevel < 0) {
        currentSimLevel = 0;
    }
    
    if (currentSimLevel > 10) {
        currentSimLevel = 10;
    }
    
    notcherUI.currentLeds.setValue(currentSimLevel);
    
    notcherUI.currentLeds.repaint();

}// end of NotcherEventHandler::simulateVoltageAndCurrentLevels
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::displayAbout
//
// Displays about information.
//

private void displayAbout()
{

    view.displayAbout();

}//end of NotcherEventHandler::displayAbout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::doSomething1
//

private void doSomething1()
{


}//end of NotcherEventHandler::doSomething1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::doSomethingInWorkerThread
//
// Does nothing right now -- modify it to call a function which takes a long
// time to finish. It will be run in a background thread so the GUI is still
// responsive.
// -- CHANGE THE NAME TO REFLECT THE ACTION BEING DONE --
//

private void doSomethingInWorkerThread()
{

    //define and instantiate a worker thread to create the file


    //----------------------------------------------------------------------
    //class SwingWorker
    //

    workerThread = new SwingWorker<Void, String>() {
        @Override
        public Void doInBackground() {

            //do the work here by calling a function

            return(null);

        }//end of doInBackground

        @Override
        public void done() {

            //clear in progress message here if one is being displayed

            try {

                //use get(); function here to retrieve results if necessary
                //note that Void type here and above would be replaced with
                //the type of variable to be returned

                Void v = get();

            } catch (InterruptedException ignore) {}
            catch (java.util.concurrent.ExecutionException e) {
                String why;
                Throwable cause = e.getCause();
                if (cause != null) {
                    why = cause.getMessage();
                } else {
                    why = e.getMessage();
                }
                System.err.println("Error creating file: " + why);
            }//catch

        }//end of done

        @Override
        protected void process(java.util.List <String> pairs) {

            //this method is not used by this application as it is limited
            //the publish method cannot be easily called outside the class, so
            //messages are displayed using a ThreadSafeLogger object and status
            //components are updated using a GUIUpdater object

        }//end of process

    };//end of class SwingWorker
    //----------------------------------------------------------------------

}//end of NotcherEventHandler::doSomethingInWorkerThread
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::doSomething2
//

private void doSomething2()
{


}//end of NotcherEventHandler::doSomething2
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::run
//
// This is the part which runs as a separate thread.  The actions of accessing
// remote devices occur here.  If they are done in a timer call instead, then
// buttons and displays get frozen during the sometimes lengthy calls to access
// the network.
//
// NOTE:  All functions called by this thread must wrap calls to alter GUI
// components in the invokeLater function to be thread safe.
//

@Override
public void run()
{

    //call the control method repeatedly
    while(true){

        control();

        //sleep for 2 seconds -- all timing is based on this period
        threadSleep(2000);

    }

}//end of NotcherEventHandler::run
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::threadSleep
//
// Calls the Thread.sleep function. Placed in a function to avoid the
// "Thread.sleep called in a loop" warning -- yeah, it's cheezy.
//

public void threadSleep(int pSleepTime)
{

    try {Thread.sleep(pSleepTime);} catch (InterruptedException e) { }

}//end of NotcherEventHandler::threadSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::control
//
// Performs all display and control.  Call this from a thread.
//

public void control()
{

    //update the display every 30 seconds
    if (displayUpdateTimer++ == 14){
        displayUpdateTimer = 0;
        //call function to update stuff here
    }

}//end of NotcherEventHandler::control
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

public void displayErrorMessage(String pMessage)
{

    view.displayErrorMessage(pMessage);

}//end of NotcherEventHandler::displayErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::shutDown
//
// Disables chassis power and performs any other appropriate shut down
// operations.
//
// This is done by setting a flag so that this class's thread can do the
// actual work, thus avoiding thread contention.
//

public void shutDown()
{

    shutDown = true;

}//end of NotcherEventHandler::shutDown
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// NotcherEventHandler::(various window listener functions)
//
// These functions are implemented per requirements of interface Event Handler,
// which includes WindowListener functions, but do nothing at the present time.  
// As code is added to each function, it should be moved from this section and 
// formatted properly.
//
// Although NotcherEventHandler has no windows to control, the WindowListener
// functions are still required because they are a part of the EventProcessor
// interface.
//

@Override
public void windowActivated(WindowEvent e){}
@Override
public void windowDeactivated(WindowEvent e){}
@Override
public void windowOpened(WindowEvent e){}
@Override
public void windowClosing(WindowEvent e){}
@Override
public void windowClosed(WindowEvent e){}
@Override
public void windowIconified(WindowEvent e){}
@Override
public void windowDeiconified(WindowEvent e){}

//end of NotcherEventHandler::(various window listener functions)
//-----------------------------------------------------------------------------

}//end of class NotcherEventHandler
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
