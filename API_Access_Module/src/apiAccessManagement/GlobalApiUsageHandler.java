package apiAccessManagement;

import java.util.HashMap;

public class GlobalApiUsageHandler {
	private HashMap<String, Integer> apiAccessLimit, apiAccessCounter;

	public GlobalApiUsageHandler() {
		apiAccessLimit = new HashMap<>();
		apiAccessCounter = new HashMap<>();
	}

	public synchronized void setAccessLimitForApi(String apiName, int accessLimit) throws Exception {
		synchronized (apiAccessLimit) {
			if (apiName != null && apiName != "" && accessLimit >= 0) {
				apiAccessLimit.put(apiName, accessLimit);
			} else {
				throw new Exception("Invalid arguments");
			}
		}
	}

	public synchronized boolean isAccessAllowedForApi(String apiName) {
		synchronized (apiAccessLimit) {
			if (apiAccessLimit.containsKey(apiName)) {
				synchronized (apiAccessCounter) {
					// if limit exists for this apiName
					if (apiAccessCounter.containsKey(apiName)) {
						// if this apiName has been used at least once, it has been counted
						return apiAccessCounter.get(apiName) < apiAccessLimit.get(apiName);
					}
					// if limit exists and this api was never counted to be used in this minute,
					// then it is permitted
					return (apiAccessLimit.get(apiName) > 0);
				}
			}
			// if no limit exists for this apiName
			return false;
		}
	}

	public synchronized void addUsageCountForApi(String apiName) {
		synchronized (apiAccessCounter) {
			int prevCountForApi = 0;
			if (apiAccessCounter.containsKey(apiName)) {
				prevCountForApi = apiAccessCounter.get(apiName);
			}
			apiAccessCounter.put(apiName, prevCountForApi + 1);
		}
	}

	public synchronized void resetAccessCounter() {
		synchronized (apiAccessCounter) {
			apiAccessCounter.clear();
		}
	}
}
