import java.util.Random; // Import the Random number generator class
import java.io.*; // Import the File class
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Instantiate the QueueingSystem class
        QueueingSystem simulation = new QueueingSystem();
        /* Specify the number of events for the timing function. */
        simulation.num_events = 2;
        try {
            /* Open input file. */
            simulation.infile = new File("mm1.in");
            Scanner reader = new Scanner(simulation.infile);
            /* Read input parameters. */
            String data = reader.nextLine();
            String[] fileValues = data.split("\\s");
            simulation.mean_interarrival = Float.parseFloat(fileValues[0]);
            simulation.mean_service = Float.parseFloat(fileValues[1]);
            simulation.num_delays_required = Integer.parseInt(fileValues[2]);
            reader.close();
            /* Write report heading and input parameters. */
            simulation.outfile = new File("mm1.out");
            FileOutputStream FileOutput = new FileOutputStream(simulation.outfile);
            BufferedWriter Buffered = new BufferedWriter(new OutputStreamWriter(FileOutput));
            Buffered.write("Single-server queueing system");
            Buffered.newLine();
            Buffered.newLine();
            Buffered.write(String.format("Mean interarrival time: %11.3f minutes", simulation.mean_interarrival));
            Buffered.newLine();
            Buffered.newLine();
            Buffered.write(String.format("Mean service time: %16.3f minutes", simulation.mean_service));
            Buffered.newLine();
            Buffered.newLine();
            Buffered.write(String.format("Number of customers: %14d\n\n", simulation.num_delays_required));
            Buffered.newLine();
            Buffered.newLine();
            Buffered.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        /* Initialize the simulation. */
        simulation.initialize();
        /* Run the simulation while more delays are still needed. */
        while (simulation.num_custs_delayed < simulation.num_delays_required) {
            /* Determine the next event. */
            simulation.timing();
            /* Update time-average statistical accumulators. */
            simulation.update_time_avg_stats();
            /* Invoke the appropriate event function. */
            switch (simulation.next_event_type) {
            case 1:
                simulation.arrive();
                break;
            case 2:
                simulation.depart();
                break;
            }
        }
        /* Invoke the report generator and end the simulation. */
        simulation.report();
    }
}