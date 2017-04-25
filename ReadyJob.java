public class ReadyJob{
    private int jobNumber;
    private int jobSize;
    private int CPUTime;
    private int startingAddress;

    public ReadyJob(int jobNumber, int jobSize, int maxCPUTime, int startingAddress){
        this.jobNumber = jobNumber;
        this.jobSize = jobSize;
        this.CPUTime = maxCPUTime;
        this.startingAddress = startingAddress;
    }

    public int getJobNumber(){return jobNumber;}
    public int getJobSize(){return jobSize;}
    public int getCPUTime(){return CPUTime;}
    public int getStartingAddress(){return startingAddress;}


    public void setJobNumber(int jobNumber){this.jobNumber = jobNumber;}
    public void setJobSize(int jobSize){this.jobSize = jobSize;}
    public void setCPUTime(int maxCPUTime){this.CPUTime = maxCPUTime;}
    public void setStartingAddress(int startingAddress){this.startingAddress = startingAddress;}


}