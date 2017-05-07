import java.util.LinkedList;
import java.util.ListIterator;

class MemoryList{
	LinkedList<Memory> memLst;
	public MemoryList(){
		memLst = new LinkedList<Memory>();
		memLst.add(new Memory());
    }
    
	/**
	*Frees a chunk of memory 
	* @param mem
	*Memory to be freed
	*/
	private void free(Memory mem){
		mem.free();
	}
	
    /**
    *Removes all memory chunks with size 0 or less 
    */
    private void clean(){
    	ListIterator<Memory> memIter = memLst.listIterator();
    	//Start tmp at the first value
    	Memory tmp = memIter.next();
    		//loop each value to remove any memory chunks with size 0
    		while(memIter!=null){
    			if(tmp.getSize() <= 0){
    				memLst.remove(tmp);
    			} 			
    			try{
    				tmp = memIter.next();
    			}
    			catch(Exception NoSuchElementException){
    				return;
    			}
    		}
    }
    
    /**
    *Merges any adjacent free Spaces 
    */
    private void mergeAdjacent(){
    	ListIterator<Memory> memIter = memLst.listIterator();
    	Memory toBeMerged = memIter.next();
    	boolean merge = false;
    	boolean toCheckAgain = false;
    	try{
    		//loop to see if the current memory chunk and the previous memory chunk can
    		//be merged and merge them if so
    		for(Memory tmp = toBeMerged;tmp != null;tmp = memIter.next()){
    			if(!tmp.isOccupied() && merge){
    				toBeMerged.increase(tmp.getSize());
    				memLst.set(memLst.indexOf(toBeMerged), toBeMerged);
    				memLst.remove(tmp);
    				merge = false;
    				toCheckAgain = true;
    			}else if(!tmp.isOccupied()){
    				toBeMerged = tmp;
    				merge = true;
    			}else{
    				merge = false;
    			}
    		}
    	}
    	catch(Exception NoSuchElementException){
    		//if there was a merge, another merge may be possible
    		if(toCheckAgain)
    			mergeAdjacent();
    		return;
    	}
    }
	
	/**
	*Adds a new job into Memory
	* @param jobNum
	* number of the Job
	* @param startTime
	* time when the job is added
	* @param size
	* Size of the Job
	* @return
	* The start Address of the job or -1 if job couldn't fit
	*/
    public int add(int jobNum, int size){
    	//Declares an Iterator and starts a Memory tmp at the first value
    	ListIterator<Memory> memIter = memLst.listIterator();
    	Memory tmp = memIter.next();
    	while(memIter!=null){
    		//We check if there is an unoccupied spot that fits
    		if(!tmp.isOccupied() && tmp.getSize() >= size){
    			//We add the new Memory into the LinkedList
    			memLst.add(memLst.indexOf(tmp),new Memory(jobNum,tmp.getLocation(),size,true));
    			//We change the values of the free space 
    			tmp.setSize(tmp.getSize() - size);
    			tmp.setStartAddr(tmp.getLocation() + size);
    			//We remove any empty memory chunks
    			clean();  
    			return tmp.getLocation()-size;
    		}	
    		//We move to the next memory chunk
    		try{
    			tmp = memIter.next();
    		}
    		//The exception is called at the last value for an unidentifiable reason
    		catch(Exception NoSuchElementException){
    			return -1;
    		}
    	}
		return -1;
    }
    
    /**
    * Frees a chunk of memory and if the next memory is free, merges
    * both memory chunks 
    * @param jobNum
    * The job number of the memory to be freed 
    */
    public void remove(int jobNum){
    	//Declares an Iterator and starts a Memory tmp at the first value
    	ListIterator<Memory> memIter = memLst.listIterator();
    	Memory tmp = memIter.next();
    	while(memIter!=null){
    		//We check if the job number matches
    		if(tmp.getJobNumber() == jobNum){
    			//we change the isOccupied flag to false
    			free(tmp);
    			//merge any adjacent free spaces
    			mergeAdjacent();
    			return;
    		}	
    		try{
    			tmp = memIter.next();
    		}
    		catch(Exception NoSuchElementException){
    			return;
    		}
    	}
    }
    
    /**
    *Displays all contents in the list for debugging only 
    */
    public void displayContents(){
    	System.out.println("PRINTING...");
    	ListIterator<Memory> memIter = memLst.listIterator();
    	Memory tmp = memIter.next();
    	while(memIter!=null){
    		System.out.println("//////////////////////////////");
    		System.out.println("Job: " + tmp.getJobNumber());
    		System.out.println("Size: " + tmp.getSize());
    		System.out.println("Start: " + tmp.getLocation());
    		System.out.println("Occupied: " + tmp.isOccupied());
    		System.out.println("//////////////////////////////");
    		try{ 
    			tmp = memIter.next();
    		}
    		catch(Exception NoSuchElementException){
    			return;
    		}
    	}
    } 
    
    /**
    *Searches for a job's address
    * @param jobNum
    * The number of the job
    * @return
    * The starting address of the job
    */
    public int findLocation(int jobNum){
    	ListIterator<Memory> memIter = memLst.listIterator();
    	Memory tmp = memIter.next();
    	//We loop until we get a match or until we check all values
    	while(memIter!=null){
    		//If there is a match, we return the job number
    		if(tmp.getJobNumber() == jobNum){
    			return tmp.getLocation();
    		}
    		try{
    			tmp = memIter.next();
    		}
    		catch(Exception NoSuchElementException){
    			return -1;
    		}
    	}
    	//If no job number was found, we return -1
		return -1;
    }
    
    public void changeIO(int jobNum,int mode){
    	ListIterator<Memory> memIter = memLst.listIterator();
    	Memory tmp = memIter.next();
    	while(memIter!=null){
    		
    		if(tmp.getJobNumber() == jobNum){
    			switch(mode){
    			case 0:
    				tmp.decrementIO();
    				return;
    			case 1:
    				tmp.incrementIO();
    				return;
    			}
    		}
    		try{
    			tmp = memIter.next();
    		}
    		catch(Exception NoSuchElementException){
    		}
    	}
    }
    
    public Memory get(int jobNum){
    	ListIterator<Memory> memIter = memLst.listIterator();
    	Memory tmp = memIter.next();
    	while(memIter!=null){
    		if(tmp.getJobNumber() == jobNum){
    			return tmp;
    		}
    		try{
    			tmp = memIter.next();
    		}
    		catch(Exception NoSuchElementException){
    			return null;
    		}
    	}
		return null;
    }

	public boolean isEmpty() {
		return memLst.isEmpty();
	}
}
