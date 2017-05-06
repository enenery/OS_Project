public class ReadyJob{
    private int jobNumber;
    private int jobSize;
    private int CPUTime;
    private int startingAddress;
    private int usedCPUTime;

    public ReadyJob(int jobNumber, int jobSize, int maxCPUTime, int startingAddress){
        this.jobNumber = jobNumber;
        this.jobSize = jobSize;
        this.CPUTime = maxCPUTime;
        this.startingAddress = startingAddress;
        this.usedCPUTime = 0;
    }

    public ReadyJob(){
        this.jobNumber = -1;
        this.jobSize = -1;
        this.CPUTime = -1;
        this.startingAddress = -1;
        this.usedCPUTime = -1;
    }

    public int getJobNumber(){return jobNumber;}
    public int getJobSize(){return jobSize;}
    public int getCPUTime(){return CPUTime;}
    public int getStartingAddress(){return startingAddress;}
    public int getUsedCPUTime(){return usedCPUTime;}


    public void setJobNumber(int jobNumber){this.jobNumber = jobNumber;}
    public void setJobSize(int jobSize){this.jobSize = jobSize;}
    public void setCPUTime(int maxCPUTime){this.CPUTime = maxCPUTime;}
    public void setStartingAddress(int startingAddress){this.startingAddress = startingAddress;}
    public void setUsedCPUTime(int usedCPUTime){this.usedCPUTime = usedCPUTime;}

    public void addUsedCPUTime(int usedCPUTime){this.usedCPUTime += usedCPUTime;}


}