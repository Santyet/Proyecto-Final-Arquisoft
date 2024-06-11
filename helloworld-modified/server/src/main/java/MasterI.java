import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Demo.*;
import com.zeroc.Ice.ConnectionRefusedException;

public class MasterI implements Master
{

    private Queue<Task> queue = new LinkedList<>();

    private Queue<Double> globalResults = new LinkedList<>();

    private ExecutorService workers = Executors.newCachedThreadPool();

    private WorkerIPrx worker;

    private AtomicInteger workerCount = new AtomicInteger();

    private Integer currentNumOfTasks = 0;

    public MasterI(com.zeroc.Ice.Communicator communicator){
        //
        // Install shutdown hook to (also) destroy communicator during JVM shutdown.
        // This ensures the communicator gets destroyed when the user interrupts the application with Ctrl-C.
        //

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            shutdownWorkers();
            communicator.destroy();
        }));

        this.worker = run(communicator);
    }

    @Override
    public void createTask(Task task, com.zeroc.Ice.Current current){

        System.out.println("Lower: "+task.lowerLimit + " Upper: " + task.upperLimit + " number of worker: " + workerCount );

        if(workerCount.get() > 1){

            double lower = task.lowerLimit;
            double upper = task.upperLimit;
            int numOfUnits = (int) Math.abs(upper - lower) + 1;
            int interval = numOfUnits / workerCount.get();
            int numOfTasks = numOfUnits / interval;
            int numOfWorkers = workerCount.get();
            boolean flag = false;

            do{
                if ( interval >= 5 && interval <= 10 ){
                    double fakeUpper = lower + interval;
                    for(int i = 0; i < numOfTasks; i ++  ){

                        if(i == numOfTasks - 1){
                            queue.add(new Task(task.funct, lower, upper, task.approach));
                        }else {
                            queue.add(new Task(task.funct, lower, fakeUpper, task.approach));
                        }

                        lower= lower + interval;
                        fakeUpper = fakeUpper + interval;
                    }

                    flag = true;
                    currentNumOfTasks = numOfTasks;
                    launchWorkers(current);

                }else if(interval >10 || numOfWorkers == 1){

                    double fakeUpper = lower + interval;
                    for(int i = 0; i < numOfTasks; i ++  ){

                        if(i == numOfTasks - 1){
                            queue.add(new Task(task.funct, lower, upper, task.approach));
                        }else {
                            queue.add(new Task(task.funct, lower, fakeUpper, task.approach));
                        }

                        lower= lower + interval;
                        fakeUpper = fakeUpper + interval;
                    }

                    flag = true;
                    currentNumOfTasks = numOfTasks;
                    launchWorkers(current);
                }
                else {
                    numOfWorkers = numOfWorkers / 2;
                    interval = numOfUnits / numOfWorkers;
                    numOfTasks = numOfUnits / interval;
                }


            }while (!flag);

        }else{
            currentNumOfTasks = 1;
            queue.add(task);
            launchWorkers(current);
        }

    };

    @Override
    public void launchWorkers(com.zeroc.Ice.Current current){
        worker.requestTask();
    };

    @Override
    public Task getTask(com.zeroc.Ice.Current current){
        Task task;
        try {
            task = queue.remove();
        }
        catch (Exception e){
            task = null;
        }
        return task;
    };



    @Override
    public void registerWorker(com.zeroc.Ice.Current current){
        workerCount.incrementAndGet();
        System.out.println(workerCount);
    };

    public void unregisterWorker(com.zeroc.Ice.Current current){
        workerCount.decrementAndGet();
        System.out.println(workerCount);
    };


    @Override
    public void addPartialResult(double partial, com.zeroc.Ice.Current current){
        globalResults.add(partial);
    };

    @Override
    public double processPartialResults(com.zeroc.Ice.Current current){
        while (globalResults.size() != currentNumOfTasks){
            try {
                Thread.sleep(10);
            }catch (Exception e){

            }
        }
        double result = globalResults.stream().mapToDouble(Double::doubleValue).sum();
        globalResults.clear();
        return result;
    };


    public void shutdownWorkers(){
        System.out.println("Shutting down workers");
        worker.endWorkers();
    };

    private WorkerIPrx run(com.zeroc.Ice.Communicator communicator)
    {
        String topicName = "time";

        com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx.checkedCast(
                communicator.propertyToProxy("TopicManager.Proxy"));
        if(manager == null)
        {
            System.err.println("invalid proxy");
            return null;
        }

        //
        // Retrieve the topic.
        //
        com.zeroc.IceStorm.TopicPrx topic;
        try
        {
            topic = manager.retrieve(topicName);
        }
        catch(com.zeroc.IceStorm.NoSuchTopic e)
        {
            try
            {
                topic = manager.create(topicName);
            }
            catch(com.zeroc.IceStorm.TopicExists ex)
            {
                System.err.println("temporary failure, try again.");
                return null;
            }
        }

        //
        // Get the topic's publisher object, and create a Clock proxy with
        // the mode specified as an argument of this application.
        //
        com.zeroc.Ice.ObjectPrx publisher = topic.getPublisher();

        WorkerIPrx worker = WorkerIPrx.uncheckedCast(publisher);

        return worker;
    }
}
