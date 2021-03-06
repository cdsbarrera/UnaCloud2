package unacloud.share.enums;

public enum ServerVariableProgramEnum {
	
	AGENT,
	CONTROL,
	FILE_MANAGER,
	WEB,
	SERVER;
	
	public static ServerVariableProgramEnum getEnum(String type){
		if(type.equals(AGENT.name()))return AGENT;
		if(type.equals(CONTROL.name()))return CONTROL;
		if(type.equals(FILE_MANAGER.name()))return FILE_MANAGER;
		if(type.equals(WEB.name()))return WEB;
		if(type.equals(SERVER.name()))return SERVER;
		return null;
	}

}
