//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import Demo.ClockPrx;
import com.zeroc.Ice.CommunicatorDestroyedException;

public class Publisher
{
    public static void main(String[] args)
    {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        //
        // Try with resources block - communicator is automatically destroyed
        // at the end of this try block
        //
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.pub", extraArgs))
        {
            communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceStorm.clock");
            //
            // Install shutdown hook to (also) destroy communicator during JVM shutdown.
            // This ensures the communicator gets destroyed when the user interrupts the application with Ctrl-C.
            //
            Runtime.getRuntime().addShutdownHook(new Thread(() -> communicator.destroy()));

            status = run(communicator, extraArgs.toArray(new String[extraArgs.size()]));
        }
        System.exit(status);
    }

    public static void usage()
    {
        System.out.println("Usage: [--datagram|--twoway|--oneway] [topic]");
    }

    private static int run(com.zeroc.Ice.Communicator communicator, String[] args)
    {
        String topicName = "time";

        com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx.checkedCast(
                communicator.propertyToProxy("TopicManager.Proxy"));
        if(manager == null)
        {
            System.err.println("invalid proxy");
            return 1;
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
                return 1;
            }
        }

        //
        // Get the topic's publisher object, and create a Clock proxy with
        // the mode specified as an argument of this application.
        //
        com.zeroc.Ice.ObjectPrx publisher = topic.getPublisher();

        String message="";

        ClockPrx clock = ClockPrx.uncheckedCast(publisher);
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("publishing tick events. Press ^C to terminate the application.");
        try
        {
            // String client = System.getProperty("user.name") + ":" + java.net.InetAddress.getLocalHost().getHostName() + "=";

            System.out.print("Enter a function to integrate ('exit' to quit): \n");
            String userInput = consoleInput.readLine();
            if (!userInput.equals("exit")) {
                System.out.print("Enter lower limit: \n");
                double lowerLimit = Double.parseDouble(consoleInput.readLine());

                System.out.print("Enter upper limit: \n");
                double upperLimit = Double.parseDouble(consoleInput.readLine());
                message =  userInput;


                //service.printString(message,lowerLimit, upperLimit, clprx);
            } else {
                System.out.println("Bye bye!");
            }

            while(true)
            {

                clock.tick(message);
                /* clock.tick(date.format(new java.util.Date()));

                try
                {
                    Thread.currentThread();
                    Thread.sleep(1000);
                }
                catch(java.lang.InterruptedException e)
                {
                }*/
            }
        }
        catch(CommunicatorDestroyedException | IOException ex)
        {
            // Ctrl-C triggered shutdown hook, which destroyed communicator - we're terminating
        }

        return 0;
    }
}