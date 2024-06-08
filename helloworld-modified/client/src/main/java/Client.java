import com.zeroc.Ice.ObjectPrx;

import Demo.PrinterCallbackPrx;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client
{
    public static void main(String[] args)
    {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client",extraArgs))
        {
            //com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 10000");
            Demo.PrinterPrx service = Demo.PrinterPrx
                    .checkedCast(communicator.propertyToProxy("Printer.Proxy"));
            
            if(service == null)
            {
                throw new Error("Invalid proxy");
            }
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Callback");
            com.zeroc.Ice.Object object = new CallbackImpl();
            ObjectPrx prx = adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("CallbackService"));
            adapter.activate();

            PrinterCallbackPrx clprx=PrinterCallbackPrx.uncheckedCast(prx);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            try{
                String client = System.getProperty("user.name") + ":" + java.net.InetAddress.getLocalHost().getHostName() + "=";

                while (true) {
                    System.out.print("Enter message ('exit' to quit or 'atr' to show attributes'): ");
                    String userInput = consoleInput.readLine();

                    String message = client + userInput;

                    service.printString(message,clprx);

                    if ("exit".equalsIgnoreCase(userInput)) {
                        System.out.println("Bye bye!");
                        break;
                    }

                    if ("atr".equalsIgnoreCase(userInput)) {

                        int requests = 1;
                        double start = System.currentTimeMillis();
                        double end = 0;
                        int unprocessed = 0;
                        int processed = 0;
                        double latency = 0;

                        while (2000 > end - start){
                            double startRequest = System.currentTimeMillis();
                            try {
                                service.printString(client + " is testing: " + userInput, clprx);
                                processed++;
                            } catch (Exception e) {
                                unprocessed++;
                            };
                            end = System.currentTimeMillis();

                            System.out.println("\nRequest Number: " + requests);
                            requests++;
                            System.out.println("Response Time (ms): " + (end - startRequest));

                            latency += end - startRequest;
                        }

                        System.out.println("\n\n" + "Processed Requests: " + processed);
                        System.out.println("\n" + "Unprocessed Requests: " + unprocessed);
                        System.out.println("\n" + "Unprocessed Rate: " + (unprocessed/2000)*100 + "%");
                        System.out.println("\n" + "Average Response Time: " + latency/ (double) (unprocessed + processed));
                        System.out.println("\n" + "A total of " + (unprocessed + processed) + " requests were sent in 2000 ms");

                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("callback invoked");
            communicator.shutdown();
        }
    }
}