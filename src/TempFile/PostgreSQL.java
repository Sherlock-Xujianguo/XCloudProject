
import java.sql.*;

public class PostgreSQL{
	private Connection connect = null;
	private Statement statement = null;
	
	String url = "jdbc:postgresql://localhost/postgres";
	String userName = "postgres";
	String PassWord = "112358";
	
	PostgreSQL(String url,String userName,String PassWord) {
		// TODO Auto-generated constructor stub
		try {
			this.url = url;
			this.userName = userName;
			this.PassWord = PassWord;
		
			Class.forName("org.postgresql.Driver");
			connect = DriverManager.getConnection(url, userName, PassWord);
			
			connect.setAutoCommit(false);			//改为手动提交方式进行提交
		}catch(Exception e) {
			System.out.println("connect error");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Opened successfully");
	}
	
	PostgreSQL(){
		this("jdbc:postgresql://localhost/postgres","postgres","112358");
	}
	
	
	public void StatementClose() {
		try {
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		statement = null;
	}
	
	public int SQLUpdataGeneral(String SQL) {
		if(connect == null) {System.out.println("Connect error!");return -1;}
		
		int num = 0;
		
		try {
			statement = connect.createStatement();
			num = statement.executeUpdate(SQL);
			connect.commit();					//手动提交更新

		}catch(Exception e) {
			System.out.println("Update Error");
			
			try {
				connect.commit();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			return -1;
		}
		return num;
	}
	
	public ResultSet SQLSearchGenral(String SQL,ResultSet result) {
		try {
			statement = connect.createStatement();
			result = statement.executeQuery(SQL);
			
		}catch(SQLException e) {
			System.out.println("Error");
			e.printStackTrace();
			try {
				connect.commit();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		return result;
	}
	
	public void CreatNewTable(String NewTableName,String Keys) {
		if(connect == null) {System.out.println("Connect error!");return ;}
		
		String Sql = "CREATE TABLE "+NewTableName+" ("+Keys+"); ALTER TABLE "+NewTableName+" OWNER TO postgres;";
		this.SQLUpdataGeneral(Sql);	
		
	}
	
	public void Insert(String TableName,String InsertCommend) {
		if(connect == null) {System.out.println("Connect error");return ;}
		
		String Sql = "INSERT INTO "+TableName+" "+InsertCommend+";";
		this.SQLUpdataGeneral(Sql);
		
	}
	
	public int Update(String TableName,String UpdateCommend) {
		if(connect == null) {System.out.println("Connect error");return -1;}
		String Sql = "UPDATE "+TableName+" "+UpdateCommend+";";
		
		int updateNum = this.SQLUpdataGeneral(Sql);
		
		return updateNum;
		
	}
	
	public ResultSet Search(String TableName,String SearchFor,String Condiction) {
		if(connect == null) {System.out.println("Connect error");return null;}
		
		String Sql = "SELECT "+SearchFor+ " FROM "+TableName+" "+Condiction+" ;";
		
		ResultSet result = null; 
		try {
			result = this.SQLSearchGenral(Sql, result);
			//result.next();	
			//result.first();
		}catch(Exception e) {
			return null;
		}
		
		return result;
	}
	
	public int Delete(String TableName,String DeleteCommend) {
		if(connect == null) {System.out.println("Connect error");return -1;}
		
		String Sql = "DELETE FROM "+TableName+" "+DeleteCommend+" ;";
		return this.SQLUpdataGeneral(Sql);
	}
	
	public void Close() {
		try {
			this.connect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		PostgreSQL PSQL = new PostgreSQL();
		
		//PSQL.CreatNewTable("TestUser", "FileName text NOT NULL,Filelength text,FileGetTime text,FileDES text,FileIsUsing boolean");
		
		//ResultSet result = PSQL.Search("TEST", "NAME,INSCHOOL", "WHERE AGE = 18");
		
		//try {
		//	while(result.next()) {
		//		System.out.println("NAME: "+result.getString("NAME")+"INSCHOOL: "+result.getBoolean("INSCHOOL"));
		//	}
		//	PSQL.StatementClose();
		//} catch (SQLException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		//PSQL.Insert("TEST", "(ID,NAME,AGE,INSCHOOL) VALUES (5,'E',19,TRUE)");
		
		//PSQL.Delete("TEST", "WHERE NAME = 'B'");
		
		//PSQL.Update("TEST", "SET INSCHOOL = TRUE WHERE NAME = 'D'");
		
	}
}