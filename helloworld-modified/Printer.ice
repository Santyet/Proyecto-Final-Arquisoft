module Demo
{
    interface PrinterCallback
    {
        void callbackString(string s);
    }

    interface Printer
    {
        string printString(string s, long startTime, double lowerLimit, double upperLimit, int approach, PrinterCallback* cb);
    }
    class Task
        {
            string funct;
            double lowerLimit;
            double upperLimit;
            int approach;
        }

    interface Master
        {
            void createTask(Task task);
            void launchWorkers();
            Task getTask();
            void registerWorker();
            void unregisterWorker();
            void addPartialResult(double partial);
            double processPartialResults();
        }

    interface WorkerI
        {
            void requestTask();
            void processTask(Task task);
            void endWorkers();
        }
}
