class Memory{
    private static final int memSize = 99;
    //Variables
    private int startAddr;
    private int size;
    private boolean occupied;
    protected Memory next;
    
    //Constructors
    public Memory(int startAddr, int size, boolean occupied){
        this.startAddr = startAddr;
        this.size = size;
        next = null;
        this.occupied = occupied;
    }
    public Memory(){
        size = 100;
        startAddr = -1;
    	next = null;
    }
    
    //Methods
    void split(int size){
    	this.startAddr = this.startAddr + size;
    	this.size = this.size - size;
    }
    
    
    //Setter functions
    void setNext(Memory mem){
    	mem.next = this.next;
    	this.next = mem;
    	
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
}