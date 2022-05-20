package teste.ci1;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import com.ibm.cics.server.CicsConditionException;
import com.ibm.cics.server.InvalidRequestException;
import com.ibm.cics.server.Program;
import com.ibm.cics.server.Task;

/**
 * Provides a simple example of LINKing to a CICS program using JCICS,
 * passing a byte array for a COMMAREA.
 * 
 * This class executes in an OSGi JVM server environment.
 */
public class link{

    private static final String PROG_NAME = "EC01";
    private static final int CA_LEN = 18;
    private static final String CCSID = System.getProperty("com.ibm.cics.jvmserver.local.ccsid");

    /**
     * Main entry point to a CICS OSGi program.
     * This can be called via a LINK or a 3270 attach.
     * 
     * The fully-qualified name of this class should be added to the
     * CICS-MainClass entry in the parent OSGi bundle's manifest.
     */
    public static void main(String[] args){
        // Get details about our current CICS task
        Task task = Task.getTask();
        task.out.println(" - Starting LinkProg1");

        // Create a reference to the Program we will invoke
        Program prog = new Program();

        // Specify the properties on the program
        prog.setName(PROG_NAME);
        
        // Don't syncpoint between remote links, this is the default 
        // Setting true ensures each linked program runs in its own UOW and
        // allows the a remote server program to use a syncpoint command
        prog.setSyncOnReturn(false);  

        // Init commarea and invoke the LINK to CICS program
        // Commarea byte array should be as long as the DFHCOMMAREA structure in COBOL
        // Commarea will be padded with nulls which ensures CICS can null truncate DPL flows
        byte[] ca = new byte[CA_LEN];
        linkProg(prog, ca);

        // Build output string from commarea assuming returned data encoded in CICS local EBCDIC ccsid
        String resultStr;
        try {
            resultStr = new String(ca,CCSID);
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException(ue);     
        } 

        // Completion message  
        String msg = MessageFormat.format("Returned from link to {0} with {1}", prog.getName(), resultStr);
        task.out.println(msg);
    }


    /**
     * Link to the CICS COBOL program catching any errors from CICS
     * The invoked CICS progra will retrun the date and time
     * 
     * @param ca - byte array for input and output COMMAREA.
     */ 
    private static void linkProg(Program lp, byte[] ca) {
        try {
            // Execute the LINK to the CICS program 
            // The COMMAREA byte array is updated after the call and does not need to be returned
            lp.link(ca);
        }
        catch (InvalidRequestException ire) {
            // Ignore invalid request and just log
            Task.getTask().out.println("Invalid request on link - INVREQ");
        }
        catch (CicsConditionException cce) {
            throw new RuntimeException(cce);
        }    
    }
}

