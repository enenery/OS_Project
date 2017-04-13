class Memory{
    private static final int memSize = 99;
    
    private static int startAddr;
    private static int size;
    private static boolean occupied;
    
    protected Memory next;
    protected Memory prev;
    
    
    public Memory(int []p ,int startAddr, int size, int occupied){
        this.startAddr = startAddr;
        this.size = size;
        prev = null;
        next = null;
        if(occupied == 0)
            this.occupied = false;
        else this.occupied = true;
    }
    
    public Memory(){
        prev = null;
        next = null;
    }
    
    public int getSize(){
        return size;
    }
    
    public boolean isOccupied(){
        return occupied;
    }
}