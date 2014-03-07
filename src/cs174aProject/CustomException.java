package cs174aProject;

public class CustomException extends Exception{

	String error;
	
	public CustomException()
	{
		super();
		error = "unknown";
	}
	
	public CustomException(String err)
	{
		super(err);
		error = err;
	}
	
	public String getError()
	{
		return error;
	}
}
