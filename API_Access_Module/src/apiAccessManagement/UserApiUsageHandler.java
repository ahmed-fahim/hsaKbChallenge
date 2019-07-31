package apiAccessManagement;

import java.util.HashMap;

public class UserApiUsageHandler extends GlobalApiUsageHandler implements Comparable<UserApiUsageHandler> {
	/**
	 * Inheriting GlobalApiUsageHandler as it has common purpose with a few
	 * specialized purpose
	 */
	private String userName;
	private HashMap<String, Integer> apiTotalHitCounter;
	private int comparatorKey;

	public UserApiUsageHandler() {
		super();
		userName = "";
	}

	public UserApiUsageHandler(String mUserName) {
		super();
		userName = mUserName;
		apiTotalHitCounter = new HashMap<>();
	}

	public String getUserName() {
		return userName;
	}

	public synchronized void addHitCountForApi(String apiName) {
		synchronized (apiTotalHitCounter) {
			int prevHitCounter = 0;
			if (apiTotalHitCounter.containsKey(apiName)) {
				prevHitCounter = apiTotalHitCounter.get(apiName);
			}
			apiTotalHitCounter.put(apiName, prevHitCounter + 1);
		}
	}

	@Override
	public synchronized void addUsageCountForApi(String apiName) {
		super.addUsageCountForApi(apiName);
		addHitCountForApi(apiName);
	}

	public synchronized int getTotalHitCountForApi(String apiName) {
		int totalHitCount = 0;
		synchronized (apiTotalHitCounter) {
			if (apiTotalHitCounter.containsKey(apiName)) {
				totalHitCount = apiTotalHitCounter.get(apiName);
			}
		}
		return totalHitCount;
	}

	public synchronized void setComparator(String apiName) {
		comparatorKey = getTotalHitCountForApi(apiName);
	}

	// Compare method for Comparable inheritance
	@Override
	public int compareTo(UserApiUsageHandler o) {
		return comparatorKey - o.comparatorKey;
	}
}
