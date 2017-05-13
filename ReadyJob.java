public class ReadyJob{
    private int jobNumber;
    private int jobSize;
    private int priority;
    private int submissionTime;
    private int CPUTime;
    private int startingAddress;
    private int usedCPUTime;
    private boolean blocked;
    private boolean inDrum;

    public ReadyJob(int jobNumber,int priority, int jobSize, int maxCPUTime,int submissionTime, int startingAddress){
        this.jobNumber = jobNumber;
        this.jobSize = jobSize;
        this.priority = priority;
        this.submissionTime = submissionTime;
        this.CPUTime = maxCPUTime;
        this.startingAddress = startingAddress;
        this.usedCPUTime = 0;
        blocked = false;
        inDrum = false;
    }
    
    public ReadyJob(int jobNumber, int priority, int jobSize, int maxCPUTime, int submissionTime){
        this.jobNumber = jobNumber;
        this.priority = priority;
        this.jobSize = jobSize;
        this.CPUTime = maxCPUTime;
        this.submissionTime = submissionTime;
        blocked = false;
        inDrum = false;
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
    public int getPriority(){return priority;}
    public int getSubmissionTime(){return submissionTime;}
    public boolean isBlocked(){return blocked;}
    public boolean isInDrum(){return inDrum;}
    
    public void setJobNumber(int jobNumber){this.jobNumber = jobNumber;}
    public void setJobSize(int jobSize){this.jobSize = jobSize;}
    public void setCPUTime(int maxCPUTime){this.CPUTime = maxCPUTime;}
    public void setStartingAddress(int startingAddress){this.startingAddress = startingAddress;}
    public void setUsedCPUTime(int usedCPUTime){this.usedCPUTime = usedCPUTime;}
    public void setPriority(int priority){this.priority = priority;}
    public void setSubmissionTime(int submissionTime){this.submissionTime = submissionTime;}
    public void addUsedCPUTime(int usedCPUTime){this.usedCPUTime += usedCPUTime;}
    public void block(){this.blocked = true;}
    public void unblock(){this.blocked = false;}

    public void setInDrum(){this.inDrum = true;}
    public void outOfDrum(){this.inDrum = false;}

}