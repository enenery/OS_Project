import java.util.*;
// ToDo  problems: 1) merging memory doesn't work -> in process of fixing it
// ToDo 2) after a job is removed from memory, do not lose its usedCPUTime, which means yes, it has been removed from memory and
//Todo when it is replaced into memory, it will have different starting address; however, job #, job size and usedCPUTime etc should be retained


class os {
	private static MemoryList memoryList;
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
    static int jobLeftForSOS = -1;
    
	private static final int TIME_SLICE = 1;
    
    
	static void startup() {
		memoryList = new MemoryList();
		waitingQueue = new LinkedList<ReadyJob>();
		listReadyQue  = new LinkedList<ReadyJob>();
        IOWaitQueue = new LinkedList<ReadyJob>();
		drumBusy = false;
		diskBusy = false;
		sos.ontrace();
	}
	/**
	 * if drum is not busy, we check if a waitingQue is empty. If not empty, we try  to find a waiting job
	 * that fits in memory. If it doesn't fit immediately, we try to find a job currently in memory that should
	 * be swapped out.
	 * @param a
	 * @param p
	 */
	static void Crint(int[] a, int[] p) {
        System.out.println("CRINT ");

		ReadyJob job = getReadyJob(jobLeftForSOS);
		if(job != null && jobLeftForSOS != -1 && job.getTimeLeftForSOS() != -1 && !job.isBlocked()) {
			job.addUsedCPUTime(p[5] - job.getTimeLeftForSOS());
			System.out.println("\njob# " + jobLeftForSOS + "'s used CPU Time: " + job.getUsedCPUTime());
		}
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
                    waitingJob.setStartingAddress(startAddress);
                    if (startAddress != -1) {
                        sendAJobToDrum(waitingJob);
                        removeWaitJob(waitingJob.getJobNumber());
                        setAJobToRun(a, p);
                        addToWaitingQueue(mPCB);
                        return;
                    }
                    i++;
                }
                
