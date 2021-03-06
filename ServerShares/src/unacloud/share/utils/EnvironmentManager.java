package unacloud.share.utils;


/**
 * Class used to manage variables in environment to autoconfigure server projects
 * @author Cesar
 *
 */
public class EnvironmentManager {
	
	public static final String PATH_CONFIG = "PATH_CONFIG";
	
	/**
	 * Return the path where is located config file for project. In case variable is not define 
	 * use root path (/)
	 * @return
	 */
	public static String getConfigPath(){
		String path = System.getenv().get(PATH_CONFIG);
		if(path==null)return "";
		return path;
	}

}
