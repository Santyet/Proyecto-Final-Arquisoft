module Demo
{
    interface PrinterCallback
    {
        void callbackString(string s);
    }

    interface Printer
    {
        string printString(string s, PrinterCallback* cb);
    }
}
