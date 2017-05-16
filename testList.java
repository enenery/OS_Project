class testList{

    public static void main(String args[]){
        MemoryList tstLst = new MemoryList();
        
        tstLst.add(1,10);
        tstLst.add(2,10);
        tstLst.add(3,10);
        tstLst.add(4,10);
        tstLst.add(5,10);
        tstLst.add(6,10);
        tstLst.add(7,10);
        tstLst.add(8,10);
        tstLst.add(9,10);
        tstLst.add(0,10);
        
        tstLst.displayContents();
        
        tstLst.remove(9);
        tstLst.remove(8);
        tstLst.remove(7);
        tstLst.remove(6);
        tstLst.remove(5);
        
        tstLst.displayContents();
        
        tstLst.remove(4);
        tstLst.remove(3);
        tstLst.remove(2);
        
        tstLst.displayContents();
        
        tstLst.remove(1);
        
        tstLst.displayContents();
        
        tstLst.remove(0);
        
        tstLst.displayContents();
        
        tstLst = ;
        
        tstLst.add(1,10);
        tstLst.add(2,10);
        tstLst.add(3,10);
        tstLst.add(4,10);
        tstLst.add(5,10);
        tstLst.add(6,10);
        tstLst.add(7,10);
        tstLst.add(8,10);
        tstLst.add(9,10);
        tstLst.add(0,10);
        
        tstLst.displayContents();
        
        tstLst.remove(9);
        tstLst.remove(8);
        tstLst.remove(7);
        tstLst.remove(6);
        tstLst.remove(5);
        
        tstLst.displayContents();
        
        tstLst.remove(4);
        tstLst.remove(3);
        tstLst.remove(2);
        
        tstLst.displayContents();
        
        tstLst.remove(1);
        
        tstLst.displayContents();
        
        tstLst.remove(0);
        
        tstLst.displayContents();
    }

}