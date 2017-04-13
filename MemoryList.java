class MemoryList{
    protected Memory start;
    protected Memory end;
    
    protected int lstSize;
    
    public MemoryList(){
        lstSize = 1;
        this.start = new Memory(0,100,0);
    }
    
    public int add(int size){
        int min = 0;
        Memory tmp = start;
        while(tmp != null){
            if(tmp.getSize() > min && !tmp.isOccupied() && size < tmp.getSize())
                min = tmp.getSize();
            tmp = tmp.next;
        }
        return -1;
    }
}