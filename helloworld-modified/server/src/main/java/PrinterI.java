import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Demo.*;

public class PrinterI implements Printer {

    private PrinterCallbackPrx currentCallback;
    private Map<String, PrinterCallbackPrx> clients = new ConcurrentHashMap<>();
    private java.util.concurrent.Executors Executors;
    private ExecutorService threadPool = Executors.newFixedThreadPool(6);

    private String username;

    public String printString(String s, PrinterCallbackPrx client, com.zeroc.Ice.Current current) {

        Runnable run = new Thread(() -> {
            try {
                currentCallback = client;
                String newClient = s.split("=")[0];
                username = newClient.split(":")[1];
                clients.putIfAbsent(username, client);

                String response = process(s);

                client.callbackString(response);

            } catch (Exception e) {
                e.printStackTrace();
                client.callbackString("Thread error");
            }

        });
        threadPool.execute(run);
        return "Request processed";
    }

    private String process(String s) {

        String[] parts = s.split("=");
        try {
            String check = parts[1];
            try {

                if (Integer.parseInt(parts[1]) > 0) {
                    int num = Integer.parseInt(parts[1]);

                    System.out.println(num);

                    System.out.println("Fibonacci Series: ");
                    for (int i = 0; i < num; i++) {
                        int fibonacciNumber = fibonacci(i);
                        System.out.println(fibonacciNumber + " ");
                    }

                    String primeFactors = getPrimeFactors(num);
                    return ("Prime Factors: " + primeFactors);
                } else {
                    System.out.println(s);
                }

            } catch (NumberFormatException e) {
                System.out.println(s);
            }
            try {
                if (parts[1].startsWith("listifs")) {
                    StringBuilder resultado = new StringBuilder();
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface iface = interfaces.nextElement();
                        resultado.append(iface + " / ");
                    }
                    System.out.println(resultado);
                    return resultado + "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (parts[1].startsWith("!")) {
                String command = parts[1].substring(1);
                String result = executeCommand(command);

                System.out.println(" - Command Result: ");
                System.out.println(parts[0] + result);

                System.out.println(parts[0] + "Command Result: " + result);

                return "Command result: " + result;
            }
            if (parts[1].startsWith("listports")) {

                String command = parts[1].split(" ")[1];
                String result = executeCommand("nmap " + command);

                System.out.println(parts[0] + "\n List of ports of " + command + ":");

                return result;
            }
            if (parts[1].startsWith("list clients")) {
                return getClientsList();
            }
            if (parts[1].startsWith("BC")) {
                return broadcast(parts[1]);
            }
            if (parts[1].startsWith("to")) {
                String name = parts[1].split(" ")[1];
                return sendToClient(parts[1], name);
            }
            if (parts[1].equals("exit")) {
                clients.remove(username);
                System.out.println("User " + username + " removed");
                System.out.println("Remaining clients: " + clients);
            }
        } catch (Exception e) {
            System.out.println("Exception");
        }
        return "";
    }

    private static int fibonacci(int n) {
        if (n <= 1) {
            return n;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }

    private static String getPrimeFactors(int number) {
        StringBuilder primeFactors = new StringBuilder();

        for (int i = 2; i <= number; i++) {
            while (number % i == 0) {
                primeFactors.append(i).append(" ");
                number /= i;
            }
        }

        return primeFactors.toString().trim();
    }

    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }

    private String getClientsList() {
        StringBuilder serverMessage = new StringBuilder();
        for (Map.Entry<String, PrinterCallbackPrx> entry : clients.entrySet()) {
            serverMessage.append(entry.getKey()).append("\n");
        }

        return serverMessage.toString();
    }

    private String broadcast(String s) {
        StringBuilder serverMessage = new StringBuilder();

        for (Map.Entry<String, PrinterCallbackPrx> entry : clients.entrySet()) {
            if (entry.getValue() != currentCallback) {
                Demo.PrinterCallbackPrx otherClient = entry.getValue();
                serverMessage.append("Message sent to ").append(entry.getKey()).append("\n");
                otherClient.callbackString("Broadcast message: " + s);
            }
        }

        return serverMessage.toString();
    }

    public String sendToClient(String s, String key) {
        StringBuilder messageTo = new StringBuilder();
        boolean found = false;
        for (Map.Entry<String, PrinterCallbackPrx> entry : clients.entrySet()) {
            if (entry.getKey().equals(key)) {
                found = true;
                Demo.PrinterCallbackPrx toClient = entry.getValue();
                messageTo.append("Message sent to ").append(entry.getKey()).append("\n");
                toClient.callbackString("Someone whispered: " + s);
                break;
            }
        }
        if (!found) {
            messageTo.append("User not found");
        }
        return messageTo.toString();
    }

}