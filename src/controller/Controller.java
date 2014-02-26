/******************************************************************************
* Title: Controller.java
* Author: Mike Schoonover
* Date: 11/15/12
*
* Purpose:
*
* This class is the Controller in a Model-View-Controller architecture.
* It creates the Model and the View.
* It tells the View to update its display of the data in the model.
* It handles user input from the View (button pushes, etc.)*
* It tells the Model what to do with its data based on these inputs and tells
*   the View when to update or change the way it is displaying the data.
*
* In this implementation:
*   the Model knows only about itself
*   the View knows only about the Model and can get data from it
*   the Controller knows about the Model and the View and interacts with both
*
* In this specific MVC implementation, the Model does not send messages to
* the View -- it expects the Controller to trigger the View to request data
* from the Model when necessary.
*
* The View sends messages to the Controller in the form of action messages
* to an EventHandler object -- in this case the Controller is designated to the
* View as the EventHandler.
*
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

//-----------------------------------------------------------------------------

package controller;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import model.ADataClass;
import model.Options;
import view.View;
import view.MFloatSpinner;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Controller
//

public class Controller implements EventHandler, Runnable
{

    private ADataClass aDataClass;

    private View view;

    private Options options;
    
    private NotcherController[] notchControllerArray;

    private Boolean blinkStatusLabel = false;

    private String errorMessage;

    private SwingWorker workerThread;

    private DecimalFormat decimalFormat1 = new DecimalFormat("#.0");

    private Font tSafeFont;
    private String tSafeText;

    private int displayUpdateTimer = 0;
    
    // hss wip -- should remove
    private double voltSimLevel = 5;
    private double currentSimLevel = 0;

    private String XMLPageFromRemote;

    private boolean shutDown = false;

    private final JFileChooser fileChooser = new JFileChooser();

    private final String newline = "\n";
    
    // hss wip -- should be removed after notchers
    // are detected on the network
    private static final int NCNUMBER = 4;

//-----------------------------------------------------------------------------
// Controller::Controller (constructor)
//

public Controller()
{

}//end of Controller::Controller (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    aDataClass = new ADataClass();
    aDataClass.init();

    view = new View(this, aDataClass);
    view.init();

    //create and load the program options
    options = new Options();

    //start the control thread
    new Thread(this).start();

    view.setupAndStartMainTimer();
    
    determineNumberOfNotchersOnNetwork();
    
    createNotcherControllers();

}// end of Controller::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::determineNumberOfNotchersOnNetwork
//
// Sends a UDP packet to all remotes on the network. The notchers will return
// their IP addresses which are then stored in a list. The list is then used
// to make connection with each one separately.
// 
// hss wip -- does not yet detect notchers on network; the number of notchers is
// preset
//

public void determineNumberOfNotchersOnNetwork()
{

    notchControllerArray = new NotcherController[NCNUMBER];

}// end of Controller::determineNumberOfNotchersOnNetwork
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::createNotcherControllers
//
// Goes through the process of creating a notcherController object for each
// index in the notcher array.
//

public void createNotcherControllers()
{

    for (int i = 0; i < notchControllerArray.length; i++) {
        
        notchControllerArray[i] = new NotcherController(view, i);
        
        notchControllerArray[i].init();
    
    }

}// end of Controller::createNotcherControllers
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::actionPerformed
//
// Responds to events.
//
// This is identical to the method employed by  ActionListener objects. This
// object is not an ActionListener, but uses the same concept for clarity. The
// "View" (MVC Concept) objects catch GUI events and call this method to pass
// those events to this "Controller" object.
//

@Override
public void actionPerformed(ActionEvent e)
{

    if ("Timer".equals(e.getActionCommand())) {doTimerActions();}

    if ("Display Log".equals(e.getActionCommand())) {displayLog();}

    if ("Display Help".equals(e.getActionCommand())) {displayHelp();}

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

}//end of Controller::actionPerformed
//-----------------------------------------------------------------------------

/*
//-----------------------------------------------------------------------------
// Controller::paintComponent
//

@Override
public void paintComponent (Graphics g)
{

}// end of Controller::paintComponent
//-----------------------------------------------------------------------------

*/

//-----------------------------------------------------------------------------
// Controller::loadDataFromFile
//
// Loads data from a file.
//

public void loadDataFromFile()
{

    aDataClass.loadFromTextFile();

    view.updateGUIDataSet1();
    
}//end of Controller::loadDataFromFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::saveDataToFile
//
// Saves data to a file.
//

public void saveDataToFile()
{

    view.updateModelDataSet1();

    aDataClass.saveToTextFile();

}//end of Controller::saveDataToFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::doTimerActions
//
// Performs actions driven by the timer.
//
// Not used for accessing network -- see run function for details.
//

public void doTimerActions()
{
    
    // calls all of the doTimerActions for each of the notcherContollers
    for (int i = 0; i < notchControllerArray.length; i++) {
        
        notchControllerArray[i].doTimerActions();
    
    }
    
}//end of Controller::doTimerActions
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::displayLog
//
// Displays the log window. It is not released after closing as the information
// is retained so it can be viewed the next time the window is opened.
//

private void displayLog()
{

    view.displayLog();

}//end of Controller::displayLog
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::displayHelp
//
// Displays help information.
//

private void displayHelp()
{

    view.displayHelp();

}//end of Controller::displayHelp
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::displayAbout
//
// Displays about information.
//

private void displayAbout()
{

    view.displayAbout();

}//end of Controller::displayAbout
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::doSomething1
//

private void doSomething1()
{


}//end of Controller::doSomething1
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::doSomethingInWorkerThread
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

}//end of Controller::doSomethingInWorkerThread
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::doSomething2
//

private void doSomething2()
{


}//end of Controller::doSomething2
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::run
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

}//end of Controller::run
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::threadSleep
//
// Calls the Thread.sleep function. Placed in a function to avoid the
// "Thread.sleep called in a loop" warning -- yeah, it's cheezy.
//

public void threadSleep(int pSleepTime)
{

    try {Thread.sleep(pSleepTime);} catch (InterruptedException e) { }

}//end of Controller::threadSleep
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::control
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


    //If a shut down is initiated, clean up and exit the program.

    if(shutDown){
        //exit the program
        System.exit(0);
    }

}//end of Controller::control
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::displayErrorMessage
//
// Displays an error dialog with message pMessage.
//

public void displayErrorMessage(String pMessage)
{

    view.displayErrorMessage(pMessage);

}//end of Controller::displayErrorMessage
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::shutDown
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

}//end of Controller::shutDown
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::windowClosing
//
// Handles actions necessary when the window is closing
//

@Override
public void windowClosing(WindowEvent e)
{

    //perform all shut down procedures

    shutDown();

}//end of Controller::windowClosing
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Controller::(various window & listener functions)
//
// These functions are implemented per requirements of interfaces WindowListener
// & ChnageListener but do nothing at the present time.  As code is added to 
// each function, it should be moved from this section and formatted properly.
//

@Override
public void stateChanged(ChangeEvent ce){}

@Override
public void windowActivated(WindowEvent e){}
@Override
public void windowDeactivated(WindowEvent e){}
@Override
public void windowOpened(WindowEvent e){}
//@Override
//public void windowClosing(WindowEvent e){}
@Override
public void windowClosed(WindowEvent e){}
@Override
public void windowIconified(WindowEvent e){}
@Override
public void windowDeiconified(WindowEvent e){}

//end of Controller::(various window & change listener functions)
//-----------------------------------------------------------------------------


}//end of class Controller
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
