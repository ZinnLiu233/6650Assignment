package part1;
/*
skierID - between 1 and 100000
resortID - between 1 and 10
liftID - between 1 and 40
seasonID - 2022
dayID - 1
time - between 1 and 360
 */

import java.util.concurrent.Future;
import part2.CSVWriter;
import part2.ResponseRecord;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
  private static final int MAX_THREAD = 32;
  private static int numRequest = 0;
  private static int numSuccess = 0;
  private static int numFailure = 0;

  private static final int numSkierMax = 100000;
  private static final int numSkierMin = 0;
  private static final int numLift = 40;
  private static final int numResort = 10;
  private static final String seasonId = "2022";
  private static final String dayId = "1";

  private static final int numTotalPosts = 200000;
  private static final int numSinglePosts = 1000;
  private static BlockingQueue<ResponseRecord> responseRecords = new ArrayBlockingQueue<>(numTotalPosts);
  private static BlockingQueue<SkierThread> allThreads =  new ArrayBlockingQueue<>(300);
  private static final String baseUrl = "http://localhost:8080/A1Server_war/";
  private static final String baseUrl2 = "http://localhost:8080/A2ServerV1_war_exploded/";
  private static final String ec2Url = "http://35.93.3.155:8080/A1Server_war/";
  // http://35.93.3.155:8080/A1Server_war/skiers/12/seasons/2019/day/1/skier/123

  public static CSVWriter csvWriter;


  public static void main(String[] args) {
    CountDownLatch singleCountDown = new CountDownLatch(numSinglePosts);
    CountDownLatch totalCountDown = new CountDownLatch(numTotalPosts);

    long startTime = System.currentTimeMillis();
    process(singleCountDown, totalCountDown);
    long endTime = System.currentTimeMillis();
    long walltime = endTime - startTime;
    // show the information
    showAllinfo(walltime);

    // part2
    csvWriter = new CSVWriter(responseRecords, walltime);
    csvWriter.writeCsv();
    csvWriter.showTheRecordInfo();
  }

  private static void process(CountDownLatch singleCountDown, CountDownLatch totalCountDown){
    try{
      ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD);
      int needTask = numTotalPosts / numSinglePosts;
      // 32 Thread
      for(int i = 0; i < needTask; i++ ){
//        SkierThread skierThread = new SkierThread(numSkierMin, numSkierMax, numResort, seasonId,
//            dayId, 0, 360, numSinglePosts, numLift, singleCountDown, totalCountDown,
//            responseRecords, baseUrl);

        SkierThread skierThread = new SkierThread(numSkierMin, numSkierMax, numResort, seasonId,
            dayId, 0, 360, numSinglePosts, numLift, singleCountDown, totalCountDown,
            responseRecords, ec2Url);
        // enough for sending post
        if(totalCountDown == null){
          break;
        }
        executorService.execute(skierThread);
        allThreads.add(skierThread);
      }
      executorService.shutdown();
      executorService.awaitTermination(30, TimeUnit.SECONDS);

    }catch (InterruptedException e){
      e.printStackTrace();
    }
  }

  public static void showAllinfo(long walltime){
    for(SkierThread thread : allThreads){
      numSuccess += thread.getSuccess();
      numFailure += thread.getFailure();
    }
    long throughput = (numSuccess + numFailure) * 1000L / walltime;
    System.out.println("------------------part1 info------------------");
    System.out.println("It takes the time: " + walltime);
    System.out.println("number of successful post: " + numSuccess);
    System.out.println("number of unsuccessful post: " + numFailure);
    System.out.println("Throughput: " + throughput);
  }

  // part2, write csv
}
