package apiAccessControlUnit;

public class CommandParser {
	public static String[] getCommands(String commandLine) throws Exception{
		String[] args = commandLine.split("\\s");
		if(args[0].equals("Create")) {
			if(args.length != 3) {
				throw new Exception("Number of passed arguments does not match the required number of arguments");
			}
		}
		else if(args[0].equals("Limit")) {
			if(args.length != 3) {
				throw new Exception("Number of passed arguments does not match the required number of arguments");
			}
			try {
				Integer.parseInt(args[2]);
			}catch(NumberFormatException e) {
				throw new Exception("Invalid Argument Value For Command");
			}
		}
		else if(args[0].equals("LimitUsr")) {
			if(args.length != 4) {
				throw new Exception("Number of passed arguments does not match the required number of arguments");
			}
			try {
				Integer.parseInt(args[3]);
			}catch(NumberFormatException e) {
				throw new Exception("Invalid Argument Value For Command");
			}
		}
		else if(args[0].equals("Call")) {
			if(args.length != 3) {
				throw new Exception("Number of passed arguments does not match the required number of arguments");
			}			
		}
		else if(args[0].equals("ShowMaxHit")) {
			if(args.length != 2) {
				throw new Exception("Number of passed arguments does not match the required number of arguments");
			}		
		}
		return args;
	}
}
