import java.util.*;
class os {
	static MemoryList memoryList;
	private static LinkedList<PCB> listPCB = new LinkedList<PCB>();
	private static LinkedList<ReadyJob> listReadyQue = new LinkedList<ReadyJob>();
	static int i = 0;
	static boolean drumBusy;

	private static final int TIME_SLICE = 1;


	static void startup() {
		memoryList = new MemoryList();
		sos.ontrace();
	}

	static void Crint(int[] a, int[] p) {
		//memoryList.displayContents();
		//System.out.println(p[0]);
		i++;
		System.out.print("\nCrint" + i + " && a[0] = " + a[0]);
		PCB mPCB = new PCB(p[1], p[2], p[3], p[4], p[5]);
		listPCB.add(mPCB);
		if(!drumBusy){
		//for starting address != -1, place it into memory
		int startingAddress = memoryList.add(p[1], p[3]);		
		if (startingAddress != -1) {
			System.out.print("\nCrint" + i + " and startingAddress = " + startingAddress);
			//runReadyJob(a, p);
			sos.siodrum(p[1], p[3], startingAddress, 0);
			drumBusy = true;
			runReadyJob(a, p);
			}
		}
		else{
			p[1] = listReadyQue.getFirst().getJobNumber();
			runReadyJob(a,p);
		}
	}

	static void Svc(int[] a, int[] p) {
		switch (a[0]) {
			case 5:
				System.out.println("\nSvc: a=5");
				removeProcess(p[1]);
				memoryList.remove(p[1]);
				removeReadyJob(p[1]);
				//runReadyJob(a, p);
				a[0] = 1;
				break;
			case 6:
				System.out.println("\nSvc: a=6");
				sos.siodisk(p[1]);
				memoryList.changeIO(p[1], 1);
				a[0] = 2;
				//runReadyJob(a, p);
				break;
			case 7:
				System.out.println("\nSvc: a=7");
				System.out.println(a[0]);
				if(memoryList.get(p[1]).needsMoreIO() > 0)
					a[0]=1;
				else a[0] = 2;
				break;
		}

	}

	static void Tro(int[] a, int[] p) {
		System.out.println("\nTRO: " + "job #" + p[1] + " was running");
		ReadyJob mReadyJob = getReadyJob(p[1]);
		mReadyJob.addUsedCPUTime(TIME_SLICE);

		System.out.println("\nmaxCPUTime = " + mReadyJob.getCPUTime() +
		"\nusedCPUTime = " + mReadyJob.getUsedCPUTime());
		if(mReadyJob.getCPUTime() <= mReadyJob.getUsedCPUTime()){
			removeReadyJob(p[1]);
			memoryList.remove(p[1]);
			a[0] = 1;
		}else
		runReadyJob(a, p);
		
	}

	static void Dskint(int[] a, int[] p) {
		System.out.println("\nDsk" + a[0]);
		memoryList.changeIO(p[1], 0);
	}

	static void Drmint(int[] a, int[] p) {
		drumBusy = false;
		System.out.print("\nDrum: " + "job #" + p[1] + " is done swapping");

		PCB mPCB = getPCB(p[1]);
		mPCB.placeInMemory();

		//adds a job to ReadyQue when it is not in it yet
		if(!inReadyQue(p[1])) {
			ReadyJob mReadyJob = new ReadyJob(p[1], p[3], p[4], memoryList.findLocation(p[1]));
			if (!(listReadyQue.isEmpty())) {
				//runReadyJob(a, p);
				System.out.println("\nlistReadyQue not Empty");
				int i = 0;
				for (ReadyJob job : listReadyQue) {
					if (job.getCPUTime() > mReadyJob.getCPUTime()) {
						listReadyQue.add(i, mReadyJob);
						printReadyQue();
						//runReadyJob(a, p);
						return;
					}
					i++;
				}
				listReadyQue.add(mReadyJob);
				printReadyQue();
			} else {
				System.out.println("\nlistReadyQue is Empty");
				listReadyQue.add(mReadyJob);
			}
		}
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
			ReadyJob jobToBeRun = listReadyQue.getFirst();
			p[2] = jobToBeRun.getStartingAddress();
			p[3] = jobToBeRun.getJobSize();
			p[4] = TIME_SLICE;
			a[0] = 2;
			System.out.println("\nrunning a job #" + jobToBeRun.getJobNumber() +
							"\nstartAddress = " + p[2] +
							"\njobSize = " + p[3]);
		}else
			System.out.println("\nEmpty ReadyQue");
	}

	static ReadyJob getReadyJob(int jobNumber){
		ReadyJob temp = new ReadyJob();
		for (int i = 0; i < listReadyQue.size(); i++) {
			temp = listReadyQue.get(i);
			if (temp.getJobNumber() == jobNumber) {
				return temp;
			}
		}
		return temp;
	}

	static PCB getPCB(int jobNumber){
		PCB temp = new PCB();
		for (int i = 0; i < listPCB.size(); i++) {
			temp = listPCB.get(i);
			if (temp.getJobNumber() == jobNumber) {
				return temp;
			}
		}
		return temp;
	}

	static void printReadyQue(){
		System.out.println("ReadyQue has:");
		ReadyJob temp;
		for (int i = 0; i < listReadyQue.size(); i++) {
			temp = listReadyQue.get(i);
			System.out.print(" " + temp.getJobNumber() + " -> ");
		}
	}

	static boolean inReadyQue(int jobNumber){
		ReadyJob temp;
		for (int i = 0; i < listReadyQue.size(); i++) {
			temp = listReadyQue.get(i);
			if(temp.getJobNumber() == jobNumber)
				return true;
		}

		return false;
	}

}