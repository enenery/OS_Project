import java.util.*;
class os {
	static MemoryList memoryList;
	private static LinkedList<ReadyJob> waitingQueue;
	private static LinkedList<ReadyJob> listReadyQue;
	static int i = 0;
	static boolean drumBusy;

	private static final int TIME_SLICE = 1;

	static void startup() {
		memoryList = new MemoryList();
		waitingQueue = new LinkedList<ReadyJob>();
		listReadyQue  = new LinkedList<ReadyJob>();
		drumBusy = false;
		sos.ontrace();
	}

	static void Crint(int[] a, int[] p) {
		System.out.println("CRINT");
		i++;
		ReadyJob mPCB = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);
		
		if(!drumBusy){
			int startingAddress = memoryList.add(p[1], p[3]);		
			if (startingAddress != -1) {
				sos.siodrum(p[1], p[3], startingAddress, 0);
				pickJob(a, p);
				listReadyQue.add(new ReadyJob(p[1],p[2],p[3],p[4],p[5],startingAddress));
				drumBusy = true;
			}else{
				waitingQueue.add(mPCB);
				pickJob(a,p);
			}
		}
		else{
			waitingQueue.add(mPCB);
			pickJob(a,p);
		}
		memoryList.displayContents();
	}

	static void Svc(int[] a, int[] p) {
		System.out.println("SVC " + a[0]);
	
		switch (a[0]) {
			case 5:
				System.out.println("\nSvc: a=5");
				memoryList.remove(p[1]);
				removeReadyJob(p[1]);
				a[0] = 1;
				break;
			case 6:
				System.out.println("\nSvc: a=6");
				sos.siodisk(p[1]);
				memoryList.changeIO(p[1], 1);
				a[0] = 2;
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
		System.out.println("TRO");

		System.out.println("\nTRO: " + "job #" + p[1] + " was running");
		ReadyJob mReadyJob = getReadyJob(p[1]);
		mReadyJob.addUsedCPUTime(TIME_SLICE);

		System.out.println("\nmaxCPUTime = " + mReadyJob.getCPUTime() +
		"\nusedCPUTime = " + mReadyJob.getUsedCPUTime());
		if(mReadyJob.getCPUTime() <= mReadyJob.getUsedCPUTime()){
			removeReadyJob(p[1]);
			memoryList.remove(p[1]);
			a[0] = 1;
			pickJob(a,p);
		}else
			runReadyJob(a, p);
		
	}

	static void Dskint(int[] a, int[] p) {
		System.out.println("\nDsk" + a[0]);
		memoryList.changeIO(p[1], 0);
	}

	static void Drmint(int[] a, int[] p) {
		System.out.println("DRUMINT");

		drumBusy = false;
		
		if(!waitingQueue.isEmpty()){
			ReadyJob newJob = waitingQueue.pop();
			ListIterator<ReadyJob> jobIter = waitingQueue.listIterator();
			
				try{
					while(jobIter != null){
						int startingAddress = memoryList.add(newJob.getJobNumber(), newJob.getJobSize());		
						if (startingAddress != -1) {
							sos.siodrum(newJob.getJobNumber(), newJob.getJobSize(), startingAddress, 0);
							drumBusy = true;
							break;
						}else{
							waitingQueue.add(newJob);
						}
						newJob = jobIter.next();
					}
				}
				catch(Exception NoSuchElementException){
				}
		}
		runReadyJob(a,p);
		
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
			Memory jobToBeRun = memoryList.get(p[1]);
			p[2] = jobToBeRun.getLocation();
			p[3] = jobToBeRun.getSize();
			p[4] = TIME_SLICE;
			a[0] = 2;
			System.out.println("\nrunning a job #" + jobToBeRun.getJobNumber() +
							"\nstartAddress = " + p[2] +
							"\njobSize = " + p[3]);
		}else
			System.out.println("\nEmpty ReadyQue");
	}
	
	static void pickJob(int [] a, int [] p){
		ReadyJob jobToBeRun;
		if(!listReadyQue.isEmpty()){
			 jobToBeRun = listReadyQue.getFirst();
			 runReadyJob(jobToBeRun.getJobNumber(),jobToBeRun.getJobSize(),jobToBeRun.getStartingAddress(),a,p);
		}
		
	}
	static void runReadyJob(int jobNum, int size, int startingAddress, int [] a, int [] p){
		if (!(memoryList.isEmpty())) {
			p[1] = jobNum;
			p[2] = startingAddress;
			p[3] = size;
			p[4] = TIME_SLICE;
			a[0] = 2;
			System.out.println("\nrunning a job #" + jobNum +
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