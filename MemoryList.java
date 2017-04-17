class MemoryList{
    protected Memory start;
    protected Memory end;
    
    protected int lstSize;
    
    public MemoryList(){
        lstSize = 1;
        this.start = new Memory(0,100,false);
    }
    
    public boolean add(int size){
        Memory tmp = start;
        Memory minMem = new Memory();
        boolean foundSpot = false;
        
        while(tmp != null){
        	if(tmp.getSize() >= size && !tmp.isOccupied()){
        		if(minMem.getSize() >= tmp.getSize()){
        			minMem = tmp;
        		}
        	foundSpot = true;
        	}
        	tmp = tmp.next;
        }
        
        if(foundSpot){
        	minMem.setNext(new Memory(minMem.getLocation(),size,true));
        	minMem.split(size);
        	lstSize++;
        	return true;
        }
        lstSize++;
        return false;
    }
    
    public int getSize(Memory mem){
    	return mem.getSize();
    }
    
    public void displayContents(){
    	Memory tmp = start;
    	int x = 1;
    	while(tmp != null){
    		System.out.println("Element:" + x);
    		System.out.println("Size: " + tmp.getSize());
    		System.out.println("Address: " + tmp.getLocation());
    		System.out.println(tmp.isOccupied());
    		tmp = tmp.next;
    		x++;
    	}
    }
    
    int getSize(){
    	return lstSize;
    }
}