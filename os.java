import com.sun.org.apache.regexp.internal.RE;

import java.util.*;
class os {
	static MemoryList memoryList;
	private static LinkedList<PCB> listPCB = new LinkedList<PCB>();
	private static LinkedList<ReadyJob> listReadyQue = new LinkedList<ReadyJob>();

	private static final int TIME_SLICE = 5;


	static void startup() {
		memoryList = new MemoryList();
		sos.ontrace();
	}

	static void Crint(int[] a, int[] p) {
		System.out.print("\nCrint");
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

		/*if (!(listReadyQue.isEmpty())) {
			ReadyJob jobToBeRun = listReadyQue.getFirst();
			p[2] = jobToBeRun.getStartingAddress();
			p[3] = jobToBeRun.getJobSize();
			p[4] = TIME_SLICE;
		}*/
	}

	static void Svc(int[] a, int[] p) {
		switch (a[0]) {
			case 5:
				System.out.println("\nSvc: a=5");
				removeProcess(p[1]);
				memoryList.remove(p[1]);
				runReadyJob(a, p);
				break;
			case 6:
				System.out.println("\nSvc: a=6");
				removeReadyJob(p[1]);
				a[0] = 2;
				sos.siodisk(p[1]);
				runReadyJob(a, p);
				break;
			case 7:
				System.out.println("\nSvc: a=7");
				removeReadyJob(p[1]);
				a[0] = 1;
				//sos.siodisk(p[1]);
				//runReadyJob(a, p);
				break;
		}

	}

	static void Tro(int[] a, int[] p) {
		System.out.print("\nTro");
		//If time > 0
		//Time slice ran out
		//otherwise it finished
	}

	static void Dskint(int[] a, int[] p) {
		System.out.print("\nDisk");
	}

	static void Drmint(int[] a, int[] p) {
		System.out.print("\nDrum");
		if (!(listReadyQue.isEmpty())) {
			ReadyJob jobToBeRun = listReadyQue.getFirst();
			p[2] = jobToBeRun.getStartingAddress();
			p[3] = jobToBeRun.getJobSize();
			p[4] = TIME_SLICE;
			a[0] = 2;
		}

	}

	static void removeProcess(int jobNumber) {
		for (int i = 0; i < listPCB.size(); i++) {
			PCB temp = listPCB.get(i);
			if (temp.getJobNumber() == jobNumber) {
				System.out.println("\nremoving a process from PCB");
				listPCB.remove(i);
				break;
			}
		}
	}

	static void removeReadyJob(int jobNumber) {
		for (int i = 0; i < listReadyQue.size(); i++) {
			ReadyJob temp = listReadyQue.get(i);
			if (temp.getJobNumber() == jobNumber) {
				System.out.println("\nremoving a job from ReadyQue");
				listReadyQue.remove(i);
				break;
			}
		}
	}

	static void runReadyJob(int[] a, int[] p){
		if (!(listReadyQue.isEmpty())) {
			System.out.println("\nrunning a job from ReadyQue");
			ReadyJob jobToBeRun = listReadyQue.getFirst();
			p[2] = jobToBeRun.getStartingAddress();
			p[3] = jobToBeRun.getJobSize();
			p[4] = TIME_SLICE;
			a[0] = 2;
		}else
			System.out.println("\nEmpty ReadyQue");
	}
}