import java.util.*;


class os {
	private static MemoryList memoryList;
	private static LinkedList<ReadyJob> waitingQueue;
	private static LinkedList<ReadyJob> listReadyQue ;
    private static LinkedList<Integer> IOQueue;
	private static LinkedList<ReadyJob> jobSentToDrum;
	private static LinkedList<ReadyJob> jobLeftForSOS;
    private static boolean drumBusy;
	private static boolean diskBusy;
    
	static void startup() {
		memoryList = new MemoryList();
		waitingQueue = new LinkedList<ReadyJob>();
		listReadyQue  = new LinkedList<ReadyJob>();
        IOQueue = new LinkedList<Integer>();
		jobSentToDrum = new LinkedList<ReadyJob>();
        jobLeftForSOS = new LinkedList<ReadyJob>();
		drumBusy = false;
		diskBusy = false;
		sos.ontrace();
	}

	/**
	 * a new job arrives at Crint from sos
	 * 1. check if drum is free
	 * 1. yes -> 2. check if this new job fits in memory (okay, I don't actually like the add method in
	 * MemoryList because it does two things in one method but I think I can deal with that.)
	 * 2. yes -> (yes means memoryList.add(*) returned an int unequal -1)send this job to drum
	 * 1 & 2. No -> push this new job into waitQueue
	 * 3. set a job to run
	 * ToDo if drum is not busy but this new job does not fit add a feature to look for another job in
	 * ToDo waitingQueue that might fit, if it does, send it to Drum.
	 * @param a
	 * @param p
	 */
	static void Crint(int[] a, int[] p) {
        System.out.println("\nCRINT");

		if(!drumBusy){
			int startAddress =  memoryList.add(p[1], p[3]);
			if(startAddress != -1)
				sendAJobToDrum(p, startAddress);
			else
				addAJobToWaitQueue(p);
		}
		else{
			addAJobToWaitQueue(p);
		}

		sendAJobToDrum();
        setAJobToRun(a, p);
	}

	/**
	 *Svc
	 * For a[0] = 5
	 * check if this job has more I/O
	 * No -> remove this job from memoryList and ReadyQueue && attempt to send a job to drum
	 * Yes -> set waitingForIOCompletion to be true
	 * For a[0] = 6
	 * 1. add this new job requesting for IO in IOQueue
	 * 2. check if drum is busy
	 * 2. Yes -> a job to disk
	 * 2. No -> do nothing
	 * For a[0] = 7 (I thought jobs that request to be blocked has always more I/O to be done...nope!
	 * if there are more I/O needs to be done for this job -> block this job
	 * For all jobs, set a job to run
	 * @param a
	 * @param p
	 */
	static void Svc(int[] a, int[] p) {
        System.out.println("\nSVC " + a[0] + " Job #" + p[1]);
		switch (a[0]) {
			case 5:
                if(!isThereMoreIO(p[1])) {
					System.out.println("\n//////");
					memoryList.remove(p[1]);
					removeReadyJob(p[1]);
					sendAJobToDrum();
				}
				else {
					getReadyJob(p[1]).setWaitingForIOCompletion(true);
				}
                break;
			case 6:
				IOQueue.add(p[1]);
				if(!diskBusy)
					sendAJobToDisk();
				break;
			case 7:
                if(isThereMoreIO(p[1]))
				getReadyJob(p[1]).block();
				break;
		}
		setAJobToRun(a, p);
	}

	/**
	 * Tro
	 * (for shortest remaining time next, if Tro gets called, it means it has used up the max CPU Time)
	 * 0. check if this job needs more I/O
	 * 0. No -> remove from memory, readyJobQueue, and jobLeftForSOS list
	 * 0. Yes -> set waitingForIOCompletion to be true
	 * 1. remove this from jobLeftForSOS list, memory and readyQueue
	 * 1.5 pick a job to send to drum, if possible
	 * 2. set a job to run
	 * @param a
	 * @param p
	 */
	static void Tro(int[] a, int[] p){
		System.out.println("\nTro");
		ReadyJob job = jobLeftForSOS.getFirst();

		if(!isThereMoreIO(job.getJobNumber())) {
			memoryList.remove(job.getJobNumber());
			removeReadyJob(job.getJobNumber());
			jobLeftForSOS.remove(0);
		}
		else{
			getReadyJob(job.getJobNumber()).setWaitingForIOCompletion(true);

		}
		sendAJobToDrum();
		setAJobToRun(a, p);
	}

