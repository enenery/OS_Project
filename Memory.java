class Memory{
    //Variables
    private int startAddr;
    private int size;
    private int jobNumber;
    private boolean occupied;
    
    //Constructors
    public Memory(int jobNum, int startAddr, int size, boolean occupied){
        this.startAddr = startAddr;
        this.size = size;
        this.jobNumber = jobNum;
        this.occupied = occupied;
    }
    public Memory(){
        size = 100;
        startAddr = 0;
        jobNumber = -1;
        occupied = false;
    }
 
    //Setter functions
    void setStartAddr(int startAddr){
    	this.startAddr = startAddr;
    }
    void setSize(int size){
    	this.size = size;
    }
    void increase(int size){
    	this.size = this.size + size;
    }
    void free(){
    	this.occupied = false;
    }
    
    //Getter functions
    public int getSize(){
        return size;
    }
    public int getLocation(){
    	return startAddr;
    }
    public boolean isOccupied(){
        return occupied;
    }
    public int getJobNumber(){
    	return jobNumber;
    }
}
