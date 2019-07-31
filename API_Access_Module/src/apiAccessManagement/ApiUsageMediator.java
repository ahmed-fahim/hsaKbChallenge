package apiAccessManagement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;

public class ApiUsageMediator {
	/**
	 * The approach of this class is to have loose coupling in between 
	 * "global access handling" and "per user based access handling"
	 */
	
	private TreeMap<String, String> apiUriToName; //A map to get API Name from API Uri
	private HashSet<String> apiNameList;		  //A set to keep track of all valid API Names
	private HashMap<String, UserApiUsageHandler> apiUserHandlers; //A list of all the Users encapsulated in UserApiUsageHandler
	private GlobalApiUsageHandler globalApiUsageHandler;	//A single object of GlobalApiUsageHandler
	private static final int DEFAULT_GLOBAL_LIMIT = 5;	//The determined default Global limit for any API newly created 
	private AccessCounterResetHandler resetHandler;	//A runnable object to run on parallel thread to reset access counter every 1 minute
	
	public ApiUsageMediator() {
		apiNameList = new HashSet<>();
		apiUriToName = new TreeMap<>();
		apiUserHandlers = new HashMap<>();
		globalApiUsageHandler = new GlobalApiUsageHandler();
		resetHandler = new AccessCounterResetHandler(apiUserHandlers, globalApiUsageHandler);
		resetHandler.start();
	}

	public synchronized void Create(String apiName, String apiUri) throws Exception {
		/**
		 * Attempts to create a new API with name = apiName and uri = apiUri
		 */
		synchronized (apiUriToName) {
			synchronized (apiNameList) {
				if (apiUriToName.containsKey(apiUri) || apiNameList.contains(apiName)) {
					//if any of the value is preExisting
					throw new Exception("Attempted to create Api with duplicate Uri or duplicate Name");
				} else {
					//Creating a new URI->Name mapping in the table
					apiUriToName.put(apiUri, apiName);
					//Adding the new API Name to the set
					apiNameList.add(apiName);
				}
			}
		}

		synchronized (globalApiUsageHandler) {
			try {
				//setting the default limit
				globalApiUsageHandler.setAccessLimitForApi(apiName, DEFAULT_GLOBAL_LIMIT);
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public synchronized boolean isApiNameValid(String apiName) {
		/**
		 * An utility method to verify if api name is valid
		 */
		synchronized (apiNameList) {
			return apiNameList.contains(apiName);
		}
	}

	public synchronized void Limit(String apiName, int number) throws Exception {
		/**
		 * An api to set global limit (for total of users) for an API
		 */
		if (!isApiNameValid(apiName)) {
			throw new Exception("apiName is Invalid");
		}
		synchronized (globalApiUsageHandler) {
			try {
				globalApiUsageHandler.setAccessLimitForApi(apiName, number);
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public synchronized void Limit(String userName, String apiName, int number) throws Exception {
		/**
		 * An api to set limit for an API for a particular user
		 */
		
		if (!isApiNameValid(apiName)) {
			throw new Exception("apiName is Invalid");
		}
		synchronized (apiUserHandlers) {
			UserApiUsageHandler currentUserHandler;
			
			if (apiUserHandlers.containsKey(userName)) {
				currentUserHandler = apiUserHandlers.get(userName);
			} else {
				currentUserHandler = new UserApiUsageHandler(userName);
			}
			
			try {
				currentUserHandler.setAccessLimitForApi(apiName, number);
				apiUserHandlers.put(userName, currentUserHandler);
			} catch (Exception e) {
				throw e;
			}
		}
	}
	
	public synchronized String getNameForUri(String apiUri) throws Exception{
		/**
		 * An api to get API Name for an URI
		 */
		synchronized(apiUriToName) {
			if(apiUriToName.containsKey(apiUri)) {
				return apiUriToName.get(apiUri);
			}else {
				throw new Exception("No API matches given URI");
			}
		}
	}
	
	public synchronized boolean Call(String userName, String apiUri) throws Exception {
		/**
		 * An api to process the Call command.
		 * returns true upon successful access.
		 * returns false upon restricted access.
		 * throws Exception for invalid cases.
		 */
		String apiName = "";
		try{
			apiName = getNameForUri(apiUri);
		} catch(Exception e) { throw e; }
		
		synchronized(apiUserHandlers) {
			if(apiUserHandlers.containsKey(userName)) {
				//if userName exists
				synchronized(globalApiUsageHandler) {
					UserApiUsageHandler currentUser = apiUserHandlers.get(userName);
					if(currentUser.isAccessAllowedForApi(apiName)
						&& globalApiUsageHandler.isAccessAllowedForApi(apiName))
					{
						//if both the user and the system is allowing to access the api for this user
						currentUser.addUsageCountForApi(apiName);
						globalApiUsageHandler.addUsageCountForApi(apiName);
						apiUserHandlers.replace(userName, currentUser);
						return true;
					} else {
						return false;
					}
				}
			} else {
				throw new Exception("User does not exist");
			}
		}
	}
	
	public synchronized void getMaxHitCountForApi(String apiName, String[] result) throws Exception {
		/**
		 * Api to return maximum hit for an Api by any user.
		 * This api fills the argument result array with the answer
		 */
		if (!isApiNameValid(apiName)) {
			throw new Exception("apiName is Invalid");
		}
		
		synchronized (apiUserHandlers) {
			//if no user exists yet
			if(apiUserHandlers.size() == 0) {
				throw new Exception("Not Enough Registered User");
			}
			
			ArrayList<UserApiUsageHandler> userList = new ArrayList<>();
			for (UserApiUsageHandler user : apiUserHandlers.values()) {
				user.setComparator(apiName);
				userList.add(user);
			}
			
			//Sort with O(nLogn) time complexity
			Collections.sort(userList);
			
			//After sort, according to our implemented compareTo() method
			//the biggest value would be at the end of the list
			String maxHitterUser = userList.get(userList.size() - 1).getUserName();
			String maxHitCountStr = Integer.toString(userList.get(userList.size() - 1).getTotalHitCountForApi(apiName));
			result[0] = maxHitterUser;
			result[1] = maxHitCountStr;
			return;
		}
	}
}