	/**
	 * Dskint
	 * 0. sets diskBusy = false
	 * 1. removes a job from IOQueue
	 * 2. sends a job to disk
	 * 3. set a job to run
	 * @param a
	 * @param p
	 */
	static void Dskint(int[] a, int[] p) {
		System.out.println("\nDskint");
		diskBusy = false;
		popAJobInIOQueue();
		sendAJobToDisk();
		setAJobToRun(a, p);
	}
    
	/**
	 * Drmint
	 * 1. set up this job to be placed in ReadyQueue
	 * 1.5 sets drumBusy = false;
	 * 2. try if there is any job in waitingQueue that can be placed into memory
	 * 2. yes -> send it to drum
	 * 3. set to run a job
	 * @param a
	 * @param p
	 */
	static void Drmint(int[] a, int[] p){
		System.out.println("\nDrmint");
		drumBusy = false;
		addAJobToReadyQueue();

		ReadyJob job = pickFromWaitQueue();
			if(job != null)
				sendAJobToDrum(job);

		setAJobToRun(a, p);
	}


	/**
	 * removeReadyJob
	 * traverses the listReadyQueue to find a job that matches @param
	 * if found, set this job's inDrum to be false and remove it from the list
	 * @param jobNumber
	 */
	static void removeReadyJob(int jobNumber) {
		if(!listReadyQue.isEmpty()) {
			for (int i = 0; i < listReadyQue.size(); i++) {
				ReadyJob temp = listReadyQue.get(i);
				if (temp.getJobNumber() == jobNumber) {
					//System.out.println("\nremoving a job # " + jobNumber + " from ReadyQue");
					listReadyQue.get(i).outOfDrum();
					listReadyQue.remove(i);
					return;
				}
			}
		}
	}

	/**
	 * setAJobToRun
	 * 1. set the usedCPUTime for the previously running job
	 * 2. find a job to run
	 * 2. yes -> place this job into jobLeftForSOS list & set the time left for sos for this job
	 * 2. No -> set a[0] = 1, which means there is no jobs to run
	 * @param a
	 * @param p
	 */
	static void setAJobToRun(int [] a, int[] p) {
		os_setUsedCPUTime(p[5]);

		if (!listReadyQue.isEmpty()) {
			int i = 0;
			while (i < listReadyQue.size()) {
				ReadyJob jobToBeRun = listReadyQue.get(i);
				if (!jobToBeRun.isBlocked() && jobToBeRun.isInDrum() && !jobToBeRun.isWaitingForIOCompletion()) {
					jobLeftForSOS.add(jobToBeRun);
					jobToBeRun.setTimeLeftForSOS(p[5]);
					System.out.println("\nsetAJobToRun: We are going to run a job #" +jobToBeRun.getJobNumber());
					runReadyJob(jobToBeRun, a, p);
					return;
				} else {
					//System.out.println("\njob #" + jobToBeRun.getJobNumber() + " is blocked");
					i++;
				}
			}
		}
		a[0] = 1;
	}

	/**
	 * os_setUsedCPUTime
	 * 1. set the first job in jobLeftForSOS's usedCPUTime
	 * 2. remove this job
	 */
	static void os_setUsedCPUTime(int currentTime){
		if(!jobLeftForSOS.isEmpty()) {
			ReadyJob job = jobLeftForSOS.getFirst();
			job.addUsedCPUTime(currentTime - job.getTimeLeftForSOS());
			jobLeftForSOS.remove(0);
		}
	}

	/**
	 * runReadyJob
	 * 1. sets all the information about the jobToRun sent from setAJobToRun method into p
	 * 2. set a[0] = 2, which means there is a job to run
	 * @param jobToRun
	 * @param a
	 * @param p
	 */
	static void runReadyJob(ReadyJob jobToRun, int [] a, int [] p){
			//System.out.println("\nrunReadyJob job left for sos last was " + "job #" +jobLeftForSOS);
			p[1] = jobToRun.getJobNumber();
			p[2] = jobToRun.getStartingAddress();
			p[3] = jobToRun.getJobSize();
			p[4] = jobToRun.getRemainingCPUTime();
			a[0] = 2;
	}
    
	static ReadyJob getReadyJob(int jobNumber){
		//System.out.println("\ngetReadyJob");
		ReadyJob temp = new ReadyJob();
		for (int i = 0; i < listReadyQue.size(); i++) {
			if (listReadyQue.get(i).getJobNumber() == jobNumber) {
				return listReadyQue.get(i);
			}
		}
		//System.out.println("\ngetReadyJob: job # " + jobNumber + " DNE");
		return null;
	}
    
    
	static void printReadyQue(){
		//System.out.println("\nReadyQue has:");
		ReadyJob temp;
		for (int i = 0; i < listReadyQue.size(); i++) {
			temp = listReadyQue.get(i);
            temp.displayContents();
			//System.out.println("next -> ");
		}
	}
    
