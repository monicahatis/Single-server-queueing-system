
/* External definitions for single-server queueing system. */
import java.util.Random; // Import the Random number generator class
import java.io.*; // Import the File class
import java.lang.Math; // Import the Math class

public class QueueingSystem {
    // final means that the values are constant and cannot be changed.
    // static means that the variable belongs to the class as a whole.
    // Limit on the queue length
    public final int Q_LIMIT = 100;
    // Mnemonics for the server being busy
    public final int BUSY = 1;
    // and idle
    public final int IDLE = 0;
    // Instance variables
    public int next_event_type;
    public int num_custs_delayed;
    public int num_delays_required;
    public int num_events;
    public int num_in_q;
    public int server_status;
    public double area_num_in_q;
    public double area_server_status;
    public double mean_interarrival;
    public double mean_service;
    public double sim_time;
    public double[] time_arrival = new double[Q_LIMIT + 1];
    public double time_last_event;
    public double[] time_next_event = new double[3];
    public double total_of_delays;
    public File infile;
    public File outfile;

    // Constructor
    public QueueingSystem() {

    }

    public void initialize() {
        /* Initialize the simulation clock. */
        this.sim_time = 0.0;
        /* Initialize the state variables. */
        this.server_status = this.IDLE;
        this.num_in_q = 0;
        this.time_last_event = 0.0;
        /* Initialize the statistical counters. */
        this.num_custs_delayed = 0;
        this.total_of_delays = 0.0;
        this.area_num_in_q = 0.0;
        this.area_server_status = 0.0;
        /*
         * Initialize event list. Since no customers are present, the departure (service
         * completion) event is eliminated from consideration.
         */
        this.time_next_event[1] = this.sim_time + this.expon(this.mean_interarrival);
        this.time_next_event[2] = Math.pow(10, 30);
    }

    public void timing() {
        int i;
        double min_time_next_event = Math.pow(10, 29);
        this.next_event_type = 0;
        /* Determine the event type of the next event to occur. */
        for (i = 1; i <= this.num_events; ++i)
            if (this.time_next_event[i] < min_time_next_event) {
                min_time_next_event = this.time_next_event[i];
                this.next_event_type = i;
            }
        /* Check to see whether the event list is empty. */
        if (this.next_event_type == 0) {
            /* The event list is empty, so stop the simulation. */
            try {
                this.outfile = new File("mm1.out");
                FileOutputStream FileOutput = new FileOutputStream(this.outfile);
                BufferedWriter Buffered = new BufferedWriter(new OutputStreamWriter(FileOutput));
                Buffered.newLine();
                Buffered.write(String.format("Event list empty at time %f", this.sim_time));
                Buffered.close();
            } catch (Exception e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            System.exit(1);
        }
        /* The event list is not empty, so advance the simulation clock. */
        this.sim_time = min_time_next_event;
    }

    public void arrive() {
        double delay;
        /* Schedule next arrival. */
        this.time_next_event[1] = this.sim_time + this.expon(this.mean_interarrival);
        /* Check to see whether server is busy. */
        if (this.server_status == this.BUSY) {
            /* Server is busy, so increment number of customers in queue. */
            ++this.num_in_q;
            /* Check to see whether an overflow condition exists. */
            if (this.num_in_q > this.Q_LIMIT) {
                try {
                    this.outfile = new File("mm1.out");
                    FileOutputStream FileOutput = new FileOutputStream(this.outfile);
                    BufferedWriter Buffered = new BufferedWriter(new OutputStreamWriter(FileOutput));
                    Buffered.newLine();
                    Buffered.write(String.format("Overflow of the array time_arrival at time %f", this.sim_time));
                    Buffered.close();
                } catch (Exception e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
                System.exit(2);
            }
            /*
             * There is still room in the queue, so store the time of arrival of the
             * arriving customer at the (new) end of time_arrival.
             */
            this.time_arrival[this.num_in_q] = this.sim_time;
        } else {
            /*
             * Server is idle, so arriving customer has a delay of zero. (The following two
             * statements are for program clarity and do not affect the results of the
             * simulation.)
             */
            delay = 0.0;
            this.total_of_delays += delay;
            /* Increment the number of customers delayed, and make server busy. */
            ++this.num_custs_delayed;
            this.server_status = this.BUSY;
            /* Schedule a departure (service completion). */
            this.time_next_event[2] = this.sim_time + this.expon(this.mean_service);
        }

    }

    public void depart() {
        int i;
        double delay;
        /* Check to see whether the queue is empty. */
        if (this.num_in_q == 0) {
            /*
             * The queue is empty so make the server idle and eliminate the departure
             * (service completion) event from consideration.
             */
            this.server_status = this.IDLE;
            this.time_next_event[2] = Math.pow(10, 30);
        } else {
            /* The queue is nonempty, so decrement the number of customers in queue. */
            --this.num_in_q;
            /*
             * Compute the delay of the customer who is beginning service and update the
             * total delay accumulator.
             */
            delay = this.sim_time - this.time_arrival[1];
            this.total_of_delays += delay;
            /* Increment the number of customers delayed, and schedule departure. */
            ++this.num_custs_delayed;
            this.time_next_event[2] = this.sim_time + this.expon(this.mean_service);
            /* Move each customer in queue (if any) up one place. */
            for (i = 1; i <= this.num_in_q; ++i)
                this.time_arrival[i] = this.time_arrival[i + 1];
        }
    }

    public void report() {
        /* Compute and write estimates of desired measures of performance. */
        try {
            this.outfile = new File("mm1.out");
            FileOutputStream FileOutput = new FileOutputStream(this.outfile);
            BufferedWriter Buffered = new BufferedWriter(new OutputStreamWriter(FileOutput));
            Buffered.newLine();
            Buffered.newLine();
            Buffered.write(String.format("Average delay in queue: %11.3f minutes",
                    this.total_of_delays / this.num_custs_delayed));
            Buffered.newLine();
            Buffered.newLine();
            Buffered.write(String.format("Average number in queue: %10.3f", this.area_num_in_q / this.sim_time));
            Buffered.newLine();
            Buffered.newLine();
            Buffered.write(String.format("Server utilization: %15.3f", this.area_server_status / this.sim_time));
            Buffered.newLine();
            Buffered.newLine();
            Buffered.write(String.format("Time simulation ended: %12.3f minutes", this.sim_time));
            Buffered.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void update_time_avg_stats() {
        double time_since_last_event;
        /* Compute time since last event, and update last-event-time marker. */
        time_since_last_event = this.sim_time - this.time_last_event;
        this.time_last_event = this.sim_time;
        /* Update area under number-in-queue function. */
        this.area_num_in_q += this.num_in_q * time_since_last_event;
        /* Update area under server-busy indicator function. */
        this.area_server_status += this.server_status * time_since_last_event;
    }

    public double expon(double mean) {
        /* Return an exponential random variate with mean "mean". */
        Random rand = new Random();
        return -mean * Math.log(rand.nextDouble());
    }

}