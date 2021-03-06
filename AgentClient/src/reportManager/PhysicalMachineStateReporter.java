package reportManager;

import java.util.List;

import virtualMachineManager.PersistentExecutionManager;

/**
 * Class responsible for report this physical machine status. Every 30 seconds this class sends a keep alive message to UnaCloud server.
 * @author Clouder
 */
public class PhysicalMachineStateReporter extends Thread{

	private int REPORT_DELAY=60000;

	private static PhysicalMachineStateReporter instance;
	public static synchronized PhysicalMachineStateReporter getInstance(){
		if(instance==null)instance=new PhysicalMachineStateReporter();
		return instance;
	}
    /**
     * Constructs a physical machine reporter
     * @param id Id to be used to report this physical machine, It corresponds to the physical machine name
     * @param sleep How much should the reporter wait between reports
     */
    private PhysicalMachineStateReporter(){}

    /**
     * Method that is used to start the reporting thread
     */
    @Override
    public void run() {
       System.out.println("Start PhysicalMachineStateReporter");
       while(true){
           try{
        	   List<Long> ids = PersistentExecutionManager.returnIdsExecutions();
        	   String executions = "[";
        	   for(Long id:ids)executions+=","+id;
        	   executions = executions.replaceFirst(",", "");
        	   executions += "]";
               ServerMessageSender.reportPhyisicalMachine(executions);
           }catch(Exception sce){
        	   sce.printStackTrace();
           }
           try{
               sleep(REPORT_DELAY);
           }catch(Exception e){
        	   e.printStackTrace();
        	   break;
           }
       }
    }
}

