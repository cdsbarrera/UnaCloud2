package unacloud.share.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import unacloud.share.entities.PhysicalMachineEntity;
import unacloud.share.enums.PhysicalMachineStateEnum;

/**
 * Generic class used to process queries and updates on PhysicalMachine entity 
 * @author Cesar
 *
 */
public class PhysicalMachineManager {
	
	/**
	 * Return a PhysicalMachine entity requested by id
	 * @param id physical machine id
	 * @return physical machine entity
	 */
	public static PhysicalMachineEntity getPhysicalMachine(Long id, PhysicalMachineStateEnum machineState,Connection con){
		try {
			PreparedStatement ps = con.prepareStatement("SELECT pm.id, i.ip, pm.state, pm.last_report FROM physical_machine pm INNER JOIN ip i ON pm.ip_id = i.id WHERE pm.state = ? and pm.id = ?;");
			ps.setString(1, machineState.name());
			ps.setLong(2, id);
			System.out.println(ps.toString());
			ResultSet rs = ps.executeQuery();	
			PhysicalMachineEntity machine = null;
			if(rs.next())machine = new PhysicalMachineEntity(rs.getLong(1), rs.getString(2),new java.util.Date(rs.getTimestamp(4).getTime()), PhysicalMachineStateEnum.getEnum(rs.getString(3)));
			try{rs.close();ps.close();}catch(Exception e){}
			return machine;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	/**
	 * Return a list of all ON physical machines requested by parameters
	 * @param idList
	 * @return
	 */
	public static List<PhysicalMachineEntity> getPhysicalMachineList(Long[] idList, PhysicalMachineStateEnum machineState,Connection con){
		if(idList.length==0)return null;
		try {
			List<PhysicalMachineEntity> list = new ArrayList<PhysicalMachineEntity>();
			StringBuilder builder = new StringBuilder();
			for(@SuppressWarnings("unused") Long pm: idList){
				builder.append("?,");
			}
			String query = "SELECT pm.id, i.ip, pm.state, pm.last_report FROM physical_machine pm INNER JOIN ip i ON pm.ip_id = i.id WHERE pm.state = ? and pm.id in ("+builder.deleteCharAt( builder.length() -1 ).toString()+");";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, machineState.name());
			int index = 2;
			for(Long idpm: idList){
				ps.setLong(index++, idpm);
			}
			System.out.println(ps.toString());
			ResultSet rs = ps.executeQuery();		
			while(rs.next())list.add(new PhysicalMachineEntity(rs.getLong(1), rs.getString(2), new java.util.Date(rs.getTimestamp(4).getTime()), PhysicalMachineStateEnum.getEnum(rs.getString(3))));
			try{rs.close();ps.close();}catch(Exception e){}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Return a list of all ON physical machines
	 * @param idList
	 * @return
	 */
	public static List<PhysicalMachineEntity> getAllPhysicalMachine(PhysicalMachineStateEnum machineState,Connection con){		
		try {
			List<PhysicalMachineEntity> list = new ArrayList<PhysicalMachineEntity>();
			String query = "SELECT pm.id, i.ip, pm.state, pm.last_report FROM physical_machine pm INNER JOIN ip i ON pm.ip_id = i.id WHERE pm.state = ? ;";
			PreparedStatement ps = con.prepareStatement(query);			
			ps.setString(1, machineState.name());
			System.out.println(ps.toString());
			ResultSet rs = ps.executeQuery();		
			while(rs.next())list.add(new PhysicalMachineEntity(rs.getLong(1), rs.getString(2), new java.util.Date(rs.getTimestamp(4).getTime()), PhysicalMachineStateEnum.getEnum(rs.getString(3))));
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Update a physical machine entity on database.
	 * @param machine
	 * @return
	 */
	public static boolean setPhysicalMachine(PhysicalMachineEntity machine, Connection con){
		if(machine.getId()==null||machine.getId()<1)return false;
		try {
			String query = "update physical_machine pm set"; 
			int status = 0;
			int report = 0;
			if(machine.getStatus()!=null){query+=" pm.state = ? ";status = 1;}
			if(machine.getLastReport()!=null){query+=(status>0?",":"")+" pm.last_report = ? ";report=status+1;};
			if(status>0||report>0){
				query += "where pm.id = ? and pm.id > 0;";
				PreparedStatement ps = con.prepareStatement(query);
				int id = 1;
				if(status>0){ps.setString(status, machine.getStatus().name());id++;};
				if(report>0){ps.setTimestamp(report, new Timestamp(machine.getLastReport().getTime()));id++;};
				ps.setLong(id, machine.getId());
				System.out.println(ps.toString());
				System.out.println("Change "+ps.executeUpdate()+" lines");
				try{ps.close();}catch(Exception e){}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}		
		return false;
	}

}
