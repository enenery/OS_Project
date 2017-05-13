import java.util.*;
// ToDo  2) add swap-out feature to swap a maxCPUTIme of a job in core is larger with
// ToDo     the waiting job with a smaller maxCPUTime


class os {
	static MemoryList memoryList;
	private static LinkedList<ReadyJob> waitingQueue;
	private static LinkedList<ReadyJob> listReadyQue;
    private static LinkedList<ReadyJob> IOWaitQueue;
	static boolean drumBusy;
	static boolean diskBusy;
	static int jobToBeInDrum = -1;
    static int jobToBeInIO = -1;
    static int jobToBeSwappedOut = -1;
    static boolean swappingIn = false;
    static ReadyJob jobToBeSwappedIn = null;

	private static final int TIME_SLICE = 1;

    
	static void startup() {
		memoryList = new MemoryList();
		waitingQueue = new LinkedList<ReadyJob>();
		listReadyQue  = new LinkedList<ReadyJob>();
        IOWaitQueue = new LinkedList<ReadyJob>();
		drumBusy = false;
		diskBusy = false;
		//sos.ontrace();
	}
	/**
	 * if drum is not busy, we check if a waitingQue is empty. If not empty, we try  to find a waiting job
	 * that fits in memory. If it doesn't fit immediately, we try to find a job currently in memory that should
	 * be swapped out.
	 * @param a
	 * @param p
	 */
	static void Crint(int[] a, int[] p) {
		System.out.println("\nCRINT: job #" + p[1] + " has arrived");
		ReadyJob mPCB = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);
		ReadyJob toBeSwappedOut = null;

