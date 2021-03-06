package uniandes.unacloud.communication;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import unacloud.share.db.DeploymentManager;
import unacloud.share.db.HypervisorManager;
import unacloud.share.entities.HypervisorEntity;
import unacloud.share.entities.VirtualMachineExecutionEntity;
import unacloud.share.enums.VirtualMachineExecutionStateEnum;
import unacloud.share.enums.VirtualMachineImageEnum;
import uniandes.unacloud.FileManager;
import uniandes.unacloud.db.VirtualMachineImageManager;
import uniandes.unacloud.db.entities.VirtualImageFileEntity;

/**
 * File receiver task
 * @author Cesar
 *
 */
public class FileReceiverTask implements Runnable{
	Socket s;	
	public FileReceiverTask(Socket s) {
		System.out.println("Attending "+s.getRemoteSocketAddress());
		this.s = s;
	}
	@Override
	public void run() {
		String mainFile=null;
		String newMainFile = null;
		String message;
		try(Socket ss=s; DataInputStream is = new DataInputStream(s.getInputStream());Connection con = FileManager.getInstance().getDBConnection();) {
			Long execution = is.readLong();
			String token= is.readUTF();
			System.out.println("\tRequest " +execution+" - "+ token);
			VirtualImageFileEntity image = VirtualMachineImageManager.getVirtualImageWithFile(token, con);
			System.out.println("\tImage requested " + image);	
			if (image!=null) {
				mainFile=image.getMainFile().substring(0,image.getMainFile().lastIndexOf(java.io.File.separatorChar)+1);
				System.out.println("save in path: "+mainFile);
				try(ZipInputStream zis = new ZipInputStream(is)) {
					System.out.println("\tZip open");
					final byte[] buffer = new byte[1024 * 100];
					// for(ZipEntry entry;(entry=zis.getNextEntry())!=null;){
					TreeMap<File, String>filesTemp = new TreeMap<File, String>();
					List<HypervisorEntity>hypervisors = HypervisorManager.getAllHypervisors(con);				
					
					for (ZipEntry entry; (entry = zis.getNextEntry()) != null;) {
						boolean goodExtension = false;
						String mainExtension = null;
						System.out.println("\t\tFile: " + entry.getName());
						for(HypervisorEntity hyperv : hypervisors)
							if(hyperv.validatesExtension(entry.getName())){		
								goodExtension = true;
								mainExtension = hyperv.getExtension();
								break;
							}	
						if(goodExtension){
							File tempFile = File.createTempFile(entry.getName(), null);
							try (FileOutputStream fos = new FileOutputStream(tempFile)) {
								for (int n; (n = zis.read(buffer)) != -1;) {
									fos.write(buffer, 0, n);
								}
								if (entry.getName().endsWith(mainExtension)){
									newMainFile=mainFile + entry.getName();								
								}								
							}	
							System.out.println("Save temp "+tempFile);
							filesTemp.put(tempFile,entry.getName());
						}
						zis.closeEntry();
					}
					System.out.println("There are "+filesTemp.size()+", Delete old files");
					Long sizeImage= 0l;
					if(filesTemp.size()>0){						
						File dir = new File(mainFile);
						System.out.println(dir);
						for(java.io.File f:dir.listFiles())if(f.isFile())f.delete();
						System.out.println("Save new files");
						for(File temp:filesTemp.descendingKeySet()){
							File newFile = new File(mainFile, filesTemp.get(temp));
							System.out.println("save: "+newFile);
							try (FileInputStream streamTemp = new FileInputStream(temp);FileOutputStream ouFile = new FileOutputStream(newFile)) {
								for (int n; (n = streamTemp.read(buffer)) != -1;) {
									ouFile.write(buffer, 0, n);
								}															
							}	
							sizeImage+= temp.getTotalSpace();
							temp.delete();
						}
					}				
					
					System.out.println("reception finished: "+newMainFile);
					try {
						VirtualMachineImageManager.setVirtualMachineFile(new VirtualImageFileEntity(image.getId(), VirtualMachineImageEnum.AVAILABLE, null, null, null, sizeImage, newMainFile, null, null), false, con);
						message = "Image has been saved in server";
						System.out.println("Status changed, process closed");
					} catch (Exception e) {
						e.printStackTrace();
						 message = e.getMessage();
					}
					
				} catch (Exception e) {		
				    e.printStackTrace();
				    VirtualMachineImageManager.setVirtualMachineFile(new VirtualImageFileEntity(image.getId(), VirtualMachineImageEnum.UNAVAILABLE, null, null, null, null, null, null, null), false, con);
				    message = e.getMessage();
				}	
				DeploymentManager.setVirtualMachineExecution(new VirtualMachineExecutionEntity(execution, 0, 0, null, new Date(), null, VirtualMachineExecutionStateEnum.FINISHED, null, message), con);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}