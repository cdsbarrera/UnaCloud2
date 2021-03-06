package unacloud

/**
 * 
 * @author Cesar
 * 
 * Representation of network interface in a virtual machine
 */
class NetInterface {
	
	//-----------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------
	
	/**
	 * Ip configure in this interface
	 */
	ExecutionIP ip
	
	/**
	 * name of the interface
	 */
	String name
	
	/**
	 * representation of Deployment
	 */
	
	static belongsTo = [virtualExecution:VirtualMachineExecution]

    static constraints = {
    }
}