                //when there is no job in waitingQueue that fits without removing a job in memory
				//we find a job that should be swapped with another job
                i = 0;
                while(i < waitingQueue.size()){
                    System.out.println("\nDRUM IS NOT BUSY: A job will be removed");
                    waitingJob = waitingQueue.get(i);
                    jobToBeSwappedIn = waitingJob;
                    toBeSwappedOut = findAJobToSwap(a, p, jobToBeSwappedIn.getJobSize());
                    
                    if (toBeSwappedOut != null) {
                        memoryList.remove(toBeSwappedOut.getJobNumber());
                        listReadyQue.remove(toBeSwappedOut);
                        sendAJobToSwapOut(toBeSwappedOut);
                        addToWaitingQueue(mPCB);
                    } else
                        i++;
                    
                }
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
					System.out.println("\nDRUM : a job will be removed");
					jobToBeSwappedIn = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);
					toBeSwappedOut = findAJobToSwap(a, p, jobToBeSwappedIn.getJobSize());
                    
					if (toBeSwappedOut != null) {
                        System.out.println("\nDRUM : a job will be removed != null");
						memoryList.remove(toBeSwappedOut.getJobNumber());
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
		System.out.println("\nSVC: job left for sos last was " + "job #" +jobLeftForSOS);
		ReadyJob job = getReadyJob(jobLeftForSOS);
		if(job != null && jobLeftForSOS != -1 && job.getTimeLeftForSOS() != -1 && !job.isBlocked()) {
			int timeslice = p[5] - job.getTimeLeftForSOS();
			System.out.println("\nSVC: " + a[0] + " job# " + p[1] + "'s used CPU Time: " + job.getUsedCPUTime());
			job.addUsedCPUTime(timeslice);
		}
        
		switch (a[0]) {
			case 5:
                System.out.println("SVC " + a[0]);
                memoryList.remove(p[1]);
                removeReadyJob(p[1]);
                a[0] = 1;
                setAJobToRun(a, p);
                lookForIO();
                break;
			case 6:
                System.out.println("SVC " + a[0]);
				//System.out.println("\nSvc: a=6");
				if(!diskBusy){
					sos.siodisk(p[1]);
                    jobToBeInIO = p[1];
					diskBusy = true;
					a[0] = 2;
					setAJobToRun(a, p);
				}
				else{
                    IOWaitQueue.add(getReadyJob(p[1]));
					setAJobToRun(a,p);
				}
				memoryList.changeIO(p[1], 1);
				break;
			case 7:
                System.out.println("\ncase 7: " + "job #" + p[1] + "'s io/count = " + memoryList.get(p[1]).needsMoreIO());
				if(memoryList.get(p[1]).needsMoreIO() > 0){
					getReadyJob(p[1]).block();
					if(inReadyQue(p[1]));
					IOWaitQueue.add(getReadyJob(p[1]));
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
		System.out.print("\ntro job#" + p[1]);
		if(inReadyQue(p[1])) {
			ReadyJob mReadyJob = getReadyJob(p[1]);
			mReadyJob.addUsedCPUTime(p[5] - mReadyJob.getTimeLeftForSOS());
            
			if (mReadyJob.getCPUTime() <= mReadyJob.getUsedCPUTime()) {
				removeReadyJob(p[1]);
				memoryList.remove(p[1]);
				a[0] = 1;
				setAJobToRun(a, p);
			} else
				setAJobToRun(a, p);
            
		}
	}
    
	static void Dskint(int[] a, int[] p) {
		System.out.println("\nDskINT: job left for sos last was " + "job #" +jobLeftForSOS);
		System.out.println("\nDskINT: job #" +jobToBeInIO);

		if(inReadyQue(jobLeftForSOS)) {
			ReadyJob jobCPU = getReadyJob(jobLeftForSOS);
			if (jobLeftForSOS != -1 && jobCPU.getTimeLeftForSOS() != -1 && jobLeftForSOS != jobToBeInIO) {
				jobCPU.addUsedCPUTime(p[5] - jobCPU.getTimeLeftForSOS());
				System.out.println("\nDskINT: job #" + jobLeftForSOS + "'s used CPU Time: " + jobCPU.getUsedCPUTime());
			}
		}
		if(jobToBeInIO != -1 && inReadyQue(jobToBeInIO)) {
			ReadyJob job = getReadyJob(jobToBeInIO);
			System.out.println("\n////////DskINT: jobToBeInIO = " + jobToBeInDrum + " through getReadyJob(job#) is " + "job #" +job.getJobNumber());
		if (!job.isBlocked())
				job.addUsedCPUTime(p[5] - job.getTimeLeftForSOS());
            
			job.unblock();
		}
		
        diskBusy = false;
        
        if(inReadyQue(jobToBeInIO))
            memoryList.changeIO(jobToBeInIO, 0);
	
        System.out.println("\nDskINT: job to be in I/O is job #" + jobToBeInIO);
		//System.out.println("\nDskINT: job #" + p[1] + "'s io/count = " + memoryList.get(p[1]).needsMoreIO());

		setAJobToRun(a, p);
	}
    
	/**
	 * When called, find the newly swapped-in job in the waitingQue, remove it and add to the readyQueue; otherwise, just add it
	 * If there are more jobs in the waitingQue, pick the first node and try to place it in memory
	 * @param a
	 * @param p
	 */
	static void Drmint(int[] a, int[] p){
		System.out.println("\n//DRUMINT: jobToBeSwappedOut is job #" + jobToBeSwappedOut + "\n: jobToBeSwappedIn is job #" + jobToBeSwappedIn);

		ReadyJob job = getReadyJob(jobLeftForSOS);
		if(job != null && jobLeftForSOS != -1 && job.getTimeLeftForSOS() != -1 && !job.isBlocked()) {
			job.addUsedCPUTime(p[5] - job.getTimeLeftForSOS());
			System.out.println("\n//DRUMINT: this job left for sos last -> job# " + jobLeftForSOS + "'s used CPU Time: " + job.getUsedCPUTime());
		}
		drumBusy = false;
		
		if(swappingIn) {
				ReadyJob swappedIn = getReadyJob(jobToBeInDrum);
				if(swappedIn != null) {
					swappedIn.setInDrum();
					System.out.println("\n//DRUMINT: job #" + swappedIn.getJobNumber() + " swap completed.");
				}
			
		}else{
			if(inReadyQue(jobToBeSwappedOut)){
				ReadyJob swappedJob = getReadyJob(jobToBeSwappedOut);
				swappedJob.outOfDrum();
				removeReadyJob(jobToBeSwappedOut);
				addToWaitingQueue(swappedJob);
				System.out.println("\n///DRUMINT: job #" + swappedJob.getJobNumber() + " has been removed out of memory.");
				swappingIn = true;

				ReadyJob toBeSwappedIn = jobToBeSwappedIn;
				System.out.println("\n///DRUMINT: job #" + toBeSwappedIn.getJobNumber() + " should be sent to drum.");
				int startAddress = memoryList.add(toBeSwappedIn.getJobNumber(), toBeSwappedIn.getJobSize());
				if(startAddress != -1){
					System.out.println("\n///DRUMINT: job #" + toBeSwappedIn.getJobNumber() + " has been sent out to drum.");
					removeWaitJob(toBeSwappedIn.getJobNumber());
					sendAJobToDrum(toBeSwappedIn);
				}
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
		//memoryList.displayContents();
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
			ReadyJob job = getReadyJob(jobNum);
			int remainingCPUTime = job.getRemainingCPUTime();
			
			System.out.println("\nrunReadyJob job left for sos last was " + "job #" +jobLeftForSOS);
			System.out.println("runReadyJob job #" + jobNum + "'s used CPU Time is " + job.getUsedCPUTime() +
                               " so it has the remaining CPU Time of " + remainingCPUTime);
			job.setTimeLeftForSOS(p[5]);
			System.out.println("runReadyJob job #" + jobNum + " leaving OS now at " + p[5]);
			jobLeftForSOS = jobNum;
			p[1] = jobNum;
			p[2] = startingAddress;
			p[3] = size;
			p[4] = remainingCPUTime;
			a[0] = 2;
            
		}else
			System.out.println("\nEmpty ReadyQue");
	}
    
	static ReadyJob getReadyJob(int jobNumber){
		System.out.println("\ngetReadyJob");
		ReadyJob temp = new ReadyJob();
		for (int i = 0; i < listReadyQue.size(); i++) {
			if (listReadyQue.get(i).getJobNumber() == jobNumber) {
				temp = listReadyQue.get(i);
				temp.setTimeLeftForSOS(listReadyQue.get(i).getTimeLeftForSOS());
				temp.setUsedCPUTime(listReadyQue.get(i).getUsedCPUTime());
				temp.setStartingAddress(listReadyQue.get(i).getStartingAddress());
				temp.setCPUTime(listReadyQue.get(i).getCPUTime());
				temp.setJobNumber(listReadyQue.get(i).getJobNumber());
				temp.setJobSize(listReadyQue.get(i).getJobSize());
				temp.setPriority(listReadyQue.get(i).getPriority());
				return temp;
			}
		}
		System.out.println("\ngetReadyJob: job # " + jobNumber + " DNE");
		return null;
	}
    
    
	static void printReadyQue(){
		System.out.println("\nReadyQue has:");
		ReadyJob temp;
		for (int i = 0; i < listReadyQue.size(); i++) {
			temp = listReadyQue.get(i);
            temp.displayContents();
			System.out.println("next -> ");
		}
	}
    
	static void printWaitQue(){
		System.out.println("\nWaitQue has:");
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
    ///////////////////////////////////////////
	static boolean inWaitQue(int jobNumber){
		ReadyJob temp;
		for (int i = 0; i < waitingQueue.size(); i++) {
			temp = waitingQueue.get(i);
			if(temp.getJobNumber() == jobNumber)
				return true;
		}
		return false;
	}
    ///////////////////////////////////////////
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
    ///////////////////////////////////////////
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
		System.out.println("\nlookForIO :");
        if(!IOWaitQueue.isEmpty() && !diskBusy){
			System.out.println("\nlookForIO 1:");
            ReadyJob mJob = IOWaitQueue.pop();
            if(inReadyQue(mJob.getJobNumber())) {
				System.out.println("\nlookForIO2: ");
				sos.siodisk(mJob.getJobNumber());
				jobToBeInIO = mJob.getJobNumber();
				diskBusy = true;
			}
        }
    }

    static boolean canFit(int size, int jobNum){
        MemoryList tmp = memoryList.copy(memoryList);
        //tmp = new MemoryList(memoryList);
        if(tmp!=null) {
			tmp.remove(jobNum);
			if (tmp.add(-1, size) != -1)
				return true;
			else return false;
		}
		return false;
    }
    
    static void sendAJobToDrum(ReadyJob toBeSent){
		sos.siodrum(toBeSent.getJobNumber(), toBeSent.getJobSize(), toBeSent.getStartingAddress(), 0);
		jobToBeInDrum = toBeSent.getJobNumber();
		addToReadyQueue(new ReadyJob(toBeSent.getJobNumber(), toBeSent.getPriority(), toBeSent.getJobSize(),
                                     toBeSent.getCPUTime(), toBeSent.getSubmissionTime(), toBeSent.getStartingAddress()));
		System.out.println("\njobToBeInDrum is job #" + jobToBeInDrum + " is added to ReadyQue with starting address at " + toBeSent.getStartingAddress());
		swappingIn = true;
		drumBusy = true;
	}
    
	static void sendAJobToSwapOut(ReadyJob toBeSwappedOut){
		addToWaitingQueue(toBeSwappedOut);
    	removeReadyJob(toBeSwappedOut.getJobNumber());
		sos.siodrum(toBeSwappedOut.getJobNumber(), toBeSwappedOut.getJobSize(), toBeSwappedOut.getStartingAddress(), 1);
		swappingIn = false;
		jobToBeSwappedOut = toBeSwappedOut.getJobNumber();
		System.out.println("\njobToBeSwapOut is job #" + jobToBeSwappedOut);
		drumBusy = true;
	}
    

}