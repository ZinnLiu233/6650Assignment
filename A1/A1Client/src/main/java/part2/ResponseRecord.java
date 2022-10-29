package part2;

public class ResponseRecord {
  private long startTime;
  private long latency;
  private int responseStatus;
  private String type;

  public ResponseRecord(long startTime, long latency, int responseStatus, String type) {
    this.startTime = startTime;
    this.latency = latency;
    this.responseStatus = responseStatus;
    this.type = type;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getLatency() {
    return latency;
  }

  public int getResponseStatus() {
    return responseStatus;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "ResponseRecord{" +
        "startTime=" + startTime +
        ", latency=" + latency +
        ", responseStatus=" + responseStatus +
        ", type='" + type + '\'' +
        '}';
  }
}