		if (!drumBusy) {
			System.out.println("\nDRUM IS NOT BUSY");

			if(!waitingQueue.isEmpty()) {
				ReadyJob waitingJob = null;
				int startAddress = -1;
				int i = 0;

					while (i < waitingQueue.size() ) {
						waitingJob = waitingQueue.get(i);
						startAddress = memoryList.add(waitingJob.getJobNumber(), waitingJob.getJobSize());

						if (startAddress != -1) {
							sendAJobToDrum(waitingJob);
							setAJobToRun(a, p);
							return;
						} else {
							jobToBeSwappedIn = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);
							toBeSwappedOut = findAJobToSwap(a, p, jobToBeSwappedIn.getJobSize());

							if (toBeSwappedOut != null) {
								sendAJobToSwapOut(toBeSwappedOut);
							} else
								i++;
						}
					}
						if(waitingQueue.size() == i)
							addToWaitingQueue(mPCB);
				
			}else
				{

				/*when a waitingQue is empty, send this newly arrived job to siodrum
					if the newly arrived job doesn't fit in memory, try to find a job to swap out
				 */

				int startingAddress = memoryList.add(p[1], p[3]);
				if (startingAddress != -1) {
					sendAJobToDrum(new ReadyJob(p[1], p[2], p[3], p[4], p[5], startingAddress));
				} else {
					jobToBeSwappedIn = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);
					toBeSwappedOut = findAJobToSwap(a, p, jobToBeSwappedIn.getJobSize());

					if (toBeSwappedOut != null) {
						sendAJobToSwapOut(toBeSwappedOut);
					} else
						addToWaitingQueue(mPCB);
				}
			}
		}else {
			System.out.println("\nDRUM IS BUSY");
			addToWaitingQueue(mPCB);
		}

        printReadyQue();
        printWaitQue();
        setAJobToRun(a, p);
	}
    
	static void Svc(int[] a, int[] p) {

		switch (a[0]) {
			case 5:
                memoryList.remove(p[1]);
                removeReadyJob(p[1]);
                a[0] = 1;
                setAJobToRun(a, p);
                lookForIO();
                break;
			case 6:
				//System.out.println("\nSvc: a=6");
				if(!diskBusy){
					sos.siodisk(p[1]);
                    jobToBeInIO = p[1];
					diskBusy = true;
					a[0] = 2;
				}
				else{
                    IOWaitQueue.add(getReadyJob(p[1]));
					setAJobToRun(a,p);
				}
				memoryList.changeIO(p[1], 1);
				break;
			case 7:
                //getReadyJob(p[1]).displayContents();
                System.out.println(memoryList.get(p[1]).needsMoreIO());
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
			setAJobToRun(a, p);
		}else
			setAJobToRun(a, p);

	}
    
	static void Dskint(int[] a, int[] p) {
		System.out.println("\nDsk" + p[1]);
		getReadyJob(jobToBeInIO).unblock();
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

		if(swappingIn) {
			if (jobToBeInDrum != -1) {
				ReadyJob swappedIn = getReadyJob(jobToBeInDrum);
				swappedIn.setInDrum();
				System.out.println("\nDRUMINT: job #" + swappedIn.getJobNumber() + " swap completed.");
			}
		}else{
			if(jobToBeSwappedOut != -1){
				ReadyJob swappedJob = getReadyJob(jobToBeSwappedOut);
				swappedJob.outOfDrum();
				removeReadyJob(jobToBeSwappedOut);
				addToWaitingQueue(swappedJob);
				System.out.println("\n///DRUMINT: job #" + swappedJob.getJobNumber() + " has been removed out of memory.");
				ReadyJob toBeSwappedIn = jobToBeSwappedIn;
				int startAddress = memoryList.add(toBeSwappedIn.getJobNumber(), toBeSwappedIn.getJobSize());
				int i = 1;
				swappingIn = true;
				sos.siodrum(toBeSwappedIn.getJobNumber(), toBeSwappedIn.getJobSize(), startAddress, 0);
				jobToBeInDrum = toBeSwappedIn.getJobNumber();
				drumBusy = true;
			}
		}

		if(!drumBusy) {
			if (!waitingQueue.isEmpty()) {
				ReadyJob waitingJob = waitingQueue.get(0);
				int startAddress = memoryList.add(waitingJob.getJobNumber(), waitingJob.getJobSize());
				int i = 1;

				while (i < waitingQueue.size() && startAddress == -1) {
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
		}
		printReadyQue();
		printWaitQue();
		setAJobToRun(a, p);
	}
    
    
    //ListReadyQueue Functions///
	   
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
					//System.out.println("\nWe are going to run a job #" + p[1]);
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

	static void addToWaitingQueue(ReadyJob readyJob){
		int i = 0;
		while(i < waitingQueue.size()){
			if(waitingQueue.get(i).getCPUTime() > readyJob.getCPUTime()) {
				waitingQueue.add(i, readyJob);
				return;
			}
			i++;
		}
		waitingQueue.add(readyJob);
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

	/**
	 * returns a job in memory that has a larger remaining CPU time than the newly arrived job's max CPU time
	 * @param a
	 * @param p
	 * @return
	 */
	static ReadyJob findAJobToSwap(int a[], int p[], int newJobSize) {
		int remainingCPUTime = 0;
		ReadyJob jobToBeSwapped = null;
		int i = 0;
		while (i < listReadyQue.size()) {
			jobToBeSwapped = listReadyQue.get(i);
			remainingCPUTime = jobToBeSwapped.getCPUTime() - jobToBeSwapped.getUsedCPUTime();
			if (remainingCPUTime > p[4] && canFit(newJobSize, jobToBeSwapped.getJobNumber()))
				return jobToBeSwapped;
			else
			i++;
		}
		return jobToBeSwapped;
	}
    
    static void lookForIO(){
        if(!IOWaitQueue.isEmpty() && !diskBusy){
            ReadyJob mJob = IOWaitQueue.pop();
            sos.siodisk(mJob.getJobNumber());
            diskBusy = true;
        }
    
    }
    
    static boolean canFit(int size, int jobNum){
        MemoryList tmp = new MemoryList(memoryList);
        tmp.remove(jobNum);
        if(tmp.add(-1,size) != -1 )
            return true;
        else return false;
    }

    static void sendAJobToDrum(ReadyJob toBeSent){
		sos.siodrum(toBeSent.getJobNumber(), toBeSent.getJobSize(), toBeSent.getStartingAddress(), 0);
		addToReadyQueue(new ReadyJob(toBeSent.getJobNumber(), toBeSent.getPriority(), toBeSent.getJobSize(),
				toBeSent.getCPUTime(), toBeSent.getSubmissionTime(), toBeSent.getStartingAddress()));
		jobToBeInDrum = toBeSent.getJobNumber();
		System.out.println("\njob #" + jobToBeInDrum + " is added to ReadyQue with starting address at " + toBeSent.getStartingAddress());
		swappingIn = true;
		drumBusy = true;
	}

	static void sendAJobToSwapOut(ReadyJob toBeSwappedOut){
		sos.siodrum(toBeSwappedOut.getJobNumber(), toBeSwappedOut.getJobSize(), toBeSwappedOut.getStartingAddress(), 1);
		swappingIn = false;
		jobToBeSwappedOut = toBeSwappedOut.getJobNumber();
		System.out.println("\njob #" + jobToBeSwappedOut + " is added to ReadyQue with starting address at " + toBeSwappedOut.getStartingAddress());
		drumBusy = true;
	}
}