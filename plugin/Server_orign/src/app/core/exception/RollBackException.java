package app.core.exception;

public class RollBackException extends Exception {
	private String errMsg;
	
	public RollBackException(){
		super();             // call superclass constructor
	    this.errMsg = "unknown";
	}
	
	public RollBackException(String errMsg){
		super(errMsg);
		this.errMsg = errMsg; 
	}
	
	public String getError(){
		return errMsg;
	}
	
}
