package apiAccessControlUnit;

import apiAccessManagement.ApiUsageMediator;
import java.util.*;


public class MasterController {
	/**
	 * Main Controller class to receive API Calls and dispatch calls
	 */
	private ApiUsageMediator apiMgr;
	private static Scanner inp;
	
	public MasterController() {
		apiMgr = new ApiUsageMediator();
		inp = new Scanner(System.in);
	}
	public static void main(String[] args) {
		MasterController masterControl = new MasterController();
		String commandLine;
		while(true) {
			System.out.println("$Command:\n");
			commandLine = inp.nextLine();
			String[] arg; 
			try{
				//validating commands before attempting to execute
				arg = CommandParser.getCommands(commandLine);
			}catch(Exception e) {
				System.out.println(e.toString());
				continue;
			}
			masterControl.executeTask(arg);
			
		}
	}
	//dispatcher for Api Access Related operations
	//Retrieves a separate thread for any action on ApiUsageMediator
	public void executeTask(String[] args){
		Thread executionThread;
		
		if(args[0].equals("Create")) {
			executionThread = dispatchCreate(args);
		}
		else if(args[0].equals("Limit")) {
			executionThread = dispatchLimit(args);
		}
		else if(args[0].equals("LimitUsr")) {
			executionThread = dispatchLimitUsr(args);
		}
		else if(args[0].equals("Call")) {
			executionThread = dispatchCall(args);
		}
		else if(args[0].equals("ShowMaxHit")) {
			executionThread = dispatchShowMaxHit(args);	
		}
		else {
			executionThread = dispatchUnknownCommand();
		}
		executionThread.start();
	}

	/* Dispatch Methods Start */
	//Their Task is to create a new Thread for any activity for ApiAccessModule
	private Thread dispatchCreate(String[] args) {
		return new Thread() {
			@Override
			public void run() {
				try{
					apiMgr.Create(args[1], args[2]);
				}catch(Exception e) {
					System.out.println(e.toString());
					return;
				}
				System.out.println("Api "+args[0] + " executed successfully");
			}
		};
	}
	private Thread dispatchLimit(String[] args) {
		return new Thread() {
			@Override
			public void run() {
				try{
					apiMgr.Limit(args[1], Integer.parseInt(args[2]));
				}catch(Exception e) {
					System.out.println(e.toString());
					return;
				}
				System.out.println("Api "+args[0] + " executed successfully");
			}
		};
	}
	private Thread dispatchLimitUsr(String[] args) {
		return new Thread() {
			@Override
			public void run() {
				try{
					apiMgr.Limit(args[1], args[2], Integer.parseInt(args[3]));
				}catch(Exception e) {
					System.out.println(e.toString());
					return;
				}
				System.out.println("Api "+args[0] + " executed successfully");
			}
		};
	}
	private Thread dispatchCall(String[] args) {
		return new Thread() {
			@Override
			public void run() {
				try{
					boolean resp = apiMgr.Call(args[1], args[2]);
					if(resp) {
						System.out.println("Api Uri"+ args[2] + " successfully accessed by " + args[1]);
					}else {
						System.out.println("Api Uri"+ args[2] + " restricted access for " + args[1]);
					}
				}catch(Exception e) {
					System.out.println(e.toString());
					return;
				}
			}
		};
	}
	private Thread dispatchShowMaxHit(String[] args) {
		return new Thread() {
			@Override
			public void run() {
				String[] result = new String[2];
				String apiName;
				try {
					apiName = apiMgr.getNameForUri(args[1]);
				} catch (Exception e) {
					System.out.println(e.toString());
					return;
				}
				
				try{
					apiMgr.getMaxHitCountForApi(apiName, result);
				}catch(Exception e) {
					System.out.println(e.toString());
					return;
				}
				
				System.out.println("API Name = " + apiName + ", Uri = " + args[1] + ", MaxHit = " + result[1] + ", User = " + result[0]);
			}
		};
	}
	
	private Thread dispatchUnknownCommand() {
		return  new Thread() {
			@Override
			public void run() {
				System.out.println("Unsupported command");
			}
		};
	}
	
	/* Dispatch Methods End */
}
