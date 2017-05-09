import java.util.*;
class os {
	static MemoryList memoryList;
	private static LinkedList<ReadyJob> waitingQueue;
	private static LinkedList<ReadyJob> listReadyQue;
	static boolean drumBusy;
	static boolean diskBusy;
	private static final int TIME_SLICE = 1;

	static void startup() {
		memoryList = new MemoryList();
		waitingQueue = new LinkedList<ReadyJob>();
		listReadyQue  = new LinkedList<ReadyJob>();
		drumBusy = false;
		diskBusy = false;
		sos.ontrace();
	}

	static void Crint(int[] a, int[] p) {
		ReadyJob mPCB = new ReadyJob(p[1], p[2], p[3], p[4], p[5]);
		if(!drumBusy){
			int startingAddress = memoryList.add(p[1], p[3]);		
			if (startingAddress != -1) {				
				sos.siodrum(p[1], p[3], startingAddress, 0);
				listReadyQue.add(new ReadyJob(p[1],p[2],p[3],p[4],p[5],startingAddress));
				pickJob(a, p);
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
	}

	static void Svc(int[] a, int[] p) {	
		switch (a[0]) {
			case 5:
				memoryList.remove(p[1]);
				removeReadyJob(p[1]);
				if(!oneJobOrLess())
					pickJob(a,p);
				else a[0] = 1;
				break;
			case 6:
				if(!diskBusy){
					sos.siodisk(p[1]);
					diskBusy = true;
					a[0] = 2;
				}
				else{
					pickJob(a,p);
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
					pickJob(a,p);
				}	
				else{
					getReadyJob(p[1]).unblock();
					runReadyJob(a,p);
				}
				break;
		}

	}

	static void Tro(int[] a, int[] p){
		ReadyJob mReadyJob = getReadyJob(p[1]);
		mReadyJob.addUsedCPUTime(TIME_SLICE);
		if(mReadyJob.getCPUTime() <= mReadyJob.getUsedCPUTime()){
			removeReadyJob(p[1]);
			memoryList.remove(p[1]);
			a[0] = 1;
			pickJob(a,p);
		}else
			runReadyJob(a, p);
	}

	static void Dskint(int[] a, int[] p) {
		getReadyJob(p[1]).unblock();
		diskBusy = false;
		memoryList.changeIO(p[1], 0);
	}

	static void Drmint(int[] a, int[] p) {
		drumBusy = false;
		attemptAdd();
		runReadyJob(a,p);
	}

	static void removeReadyJob(int jobNumber) {
		for (int i = 0; i < listReadyQue.size(); i++) {
			ReadyJob temp = listReadyQue.get(i);
			if (temp.getJobNumber() == jobNumber) {
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
			
		}
	}
	
	static void attemptAdd(){
		if(!waitingQueue.isEmpty() && !drumBusy){
			ReadyJob newJob = waitingQueue.pop();
			ListIterator<ReadyJob> jobIter = waitingQueue.listIterator();
			try{
				while(jobIter != null){
					int startingAddress = memoryList.add(newJob.getJobNumber(), newJob.getJobSize());		
					if (startingAddress != -1) {
						listReadyQue.add(new ReadyJob(newJob.getJobNumber(),newJob.getPriority(),newJob.getJobSize(),newJob.getCPUTime(),newJob.getSubmissionTime(),startingAddress));
						sos.siodrum(newJob.getJobNumber(), newJob.getJobSize(), startingAddress, 0);
						drumBusy = true;
						break;
					}else{
						waitingQueue.add(newJob);
					}
					newJob = jobIter.next();
				}
			}catch(Exception NoSuchElementException){return;}
		}
	}
	
	static void pickJob(int [] a, int [] p){
		if(!oneJobOrLess()){
			ListIterator<ReadyJob> redIter = listReadyQue.listIterator();
			ReadyJob jobToBeRun = redIter.next();
			try{
				while(jobToBeRun != null)
					if(!jobToBeRun.isBlocked()){
						runReadyJob(jobToBeRun.getJobNumber(),jobToBeRun.getJobSize(),jobToBeRun.getStartingAddress(),a,p);
						return;
					}
					else{
						jobToBeRun = redIter.next();
					}
			}
			catch(Exception NoSuchElementException){
				printReadyQue();
				a[0] = 1;
				return;
			}
		}
	}
	
	static void runReadyJob(int jobNum, int size, int startingAddress, int [] a, int [] p){
		if (!(memoryList.isEmpty())) {
			p[1] = jobNum;
			p[2] = startingAddress;
			p[3] = size;
			p[4] = TIME_SLICE;
			a[0] = 2;
		}
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
		ReadyJob temp;
		for (int i = 0; i < listReadyQue.size(); i++) {
			temp = listReadyQue.get(i);
			temp.displayContents();
		}
	}
	
	static void printWaitQue(){
		ReadyJob temp;
		for (int i = 0; i < waitingQueue.size(); i++) {
			temp = waitingQueue.get(i);
		}
	}
	
	static boolean oneJobOrLess(){
		if(listReadyQue.size() < 2)
			return true;
		else return false;
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