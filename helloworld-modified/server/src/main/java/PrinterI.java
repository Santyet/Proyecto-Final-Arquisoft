import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.UnivariateFunction;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import Demo.*;

public class PrinterI implements Printer {

    private PrinterCallbackPrx currentCallback;
    private java.util.concurrent.Executors Executors;
    private ExecutorService threadPool = Executors.newFixedThreadPool(12);
    private String username;

    public String printString(String s, double lowerLimit, double upperLimit, int approach, PrinterCallbackPrx client, com.zeroc.Ice.Current current) {

        com.zeroc.Ice.Communicator communicator = current.adapter.getCommunicator();
        Demo.MasterPrx service = Demo.MasterPrx.checkedCast(communicator.propertyToProxy("MasterI.Proxy"));
        if (service == null) {
            throw new Error("Invalid proxy");
        }
        Runnable run = new Thread(() -> {
            try {
                currentCallback = client;
                String response = processInput(s, lowerLimit, upperLimit, approach, client, service, current);
                client.callbackString(response);

            } catch (Exception e) {
                e.printStackTrace();
                client.callbackString("Thread error");
            }

        });
        threadPool.execute(run);
        return "Request processed";
    }

    private String processInput(String s, double lowerLimit, double upperLimit, int approach, PrinterCallbackPrx client, Demo.MasterPrx service, com.zeroc.Ice.Current current) {
        String[] parts = s.split("=");
        Task task = new Task(parts[1], lowerLimit, upperLimit, approach);
        service.createTask(task);
        double answer = service.processPartialResults();

        return "Answer: " + answer;
    }

}