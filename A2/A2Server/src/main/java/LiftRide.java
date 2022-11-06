public class LiftRide {
  private int time;
  private int liftID;
  private int waitTime;


  public LiftRide(int time, int liftID, int waitTime) {
    this.time = time;
    this.liftID = liftID;
    this.waitTime = waitTime;
  }

  public int getTime() {
    return time;
  }

  public int getLiftID() {
    return liftID;
  }

  public int getWaitTime() {
    return waitTime;
  }
}
