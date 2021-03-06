package unacloud

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import unacloud.share.entities.HypervisorEntity;
import uniandes.unacloud.FileManager;
import uniandes.unacloud.db.UserManager;
import uniandes.unacloud.db.entities.UserEntity;
import unacloud.share.db.HypervisorManager;
import grails.converters.JSON

class FileController {
	
	//-----------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------
	
	/**
	 * Representation of image services
	 */
	
	FileService fileService
	
	
	//-----------------------------------------------------------------
	// Actions
	//-----------------------------------------------------------------

   /**
	 * Validates file parameters are correct and save new uploaded image. Redirects
	 * to index when finished or renders an error message if uploaded
	 * files are not valid.
	 *
	 */
	def upload(){
		def resp
		println params.token
		if(params.token&&!params.token.empty){
			if(request.multiFileMap&&request.multiFileMap.files&&request.multiFileMap.files.size()>0){				
				def files = request.multiFileMap.files
				boolean validate=true
				String mainExtension = null
				try{
					Connection con = FileManager.getInstance().getDBConnection()
					List<HypervisorEntity>hypervisors = HypervisorManager.getAllHypervisors(con);
					files.each {
						if(validate){
							if(it.isEmpty()){
								resp = [success:false,'message':'File cannot be empty.'];
								validate= false;
							}
							else{
								boolean goodExtension = false;
								def fileName=it.getOriginalFilename()
								for(HypervisorEntity hyperv in hypervisors)
								if(hyperv.validatesExtension(fileName)){
									goodExtension = true;
									mainExtension = hyperv.extension
									break;
								}
								if(!goodExtension){
									resp = [success:false,'message':'Invalid file type.']
									validate= false;
								}																						
							}							
						}					
					}
					con.close()
				}catch(Exception e) {
					validate=false;
					resp = [success:false,'message':e.message]
				}
				if(validate){
					try{
						def createPublic = fileService.upload(files, params.token,mainExtension)
						if(createPublic!=null){
							resp = [success:true,'redirect':'list','cPublic':createPublic];
						}else resp = [success:true,'redirect':'list'];
					}
					catch(Exception e) {
						resp = [success:false,'message':e.message]
					}
				}
			}else resp = [success:false,'message':'File(s) to upload is/are missing.'];
		}else resp = [success:false,'message':'All fields are required'];
		render resp as JSON
	}
	
	
	/**
	 * Validates file parameters are correct and save new files for the image.
	 * Redirects to index when finished or renders an error message if uploaded
	 * files are not valid.
	 */
	
	def updateFiles(){
		def resp
		if(params.token&&!params.token.empty){
			if(request.multiFileMap&&request.multiFileMap.files&&request.multiFileMap.files.size()>0){
				def files = request.multiFileMap.files
				println 'valid files'
				boolean validate=true
				String mainExtension = null
				try{
					Connection con = FileManager.getInstance().getDBConnection()
					List<HypervisorEntity>hypervisors = HypervisorManager.getAllHypervisors(con);
					files.each {
						if(validate){
							if(it.isEmpty()){
								resp = [success:false,'message':'File cannot be empty.'];
								validate= false;
							}
							else{								
								boolean goodExtension = false;
								def fileName=it.getOriginalFilename()
								for(HypervisorEntity hyperv in hypervisors)
								if(hyperv.validatesExtension(fileName)){		
									goodExtension = true;
									mainExtension = hyperv.extension
									break;
								}	
								if(!goodExtension){													
									resp = [success:false,'message':'Invalid file type.']
									validate= false;
								}
							}
						}					
					}
					con.close()
				}catch(Exception e) {
					validate=false;
					resp = [success:false,'message':e.message]
				}
				if(validate){
					fileService.updateFiles(files,params.token,mainExtension)
					resp = [success:true,'redirect':'../list']
				}
			}else{
				resp = [success:false,'message':'File(s) to upload is/are missing.'];		
			}
		}else{
			resp = [success:false,'message':'Error! image does not exist.'];	
		}
		render resp as JSON
	}
}
