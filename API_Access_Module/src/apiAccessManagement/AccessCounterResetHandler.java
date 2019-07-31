package apiAccessManagement;

import java.util.HashMap;

public class AccessCounterResetHandler implements Runnable {
	private static Thread ownThread;
	private HashMap<String, UserApiUsageHandler> apiUserHandlers;
	private GlobalApiUsageHandler globalApiUsageHandler;
	private static final long RESET_INTERVAL = 60 * 1000;
	private Integer threadRunnerLock;

	public AccessCounterResetHandler() {
		// Redundant Default Construction in case Object is called with empty arguments
		apiUserHandlers = new HashMap<>();
		globalApiUsageHandler = new GlobalApiUsageHandler();
		threadRunnerLock = 0;
	}

	public AccessCounterResetHandler(HashMap<String, UserApiUsageHandler> userHandlerArg,
			GlobalApiUsageHandler globalHandlerArg) {
		apiUserHandlers = userHandlerArg;
		globalApiUsageHandler = globalHandlerArg;
		threadRunnerLock = 0;
	}

	public void start() {
		if (ownThread == null) {
			ownThread = new Thread(this, "ResettingThread");
		}
		threadRunnerLock = 1;
		ownThread.start();
	}

	public void stop() {
		synchronized (threadRunnerLock) {
			threadRunnerLock = 0;
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() {
		while (true) {
			synchronized (threadRunnerLock) {
				if (threadRunnerLock == 0) {
					break;
				}
			}
			try {
				ownThread.sleep(RESET_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (apiUserHandlers) {
				for (UserApiUsageHandler user : apiUserHandlers.values()) {
					user.resetAccessCounter();
				}
			}
			synchronized (globalApiUsageHandler) {
				globalApiUsageHandler.resetAccessCounter();
			}
		}
	}
}
