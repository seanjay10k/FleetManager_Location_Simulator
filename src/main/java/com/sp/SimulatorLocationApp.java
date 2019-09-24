package com.sp;

import java.io.IOException;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SimulatorLocationApp {

	public static void main(String[] args) throws IOException, InterruptedException 
	{
			//ConfigurableApplicationContext for event handling
			//The whole reason for using ConfigurableApplicationContext is that 
		try {
			ConfigurableApplicationContext ctx = SpringApplication.run(SimulatorLocationApp.class);	

			LocationSimulator sim = ctx.getBean(LocationSimulator.class);
			//LocationSimulator reads geolocations for vehicles from files. Name of vehicle is configured as name of file. 
			//Each file has list of lat/long representing the route the vehicle took
			//Runnable in LocationSimulator first reads from all available files and 
			//puts all geolocations per vehicle on a map of Map<String,List<String>>
			//then, the runnable proceeds to create the object of type Location.
			//Location implements interface Callable<Object>, so is of type Callable<Object> and represents a vehicle and all its positions(List)
			//if 2 files, then that means 2 vehicles with list of geolocation, so two Location object is created
			//then the ExecutorThreadPool of LocationSimulator invokes invokeAll method on the List of these Location Objects, 
			//on multiple threads
			//Since, Location implements Callable<Object>, threads concurrently execute the call method inside.
			//Call method for that vehicle, on that thread, gets geolocation line by line and adds to the ActiveMQ
			//there is delay in between adding to queue to give impression of location updating in real time
			Thread t= new Thread(sim);
			// LocationSimulator implements runnable. Hence, run method inside LocationSimulator is invoked when this thread is started
			// LocationSimulator leads the flow of the purpose of this application
			t.start();

			//thread created above is running. LocationSimulator and Location is doing its job. There are multiple programmatic delays implemented within them.
			// However, on the original thread, we are willing to end it all if user enters a key
			String str=null;
			Scanner sc=null;
			do {
			System.out.println("Press 'q' to terminate.");
			sc= new Scanner(System.in);
			str= sc.nextLine();
			}while(!str.equalsIgnoreCase("q"));
			
			//invoke finish method in LocationSimulator that terminates all the threadpool
			sim.finish();
		}catch(Exception e) {
			throw e;
		}
		
	}

}

