import java.util.*;
// ToDo 1)find out what we are doing to make job #5 blocked in os though it is unblocked in sos
// ToDo  2) add swap-out feature to swap a maxCPUTIme of a job in core is larger with
// ToDo     the waiting job with a smaller maxCPUTime


class os {
	static MemoryList memoryList;
	private static LinkedList<ReadyJob> waitingQueue;
	private static LinkedList<ReadyJob> listReadyQue;
	static int i = 0;
	static boolean drumBusy;
	static boolean diskBusy;
	static int jobToBeInDrum = -1;

	private static final int TIME_SLICE = 1;

	static void startup() {
		memoryList = new MemoryList();
		waitingQueue = new LinkedList<ReadyJob>();
		listReadyQue  = new LinkedList<ReadyJob>();
		drumBusy = false;
		diskBusy = false;
		//sos.ontrace();
	}

	/**
	 * if drum is not busy, we will add it to memoryList and call siodrum for sos to place it in its memory
	 * @param a
	 * @param p
	 */
	static void Crint(int[] a, int[] p) {
		System.out.println("\nCRINT: job #" + p[1] + " has arrived");
		ReadyJob mPCB = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);

		if (!drumBusy) {
			System.out.println("\nDRUM IS NOT BUSY");
			int startingAddress = memoryList.add(p[1], p[3]);
			if (startingAddress != -1) {
				sos.siodrum(p[1], p[3], startingAddress, 0);
				addToReadyQueue(new ReadyJob(p[1], p[2],
						p[3], p[4], p[5], startingAddress));
				System.out.println("\njob #" + p[1] + " is added to ReadyQue with starting address at " +  startingAddress);
				jobToBeInDrum = p[1];
				drumBusy = true;
			}else
				waitingQueue.add(mPCB);

		}else {
			System.out.println("\nDRUM IS BUSY");
			waitingQueue.add(mPCB);
		}
			printReadyQue();
			printWaitQue();
			setAJobToRun(a, p);
	}

	static void Svc(int[] a, int[] p) {

		switch (a[0]) {
			case 5:
				//System.out.println("\nSvc: a=5");
				memoryList.remove(p[1]);
				removeReadyJob(p[1]);
				a[0] = 1;
				setAJobToRun(a, p);
				break;
			case 6:
				//System.out.println("\nSvc: a=6");
				if(!diskBusy){
					sos.siodisk(p[1]);
					diskBusy = true;
					a[0] = 2;
				}
				else{
					setAJobToRun(a,p);
				}
				memoryList.changeIO(p[1], 1);
				break;
			case 7:
				if(memoryList.get(p[1]).needsMoreIO() > 0){
					getReadyJob(p[1]).block();
					if(oneJobOrLess()){
						a[0] = 1;
						return;
					}
					setAJobToRun(a,p);
				}
				else{
					getReadyJob(p[1]).unblock();
					setAJobToRun(a,p);
				}
				break;
		}

	}

	static void Tro(int[] a, int[] p){
		//System.out.print("\ntro");
		ReadyJob mReadyJob = getReadyJob(p[1]);
		mReadyJob.addUsedCPUTime(TIME_SLICE);

		if(mReadyJob.getCPUTime() <= mReadyJob.getUsedCPUTime()){
			removeReadyJob(p[1]);
			memoryList.remove(p[1]);
			a[0] = 1;
			//pickJob(a,p);
			setAJobToRun(a, p);
		}else
			setAJobToRun(a, p);

	}

	static void Dskint(int[] a, int[] p) {
		System.out.println("\nDsk" + a[0]);
		getReadyJob(p[1]).unblock();
		diskBusy = false;
		memoryList.changeIO(p[1], 0);
	}

	/**
	 * When called, find the newly swapped-in job in the waitingQue, remove it and add to the readyQueue; otherwise, just add it
	 * If there are more jobs in the waitingQue, pick the first node and try to place it in memory
	 * @param a
	 * @param p
	 */
	static void Drmint(int[] a, int[] p){
		drumBusy = false;

		if(jobToBeInDrum != -1) {
			ReadyJob swappedIn = getReadyJob(jobToBeInDrum);
			swappedIn.setInDrum();
			System.out.println("\nDRUMINT: job #" + swappedIn.getJobNumber() + " swap completed.");
		}

		if(!waitingQueue.isEmpty()) {
			ReadyJob waitingJob = waitingQueue.get(0);
			int startAddress = memoryList.add(waitingJob.getJobNumber(), waitingJob.getJobSize());
			int i = 1;

			while(i < waitingQueue.size() && startAddress == -1){
				waitingJob = waitingQueue.get(i);
				startAddress = memoryList.add(waitingJob.getJobNumber(), waitingJob.getJobSize());
				i++;
			}

			if (startAddress != -1) {
				removeWaitJob(waitingJob.getJobNumber());
				System.out.println("\nDRUMINT: job #" + waitingJob.getJobNumber() + " swap completed.");
				addToReadyQueue(new ReadyJob(waitingJob.getJobNumber(), waitingJob.getPriority(),
						waitingJob.getJobSize(), waitingJob.getCPUTime(), waitingJob.getSubmissionTime(), startAddress));
				System.out.println("\njob #" + waitingJob.getJobNumber() + " is added to ReadyQue with starting address at " + startAddress);
				sos.siodrum(waitingJob.getJobNumber(), waitingJob.getJobSize(), startAddress, 0);
				jobToBeInDrum = waitingJob.getJobNumber();
				drumBusy = true;
			}
		}

		printReadyQue();
		printWaitQue();
		setAJobToRun(a, p);
	}
	static void removeReadyJob(int jobNumber) {
		for (int i = 0; i < listReadyQue.size(); i++) {
			ReadyJob temp = listReadyQue.get(i);
			if (temp.getJobNumber() == jobNumber) {
				System.out.println("\nremoving a job # " + jobNumber + " from ReadyQue");
				listReadyQue.remove(i);
				break;
			}
		}

		printReadyQue();
		printWaitQue();
	}

	static void setAJobToRun(int [] a, int[] p) {
		if (!listReadyQue.isEmpty()) {
			int i = 0;
			while (i < listReadyQue.size()) {
				ReadyJob jobToBeRun = listReadyQue.get(i);
				if (!jobToBeRun.isBlocked() && jobToBeRun.isInDrum()) {
					//System.out.println("\nWe are going to run a job #" + p[1] +
							//"\nstartAddress = " + p[2] + "\njobSize = " + p[3]);
					runReadyJob(jobToBeRun.getJobNumber(), jobToBeRun.getJobSize(), jobToBeRun.getStartingAddress(), a, p);
					return;
				} else {
					//System.out.println("\njob #" + jobToBeRun.getJobNumber() + " is blocked");
					i++;
				}
			}
			a[0] = 1;
		}
	}

	static void runReadyJob(int jobNum, int size, int startingAddress, int [] a, int [] p){
		if (!(memoryList.isEmpty())) {
			p[1] = jobNum;
			p[2] = startingAddress;
			p[3] = size;
			p[4] = TIME_SLICE;
			a[0] = 2;
			//System.out.println("\n We'll be running a job #" + p[1] +
					//"\nstartAddress = " + p[2] +
					//"\njobSize = " + p[3]);
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

	static void printReadyQue(){
		System.out.print("\nReadyQue has:");
		ReadyJob temp;
		for (int i = 0; i < listReadyQue.size(); i++) {
			temp = listReadyQue.get(i);
			System.out.print(" " + temp.getJobNumber() + " -> ");
		}
	}

	static void printWaitQue(){
		System.out.print("\nWaitQue has:");
		ReadyJob temp;
		for (int i = 0; i < waitingQueue.size(); i++) {
			temp = waitingQueue.get(i);
			System.out.print(" " + temp.getJobNumber() + " -> ");
		}
	}

	static boolean oneJobOrLess(){
		if(listReadyQue.size() < 2)
			return true;
		else
			return false;
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

	static boolean inWaitQue(int jobNumber){
		ReadyJob temp;
		for (int i = 0; i < waitingQueue.size(); i++) {
			temp = waitingQueue.get(i);
			if(temp.getJobNumber() == jobNumber)
				return true;
		}
		return false;
	}

	static void addToReadyQueue(ReadyJob readyJob){
		int i = 0;
		while(i < listReadyQue.size()){
			if(listReadyQue.get(i).getCPUTime() > readyJob.getCPUTime()) {
				listReadyQue.add(i, readyJob);
				return;
			}
			i++;
		}
		listReadyQue.add(readyJob);
	}

	static void removeWaitJob(int jobNumber){
		int i = 0;
		while(i < waitingQueue.size()){
			if(waitingQueue.get(i).getJobNumber() == jobNumber) {
				waitingQueue.remove(i);
				return;
			}
			i++;
		}

	}
}