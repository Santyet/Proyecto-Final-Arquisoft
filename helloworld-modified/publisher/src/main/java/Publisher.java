// Publisher.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import Demo.ClockPrx;
import Demo.DataRequestPrx;
import com.zeroc.Ice.CommunicatorDestroyedException;

public class Publisher {
    private static String currentData;

    public static void main(String[] args) {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.pub", extraArgs)) {
            communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceStorm.clock");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> communicator.destroy()));

            status = run(communicator, extraArgs.toArray(new String[extraArgs.size()]));
        }
        System.exit(status);
    }

    public static void usage() {
        System.out.println("Usage: [--datagram|--twoway|--oneway] [topic]");
    }

    private static int run(com.zeroc.Ice.Communicator communicator, String[] args) {
        String topicName = "time";

        com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx.checkedCast(
                communicator.propertyToProxy("TopicManager.Proxy"));
        if (manager == null) {
            System.err.println("invalid proxy");
            return 1;
        }

        com.zeroc.IceStorm.TopicPrx topic;
        try {
            topic = manager.retrieve(topicName);
        } catch (com.zeroc.IceStorm.NoSuchTopic e) {
            try {
                topic = manager.create(topicName);
            } catch (com.zeroc.IceStorm.TopicExists ex) {
                System.err.println("temporary failure, try again.");
                return 1;
            }
        }

        com.zeroc.Ice.ObjectPrx publisher = topic.getPublisher();

        ClockPrx clock = ClockPrx.uncheckedCast(publisher);
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("publishing tick events. Press ^C to terminate the application.");
        try {
            System.out.print("Enter a function to integrate ('exit' to quit): \n");
            String userInput = consoleInput.readLine();
            if (!userInput.equals("exit")) {
                System.out.print("Enter lower limit: \n");
                double lowerLimit = Double.parseDouble(consoleInput.readLine());

                System.out.print("Enter upper limit: \n");
                double upperLimit = Double.parseDouble(consoleInput.readLine());
                currentData = userInput + "=" + lowerLimit + "," + upperLimit;

                clock.tick("New integration task available");
            } else {
                System.out.println("Bye bye!");
            }

            while (true) {
                try {
                    Thread.currentThread();
                    Thread.sleep(1000);
                } catch (java.lang.InterruptedException e) {
                }
            }
        } catch (CommunicatorDestroyedException | IOException ex) {
        }

        return 0;
    }

    public String getData() {
        return currentData;
    }
}
