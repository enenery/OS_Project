class WaitingQue{
    LinkedList<ReadyJob> waitLst;
    
    WaitingQue(){
        waitLst = new LinkedList<ReadyJob>();
    }
    
    static boolean inWaitQue(int jobNumber){
		ReadyJob temp;
		for (int i = 0; i < waitingQueue.size(); i++) {
			temp = waitLst.get(i);
			if(temp.getJobNumber() == jobNumber)
				return true;
		}
		return false;
	}
}