import java.util.*;
public class UUIDMake {
	
	public static String getNewUUID() {
		UUID uuid = UUID.randomUUID();
		System.out.println(uuid);
		String[] ids =uuid.toString().split("-");
		String id = "";
		for(int i = 0;i<ids.length;i++) {
			id = id+ids[i];
		}
		return id;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		UUIDMake idmake = new UUIDMake();
		System.out.println(idmake.getNewUUID());
		
	}

}
