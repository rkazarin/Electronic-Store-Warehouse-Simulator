package cs174aProject;
import java.sql.*;


public abstract class UserHandler {

	boolean eMart_db_accessed, eDepot_db_accessed;
	DatabaseHandler myDB;
	String dbDescription, dbUser, dbPassword;
	
	public UserHandler()
	{
		eMart_db_accessed = false;
		eDepot_db_accessed = false;
	}
	public UserHandler(boolean eMart, boolean eDepot, String str_descriptor, String str_user, String str_pass)
	{
		eMart_db_accessed = eMart;
		eDepot_db_accessed = eDepot;
		dbDescription = str_descriptor;
		dbUser = str_user;
		dbPassword = str_pass;
		myDB = new DatabaseHandler(dbDescription,dbUser, dbPassword, eMart_db_accessed, eDepot_db_accessed);
	}

	public void displayResultSet(ResultSet my_rs)
	{
		try{
			ResultSetMetaData rsmd = my_rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			if(my_rs.next() == false){
				System.out.println("No Entries Selected.");
			}
			else{
				do{
					for(int j = 1; j <= numColumns; j++)
					{
							if(j > 1) System.out.print(",   ");
							String columnValue = my_rs.getString(j);
							System.out.print(rsmd.getColumnName(j) + ": " + columnValue);
					}
					System.out.println("");
				}while(my_rs.next() != false);
			}
			my_rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	public abstract boolean processInput();
	
}