	static void printWaitQue(){
		//System.out.println("\nWaitQue has:");
		ReadyJob temp;
		for (int i = 0; i < waitingQueue.size(); i++) {
			temp = waitingQueue.get(i);
			temp.displayContents();
			System.out.print(" " + temp.getJobNumber() + " -> ");
		}
	}

	/*
	static void printIOWaitQueue(){
		//System.out.println("\nIOWaitQueue has:");
		ReadyJob temp;
		for (int i = 0; i < IOWaitQueue.size(); i++) {
			temp = IOQueue.get(i);
			temp.displayContents();
			//System.out.print(" " + temp.getJobNumber() + " -> ");
		}
	}*/

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

	static ReadyJob findAJobToSwap(int a[], int p[], int newJobSize) {
		int remainingCPUTime = 0;
		ReadyJob jobToBeSwapped = BAD_JOB;
		int i = 0;
		while (i < listReadyQue.size()) {
            jobToBeSwapped = listReadyQue.get(i);
			remainingCPUTime = jobToBeSwapped.getCPUTime() - jobToBeSwapped.getUsedCPUTime();
			if (remainingCPUTime > p[4] && canFit(newJobSize, jobToBeSwapped.getJobNumber()) && !jobToBeSwapped.isLatched())
                return jobToBeSwapped;
			else
                i++;
		}
		return BAD_JOB;
	}
*/
	/**
	 * lookForIO method


    static void lookForIO(){
		//System.out.println("\nlookForIO :");
        if(!IOWaitQueue.isEmpty() && !diskBusy){
			//System.out.println("\nlookForIO 1:");
            ReadyJob mJob = IOWaitQueue.get(0);

            if(mJob != null && memoryList != null && memoryList.get(mJob.getJobNumber()) != null)
            if(inReadyQue(mJob.getJobNumber()))
            	if(memoryList.get(mJob.getJobNumber()).needsMoreIO() > 0) {
				//System.out.println("\nlookForIO2: ");
				sos.siodisk(mJob.getJobNumber());
				jobToBeInIO = mJob.getJobNumber();
				IOWaitQueue.pop();
				diskBusy = true;
			}
        }
    }
	 */

    static boolean canFit(int size, int jobNum){
        MemoryList tmp = memoryList.copy(memoryList);
        //tmp = new MemoryList(memoryList);
        if(!tmp.isEmpty()) {
			tmp.remove(jobNum);
			if (tmp.add(-1, size) != -1)
				return true;
			else return false;
		}
		return false;
    }


	/**
	 * sendAJobToDrum
	 * 1. sends a job to drum to be placed into memory
	 * 2. creates a new job instance (called ReadyJob, not ready actually)
	 * 3. adds this job into jobSentToDrumQueue
	 * 4. sets drumBusy to be true
	 * (This method is used for jobs that have originally been placed into waitingQueue)
	 * @param toBeSent
	 */
	static void sendAJobToDrum(ReadyJob toBeSent){
		sos.siodrum(toBeSent.getJobNumber(), toBeSent.getJobSize(), toBeSent.getStartingAddress(), 0);
		jobSentToDrum.add(toBeSent);
		//System.out.println("\njobToBeInDrum is job #" + jobToBeInDrum + " is added to ReadyQue with starting address at " + toBeSent.getStartingAddress());
		drumBusy = true;
	}

	/**
	 * sendAJobToDrum
	 * 1. sends a job to drum to be placed into memory
	 * 2. creates a new job instance (called ReadyJob, not ready actually)
	 * 3. adds this job into jobSentToDrumQueue
	 * 4. sets drumBusy to be true
	 * (This method is used for jobs that have never been placed in waitqueue in the first place_
	 * @param p
	 */
	static void sendAJobToDrum(int p[], int address){
		sos.siodrum(p[1], p[3], address, 0);
		ReadyJob job = new ReadyJob(p[1], p[2], p[3], p[4], p[5], address);
		jobSentToDrum.add(job);
		//System.out.println("\njobToBeInDrum is job #" + jobToBeInDrum + " is added to ReadyQue with starting address at " + toBeSent.getStartingAddress());
		drumBusy = true;
	}

	/**
	 * sendAJobToDrum
	 * if there is a job that fits in memory from waitingQueue, run it
	 * (this method is used by Tro)
	 */
	static void sendAJobToDrum(){
		if(!drumBusy) {
			ReadyJob job = pickFromWaitQueue();
			if (job != null) {
				sos.siodrum(job.getJobNumber(), job.getJobSize(), job.getStartingAddress(), 0);
				jobSentToDrum.add(job);
				drumBusy = true;
			}
		}
	}

