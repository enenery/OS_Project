import java.util.*;
class os{
	static MemoryList lst;
	private static LinkedList<PCB> listPCB = new LinkedList<PCB>();
    
	int TESTINT = 0;
    static void startup(){
        lst = new MemoryList();
	}
    
	static void Crint(int []a,int []p){
        System.out.println("!" + a[0] + "!");

        sos.siodrum(p[1],p[3],0,0);
        
        
	}

	static void Svc(int []a, int []p){
		System.out.println("Svc");
		switch (a[0]){
			case 5:
				removeProcess(p[1]);
				break;
		}
	
	}
	
	static void Tro(int []a, int []p){
        //If time > 0
        //Time slice ran out
        //otherwise it finished
	}
	
	static void Diskint(int []a, int []p){
	}

	static void Drmint(int []a, int []p){
	}
}
