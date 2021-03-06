package unacloud

import unacloud.enums.ExternalCloudTypeEnum;

/**
 * 
 * @author Cesar
 *
 *Representation of cloud provider
 */
class ExternalCloudProvider {
	
	//-----------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------
	
	/**
	 * Name of the provider
	 */
	String name
	
	String endpoint
	
	/**
	 * Type of provider (COMPUTING, STORAGE)
	 */
	ExternalCloudTypeEnum type
	
	/**
	 * hardwareProfiles: list of hardwareProfiles to deploy machines in case of Type is Computing
	 * 
	 * accounts: list of accounts used by users
	 */
	static hasMany = [hardwareProfiles: HardwareProfile, accounts:ExternalCloudAccount]
		
    static constraints = {
    }
}
