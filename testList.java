public class testList {
	public static void main(String args[]){
		System.out.println("Yes");
		MemoryList lst = new MemoryList();
		System.out.println("Yes");
		lst.add(50);
		lst.add(10);
		lst.add(40);
		if(!lst.add(59))
			System.out.println("NO");
		System.out.println("Yes");
		lst.displayContents();
		System.out.println("Yes");
	}
}
