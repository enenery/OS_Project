public class PCB{

private int jobNumber;
private int priority;
private int jobSize;
private int maxCPUTime;
private int submissionTime;

public PCB(){}

public PCB(int jobNumber, int priority, int jobSize, int maxCPUTime, int submissionTime){
    this.jobNumber = jobNumber;
    this.priority = priority;
    this.jobSize = jobSize;
    this.maxCPUTime = maxCPUTime;
    this.submissionTime = submissionTime;
}

//accesors for fields
public int getJobNumber(){return jobNumber;}
public int getPriority(){return priority;}
public int getJobSize(){return jobSize;}
public int getMaxCPUTime(){return maxCPUTime;}
public int getSubmissionTime(){return submissionTime;}

//mutators for fields
public void setJobNumber(int jobNumber){this.jobNumber = jobNumber;}
public void setPriority(int priority){this.priority = priority;}
public void setJobSize(int jobSize){this.jobSize = jobSize;}
public void setMaxCPUTime(int maxCPUTime){this.maxCPUTime = maxCPUTime;}
public void setSubmissionTime(int submissionTime){this.submissionTime = submissionTime;}

}