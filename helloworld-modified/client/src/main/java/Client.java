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
                    System.out.print("Enter a function to integrate ('exit' to quit): \n");
                    String userInput = consoleInput.readLine();
                    if (!userInput.equals("exit")) {
                        System.out.print("Enter lower limit: \n");
                        double lowerLimit = Double.parseDouble(consoleInput.readLine());

                        System.out.print("Enter upper limit: \n");
                        double upperLimit = Double.parseDouble(consoleInput.readLine());

                        System.out.print("Type integral approach: 1. Simpson 2. Romberg 3. Trapezoid : \n");
                        int approach = Integer.parseInt(consoleInput.readLine());

                        String message = client + userInput;
                        service.printString(message,lowerLimit, upperLimit, approach, clprx);
                    } else {
                        System.out.println("Bye bye!");
                        break;
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