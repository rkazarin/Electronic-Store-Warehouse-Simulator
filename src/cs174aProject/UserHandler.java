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
	public abstract boolean processInput();
	
}
