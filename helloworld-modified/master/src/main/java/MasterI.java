import Demo.*;
import com.zeroc.Ice.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MasterI implements Printer {

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private PrinterCallbackPrx currentCallback;
    private Map<String, WorkerPrx> workers = new ConcurrentHashMap<>();

    public MasterI() {
        // Initialize workers, assuming you have 5 workers
        for (int i = 0; i < 5; i++) {
            String proxy = String.format("worker%d:default -p 1000%d", i, i);
            WorkerPrx worker = WorkerPrx.checkedCast(communicator.stringToProxy(proxy));
            workers.put(proxy, worker);
        }
    }

    @Override
    public String printString(String s, double lowerLimit, double upperLimit, PrinterCallbackPrx client, Current current) {
        threadPool.execute(() -> {
            try {
                currentCallback = client;
                String response = process(s, lowerLimit, upperLimit);
                client.callbackString(response);
            } catch (Exception e) {
                e.printStackTrace();
                client.callbackString("Thread error");
            }
        });
        return "Request processed";
    }

    private String process(String s, double lowerLimit, double upperLimit) {
        String[] parts = s.split("=");
        String integralFunction = parts[1];

        // Round-robin worker selection
        String[] workerProxies = workers.keySet().toArray(new String[0]);
        String selectedWorkerProxy = workerProxies[(int) (System.currentTimeMillis() % workerProxies.length)];
        WorkerPrx worker = workers.get(selectedWorkerProxy);

        // Call worker to calculate integral
        return worker.calculateIntegral(integralFunction, lowerLimit, upperLimit);
    }
}
