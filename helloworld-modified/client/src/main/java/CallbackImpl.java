
import com.zeroc.Ice.Current;

public class CallbackImpl implements Demo.PrinterCallback{

    @Override
    public void callbackString(String s, Current current) {
        
        System.out.println("\n"+s);
    }
    
}