	/**
	 * addAJobToWaitQueue
	 * 1. creates a new job instance (called ReadyJob, not ready actually)
	 * 2. adds this job to a waitingQueue
	 * @param p
	 */
	static void addAJobToWaitQueue(int p[]){
		ReadyJob job = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);
		waitingQueue.add(job);
	}

	/**
	 * sendAJobToDisk
	 * 1. send the first job in IOQueue to siodisk
	 * 1. if you send it, set diskBusy = true
	 */
	static void sendAJobToDisk(){
		if(!IOQueue.isEmpty()) {
			sos.siodisk(IOQueue.getFirst());
			diskBusy = true;
		}
	}

	/**
	 * popAJobInIOQueue
	 * 0. remove the first job from IOQueue
	 * 1. sets drumBusy to false
	 * 2. check if there is more I/O Request for the first job
	 * 2. No -> unblock
	 * 2. a if this job was done with CPU just waiting for I/O Completion, remove it from memory and ReadyJobQueue
	 * 2. Yes -> do nothing
	 *
	 */
	static void popAJobInIOQueue(){

		if(!IOQueue.isEmpty()) {
			Integer jobFromIO = IOQueue.get(0);
			IOQueue.remove(0);

			if (!isThereMoreIO(jobFromIO)) {
				getReadyJob(jobFromIO).unblock();

				if(getReadyJob(jobFromIO).isWaitingForIOCompletion()) {
					getReadyJob(jobFromIO).setWaitingForIOCompletion(false);
					memoryList.remove(jobFromIO);
					removeReadyJob(jobFromIO);
					sendAJobToDrum();
				}
			}

		}
	}

	/**
	 * addAJobToReadyQueue
	 * 1. places a job from jobSentToDrum to ReadyQueue in an appropriate space
	 * 2. set this job's inDrum = true
	 * 3. removes a job from jobSentToDrum
	 */
	static void addAJobToReadyQueue(){

		int i = 0;
		while(i < listReadyQue.size()){
			if(listReadyQue.get(i).getCPUTime() > jobSentToDrum.get(0).getCPUTime()) {
				listReadyQue.add(i, jobSentToDrum.get(0));
				jobSentToDrum.getFirst().setInDrum();
				jobSentToDrum.remove(0);
				return;
			}
			i++;
		}
		listReadyQue.add(jobSentToDrum.get(0));
		jobSentToDrum.getFirst().setInDrum();
		jobSentToDrum.remove(0);
	}

	/**
	 * pickFromWaitQueue
	 * 1. traverse a waitingQueue to see if there is any job that fits in memory
	 * 2. if there is a job that fits, set its starting address to one it just received
	 * 2. and also remove this job from waitingQueue
	 * 3. returns a job that if it fits in memory; otherwise returns null
	 * @return readyJob
	 */
	static ReadyJob pickFromWaitQueue(){
		if(!waitingQueue.isEmpty()){
			int i = 0;
			while (i < waitingQueue.size()) {
				ReadyJob waitingJob = waitingQueue.get(i);
				int startAddress = memoryList.add(waitingJob.getJobNumber(), waitingJob.getJobSize());
				if (startAddress != -1) {
					waitingJob.setStartingAddress(startAddress);
					waitingQueue.remove(waitingJob);
					return waitingJob;
				}
				i++;
			}
		}
		return null;
	}

	/**
	 * isThereMoreIO
	 * loops through the IOQueue to find a job that matches @param
	 * found -> return true ;otherwise false
	 * @param jobNumber
	 * @return
	 */
	static boolean isThereMoreIO(int jobNumber){
		if(!IOQueue.isEmpty()){
			int i = 0;
			while (i < IOQueue.size()) {
				if (IOQueue.get(i) == jobNumber) {
					return true;
				}
				i++;
			}
		}
		return false;
	}

	/*
	static void sendAJobToSwapOut(ReadyJob toBeSwappedOut){
		addToWaitingQueue(toBeSwappedOut);
    	removeReadyJob(toBeSwappedOut.getJobNumber());
		sos.siodrum(toBeSwappedOut.getJobNumber(), toBeSwappedOut.getJobSize(), toBeSwappedOut.getStartingAddress(), 1);
		swappingIn = false;
		jobToBeSwappedOut = toBeSwappedOut.getJobNumber();
		//System.out.println("\njobToBeSwapOut is job #" + jobToBeSwappedOut);
		drumBusy = true;
	}
	*/
}
