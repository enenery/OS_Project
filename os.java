import java.util.*;
class os {
	static MemoryList memoryList;
	private static LinkedList<PCB> listPCB = new LinkedList<PCB>();
	private static LinkedList<ReadyJob> listReadyQue = new LinkedList<ReadyJob>();
	private static final int TIME_SLICE = 500;

	int TESTINT = 0;

	static void startup() {
		memoryList = new MemoryList();
	}

	static void Crint(int[] a, int[] p) {
		PCB mPCB = new PCB(p[1], p[2], p[3], p[4], p[5]);
		listPCB.add(mPCB);

		//for starting address != -1, place it into memory
		int startingAddress = memoryList.add(p[1], p[3]);
		if (startingAddress != -1) {
			sos.siodrum(p[1], p[3], startingAddress, 0);
			//create a new ReadyQue and find the right place to store it into listReadyQue
			ReadyJob mReadyJob = new ReadyJob(p[1], p[3], p[4], startingAddress);
			if (!(listReadyQue.isEmpty())) {
				int i = 0;
				for (ReadyJob job : listReadyQue) {
					if (job.getCPUTime() > mReadyJob.getCPUTime()) {
						listReadyQue.add(i, mReadyJob);
						return;
					}
					i++;
				}
				listReadyQue.add(mReadyJob);
			} else
				listReadyQue.add(mReadyJob);
		}

		if (!(listReadyQue.isEmpty())) {
			ReadyJob jobToBeRun = listReadyQue.getFirst();
			a[0] = 2;
			p[2] = jobToBeRun.getStartingAddress();
			p[3] = jobToBeRun.getJobSize();
			p[4] = TIME_SLICE;
		}
	}

	static void Svc(int[] a, int[] p) {
		System.out.println("Svc");
		switch (a[0]) {
			case 5:
				removeProcess(p[1]);
				break;
		}

	}

	static void Tro(int[] a, int[] p) {
		//If time > 0
		//Time slice ran out
		//otherwise it finished
	}

	static void Diskint(int[] a, int[] p) {
	}

	static void Drmint(int[] a, int[] p) {
	}

	static void removeProcess(int jobNumber) {
		for (int i = 0; i < listPCB.size(); i++) {
			PCB temp = listPCB.get(i);
			if (temp.getJobNumber() == jobNumber) {
				System.out.println("removing a process from PCB");
				listPCB.remove(i);
				break;
			}
		}
	}
}