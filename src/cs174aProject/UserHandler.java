package cs174aProject;
import java.sql.*;


public abstract class UserHandler {

	boolean eMart_db_accessed, eDepot_db_accessed;
	DatabaseHandler myDB;
	
	public UserHandler()
	{
		eMart_db_accessed = false;
		eDepot_db_accessed = false;
	}
	public UserHandler(boolean eMart, boolean eDepot)
	{
		eMart_db_accessed = eMart;
		eDepot_db_accessed = eDepot;		
	}
	
	public void establishUserConnection(String str_descriptor, String str_user, String str_pass)
	{
		myDB = new DatabaseHandler(str_descriptor,str_user, str_pass, eMart_db_accessed, eDepot_db_accessed);
	}
	
}
