package part2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class CSVWriter {
  private BlockingQueue<ResponseRecord> responseRecords;
  private long walltime;
  public static int numRequests = 200000;

  public CSVWriter(BlockingQueue<ResponseRecord> responseRecords, long walltime) {
    this.responseRecords = responseRecords;
    this.walltime = walltime;
  }

  public BlockingQueue<ResponseRecord> getResponseRecords() {
    return responseRecords;
  }

  public void setResponseRecords(BlockingQueue<ResponseRecord> responseRecords) {
    this.responseRecords = responseRecords;
  }

  public long getWalltime() {
    return walltime;
  }

  public void setWalltime(long walltime) {
    this.walltime = walltime;
  }

  public void writeCsv() {
    File csvfile = new File("src/part2Records.csv");
    try {
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(csvfile, true));
      bufferedWriter.write("startTime:" + "," + "requestType" + ","
          + "latency" + "," + "responseCode");
      bufferedWriter.newLine();
      for (ResponseRecord each : this.getResponseRecords()) {
        String eachRecord = each.getStartTime() + "," + each.getType() + "," +
            each.getLatency() + "," + each.getResponseStatus();
        bufferedWriter.write(eachRecord);
        bufferedWriter.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

    public void showTheRecordInfo(){
      long totalTime = 0;
      List<Long> latencies = new ArrayList<>();

      for(ResponseRecord record : responseRecords){
        long eachLatency = record.getLatency();
        totalTime += eachLatency;
        latencies.add(eachLatency);
      }

      Collections.sort(latencies);
      long medianOfLatency = 0;
      if(latencies.size() % 2 == 0){
        medianOfLatency = (latencies.get(latencies.size() / 2) + latencies.get(latencies.size()
        / 2 - 1)) / 2;
      }else{
        medianOfLatency = latencies.get(latencies.size() / 2);
      }

      long meanResponseTime = totalTime / latencies.size();
      long throughput = 1000L * numRequests / getWalltime();
      long p99 = latencies.get((int)(latencies.size() * 0.99));
      System.out.println("------------------part2 info------------------");
      System.out.println("mean response time : " + meanResponseTime + "ms");
      System.out.println("median response time : " + medianOfLatency + "ms");
      System.out.println("throughput (requests/second) : " + throughput);
      System.out.println("p99 response time: " + p99 + "ms");
      System.out.println("min response time: " + latencies.get(0) + "ms");
      System.out.println("max response time: " + latencies.get(latencies.size() - 1) + "ms");
    }
}
