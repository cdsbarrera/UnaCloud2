package unacloud

import java.util.concurrent.TimeUnit
import java.text.DateFormat
import java.text.SimpleDateFormat

import unacloud.enums.IPEnum;
import unacloud.share.enums.VirtualMachineExecutionStateEnum;

class VirtualMachineExecution {
	
	//-----------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------
	
	/**
	 * Virtual machine hostname
	 */
    String name
	
	/**
	 * Virtual machine number of processors
	 */
	HardwareProfile hardwareProfile
	
	/**
	 * Net interfaces
	 */
	static hasMany = [interfaces:NetInterface]
	
	/**
	 * Date when the node was started
	 */
	Date startTime
	
	/**
	 * Date when the node was stopped
	 */
	Date stopTime
	
	/**
	 * Actual node state (QUEUED,COPYING,CONFIGURING,DEPLOYING,DEPLOYED,FAILED,FINISHED)
	 */
	VirtualMachineExecutionStateEnum status
	
	/**
	 * Virtual Machine interface message
	 */
	String message
	
	/**
	 * Physical machine where the node is or was deployed
	 */
	PhysicalMachine executionNode
	
	/**
	 * 
	 * Monitoring system configure in virtual execution
	 */
	MonitorSystem monitorSystem
	
	/**
	 * deployed Image 
	 */
	static belongsTo = [deployImage: DeployedImage]
	
	/**
	 * Last report of virtual machine
	 */
	Date lastReport
	
	
	static constraints = {
		executionNode nullable: true
		monitorSystem nullable: true
		stopTime nullable: true 
		startTime nullable:true
		lastReport nullable:true
	}
	
	//-----------------------------------------------------------------
	// Methods
	//-----------------------------------------------------------------
	
	/**
	 * Calculates and returns the remaining execution time (00h:00m:00s format)
	 * @return formated remaining time
	 */
	def remainingTime(){
		if(stopTime==null||status!=VirtualMachineExecutionStateEnum.DEPLOYED)return '--'
		long millisTime=(stopTime.getTime()-System.currentTimeMillis())/1000
		String s = ""+millisTime%60;
        String m = ""+((long)(millisTime/60))%60;
        String h = ""+((long)(millisTime/60/60));
        if(s.length()==1)s="0"+s;
        if(m.length()==1)m="0"+m;
        return h+"h:"+m+"m:"+s+"s"
	}
	
	/**
	 * Calculates and returns the remaining execution time in hours
	 * @return hours of execution remaining
	 */
	int runningTimeInHours(){
		long millisTime=(stopTime.getTime()-startTime.getTime())/1000
		return (millisTime/60/60)+1
	}
	
	/**
	 * Save entity with netinterfaces
	 * @return
	 */
	def saveExecution(){		
		this.save(failOnError:true,flush:true)
		for(NetInterface netInterface in interfaces){
			netInterface.save(failOnerror:true,flush:true)
		}
	}
	
	/**
	 * Set status to finished and breaks free IP from net interfaces.
	 * @return
	 */
	def finishExecution(){
		this.putAt("status", VirtualMachineExecutionStateEnum.FINISHED)
		this.putAt("stopTime", new Date())
		for(NetInterface netinterface in interfaces)
			netinterface.ip.putAt("state",IPEnum.AVAILABLE)
	}
	
	/**
	 * Return main IP configured in Net interfaces
	 * @return
	 */
	def mainIp(){
		return interfaces.getAt(0).ip
	}
	
	/**
 	 * Return time of last state from node
	 * @return
	 */
	def Date getLastStateTime(){
		def requestExec = ExecutionRequest.find("from ExecutionRequest as e where e.execution = ? and status = ? ",[this,status],[sort:'requestTime', order: "desc"])
		if(requestExec)return requestExec.requestTime
		else new Date();
	}
	
	/**
	 * Returns database id
	 * @return
	 */
	def Long getDatabaseId(){
		return id;
	}
	
	/**
	 * Method used to validates if execution must show its details based in status
	 * @return
	 */
	def boolean showDetails(){
		return status.equals(VirtualMachineExecutionStateEnum.DEPLOYED)||status.equals(VirtualMachineExecutionStateEnum.RECONNECTING)||status.equals(VirtualMachineExecutionStateEnum.FAILED)
	}
}
