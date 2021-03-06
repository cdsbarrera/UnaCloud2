package hypervisorManager;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import reportManager.ServerMessageSender;

import com.andes.enums.VirtualMachineExecutionStateEnum;

import exceptions.VirtualMachineExecutionException;
import virtualMachineManager.PersistentExecutionManager;
import virtualMachineManager.configuration.AbstractVirtualMachineConfigurator;
import virtualMachineManager.entities.Image;
import virtualMachineManager.entities.VirtualMachineExecution;
import virtualMachineManager.entities.VirtualMachineImageStatus;

public class ImageCopy implements Serializable{
	
	private static final long serialVersionUID = 8911955514393569155L;
	
	//String virtualMachineName;
	/**
	 * executable file name
	 */
	File mainFile;
	/**
	 * Original image
	 */
	Image image;
	
	transient VirtualMachineImageStatus status=VirtualMachineImageStatus.FREE;
	
	/**
	 * Getters and setters
	 */
	public File getMainFile() {
		return mainFile;
	}
	public void setMainFile(File mainFile) {
		this.mainFile = mainFile;
	}
	public String getVirtualMachineName() {
		if(mainFile==null)return "null";
		String h=mainFile.getName();
		int l=h.lastIndexOf(".");
		if(l==-1)return h;
		return h.substring(0,l);
	}
	public VirtualMachineImageStatus getStatus() {
		return status;
	}
	public void setStatus(VirtualMachineImageStatus status) {
		this.status = status;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
	/**
	 * Configures and starts the copy
	 * @param machineExecution
	 */
	
	public synchronized void configureAndStart(VirtualMachineExecution machineExecution){
		Hypervisor hypervisor=HypervisorFactory.getHypervisor(getImage().getHypervisorId());
		try {
			try {
				if(hypervisor==null)throw new Exception("Hypervisor not found "+getImage().getHypervisorId());
				if(status!=VirtualMachineImageStatus.STARTING)status=VirtualMachineImageStatus.STARTING;
				Class<?> configuratorClass=Class.forName("virtualMachineManager.configuration."+getImage().getConfiguratorClass());
				Object configuratorObject=configuratorClass.getConstructor().newInstance();
				if(configuratorObject instanceof AbstractVirtualMachineConfigurator){
					AbstractVirtualMachineConfigurator configurator=(AbstractVirtualMachineConfigurator)configuratorObject;
					//configurator.setHypervisor(hypervisor);
					configurator.setExecution(machineExecution);
					//TODO Evaluar si hacerlo en el apagado porque es mas importante el tiempo de arranque.
					hypervisor.registerVirtualMachine(this);
	    			hypervisor.restoreVirtualMachineSnapshot(this,"unacloudbase");
	        		hypervisor.configureVirtualMachineHardware(machineExecution.getCores(),machineExecution.getMemory(),this);
	    			hypervisor.startVirtualMachine(this);
	    			configurator.configureHostname();
	    			configurator.configureIP();
	    			System.out.println("image config "+new Date());
	    	        PersistentExecutionManager.startUpMachine(machineExecution,!configurator.doPostConfigure());	    	       
				}else {
					ServerMessageSender.reportVirtualMachineState(machineExecution.getId(), VirtualMachineExecutionStateEnum.FAILED,"Invalid virtual machine configurator.");
					status=VirtualMachineImageStatus.FREE;
				}
				
			} catch (Exception e) {
				e.printStackTrace(System.out);
				ServerMessageSender.reportVirtualMachineState(machineExecution.getId(), VirtualMachineExecutionStateEnum.FAILED,"Configurator class error: "+e.getMessage());
				status=VirtualMachineImageStatus.FREE;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Clones the copy 
	 * @param dest empty image copy
	 * @return cloned image
	 */
	public synchronized ImageCopy cloneCopy(ImageCopy dest){
		Hypervisor hypervisor=HypervisorFactory.getHypervisor(this.getImage().getHypervisorId());
		hypervisor.cloneVirtualMachine(this,dest);
		return dest;
	}
	
	/**
	 * Makes initialization process in copy before starting
	 * @throws VirtualMachineExecutionException
	 */
	public synchronized void init()throws VirtualMachineExecutionException{
		Hypervisor hypervisor=HypervisorFactory.getHypervisor(image.getHypervisorId());
		if(hypervisor==null)throw new VirtualMachineExecutionException("Hypervisor doesn't exists on machine. Hypervisor was "+image.getHypervisorId());
		hypervisor.registerVirtualMachine(this);
		try {
			hypervisor.changeVirtualMachineMac(this);
			hypervisor.takeVirtualMachineSnapshot(this,"unacloudbase");
		} catch (HypervisorOperationException e) {
			e.printStackTrace();
		}
		hypervisor.unregisterVirtualMachine(this);
	}
	
	/**
	 * Delete the snapshot of this image to save it with current state and files
	 * @throws VirtualMachineExecutionException
	 * @throws HypervisorOperationException 
	 */
	public synchronized void deleteSnapshot()throws VirtualMachineExecutionException, HypervisorOperationException{
		Hypervisor hypervisor=HypervisorFactory.getHypervisor(this.getImage().getHypervisorId());
		hypervisor.deleteVirtualMachineSnapshot(this,"unacloudbase");		
	}

	
	/**
	 * Starts copy
	 * @throws HypervisorOperationException
	 */
	public void startVirtualMachine()throws HypervisorOperationException{
		Hypervisor hypervisor=HypervisorFactory.getHypervisor(this.getImage().getHypervisorId());
		hypervisor.startVirtualMachine(this);
	}
    
	/**
	 * Executes given command on this copy
	 * @param command command to get executed
	 * @param args command arguments
	 * @throws HypervisorOperationException
	 */
    public void executeCommandOnMachine( String command,String...args) throws HypervisorOperationException{
    	Hypervisor hypervisor=HypervisorFactory.getHypervisor(image.getHypervisorId());
    	hypervisor.executeCommandOnMachine(this,command,args);
    }
    
    /**
     * copies a file in this image clone
     * @param destinationRoute destination path in the VM
     * @param sourceFile original file
     * @throws HypervisorOperationException
     */
    public void copyFileOnVirtualMachine(String destinationRoute, File sourceFile) throws HypervisorOperationException{
    	Hypervisor hypervisor=HypervisorFactory.getHypervisor(image.getHypervisorId());
    	hypervisor.copyFileOnVirtualMachine(this,destinationRoute,sourceFile);
    }
    
    /**
     * Restarts this VM
     * @throws HypervisorOperationException
     */
    public void restartVirtualMachine() throws HypervisorOperationException{
    	Hypervisor hypervisor=HypervisorFactory.getHypervisor(this.getImage().getHypervisorId());
		hypervisor.restartVirtualMachine(this);
    }
    
    /**
     * Finalizes copy execution
     */
    public synchronized void stopAndUnregister(){
    	Hypervisor hypervisor=HypervisorFactory.getHypervisor(image.getHypervisorId());
    	hypervisor.stopAndUnregister(this);
    }
    /**
     * Finalizes copy execution without unregistering and freeing image
     */
    public synchronized void stop(){
    	Hypervisor hypervisor=HypervisorFactory.getHypervisor(image.getHypervisorId());
    	hypervisor.stopVirtualMachine(this);
    }
    /**
     * Unregistering image copy
     */
    public synchronized void unregister(){
    	Hypervisor hypervisor=HypervisorFactory.getHypervisor(image.getHypervisorId());
    	hypervisor.unregisterVirtualMachine(this);
    }
}