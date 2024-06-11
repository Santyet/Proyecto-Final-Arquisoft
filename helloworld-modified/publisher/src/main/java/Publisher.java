//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import Demo.*;
import com.zeroc.Ice.CommunicatorDestroyedException;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;

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
            com.zeroc.Ice.ObjectAdapter publisherAdapter = communicator.createObjectAdapter("Master");
            com.zeroc.Ice.Object object = new MasterI();

            publisherAdapter.add(object, com.zeroc.Ice.Util.stringToIdentity("publi"));
            publisherAdapter.activate();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> communicator.destroy()));

            status = run(communicator, extraArgs.toArray(new String[extraArgs.size()]));
        }
        System.exit(status);
    }

    public static class MasterI implements Master {

        @Override
        public String getPartition(com.zeroc.Ice.Current current) {
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            return "";
        }

        @Override
        public void calculateIntervals(Current current) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'calculateIntervals'");
        }

        @Override
        public void addPartialResult(double d, Current current) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'addPartialResult'");
        }

        @Override
        public void getTask(Current current) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getTask'");
        };
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

        try
        {

            System.out.print("Enter a function to integrate ('exit' to quit): \n");

            while(message!="exit")
            {
                String userInput = consoleInput.readLine();
                if (!userInput.equals("exit")) {
                    System.out.print("Enter lower limit: \n");
                    double lowerLimit = Double.parseDouble(consoleInput.readLine());

                    System.out.print("Enter upper limit: \n");
                    double upperLimit = Double.parseDouble(consoleInput.readLine());
                    message =  userInput;

                } else {
                    System.out.println("Bye bye!");
                    break;
                }
                int x = topic.getSubscribers().length;
                System.out.println("Number of subscribers: " + x);
                clock.tick(message);
                
            
            }
        }
        catch(CommunicatorDestroyedException | IOException ex)
        {
            // Ctrl-C triggered shutdown hook, which destroyed communicator - we're terminating
        }

        return 0;
    }
}