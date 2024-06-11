import java.io.*;


public class Server
{
    public static void main(String[] args)
    {
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.server",extraArgs))
        {
            if(!extraArgs.isEmpty())
            {
                System.err.println("too many arguments");
                for(String v:extraArgs){
                    System.out.println(v);
                }
            }
            com.zeroc.Ice.ObjectAdapter printerAdapter = communicator.createObjectAdapter("Printer");
            com.zeroc.Ice.Object printerObject = new PrinterI();

            com.zeroc.Ice.ObjectAdapter masterAdapter = communicator.createObjectAdapter("MasterI");
            com.zeroc.Ice.Object masterObject = new MasterI(communicator);

            printerAdapter.add(printerObject, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
            printerAdapter.activate();

            masterAdapter.add(masterObject, com.zeroc.Ice.Util.stringToIdentity("MasterService"));
            masterAdapter.activate();
            
            communicator.waitForShutdown();
        }
    }

    public static void f(String m)
    {
        String str = null, output = "";

        InputStream s;
        BufferedReader r;

        try {
            Process p = Runtime.getRuntime().exec(m);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream())); 
            while ((str = br.readLine()) != null) 
            output += str + System.getProperty("line.separator"); 
            br.close(); 
        }
        catch(Exception ex) {
        }
    }

}