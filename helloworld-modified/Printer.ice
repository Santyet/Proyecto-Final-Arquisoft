module Demo
{
    interface PrinterCallback{
        void callbackString(string s);
    }

    interface Printer{
        string printString(string s, double lowerLimit, double upperLimit, PrinterCallback* cb);
    }

    interface Clock{
        void tick(string s);
    }  

    interface Master{
        string getPartition();
        void calculateIntervals();
        void addPartialResult(double d);
        void getTask();
    };
};