package com.sp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class LocationSimulator implements Runnable {

	@Autowired
	private JmsTemplate jmsTemplate;

	private ExecutorService threadPool;

	public void run() {
		// LOAD THE GEOLOCATION FOR VEHICLES
		
		//to  map vehicleName with all its geolocation read from the file
		Map<String, List<String>> vehicleLocationMap = new HashMap<>();
		// to load the files dynamically
		ResourcePatternResolver resolver=new PathMatchingResourcePatternResolver();
		Resource[] rList=null;
		try {
			//get all the (resources) files that are under geoloc folder
			rList=resolver.getResources("geoloc/*");
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		for (Resource next : rList)
		{
			//get the location of the resource(file)
			URL resource_url=null;
			try {
				resource_url = next.getURL();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//get the file
			File file = new File(resource_url.getFile()); 
			//name of file is vehicle name
			String vehicle_name = file.getName();
			
			//get the file as input stream to read
			InputStream inputstream = SimulatorLocationApp.class.getResourceAsStream("/geoloc/" + vehicle_name);
			try (Scanner sc = new Scanner(inputstream))
			{
				List<String> thisVehicleReports = new ArrayList<>();
				//adding lat/long one line at a time
				while (sc.hasNextLine())
				{
					String latlong = sc.nextLine();
					thisVehicleReports.add(latlong);
				}
				//finally finish off by creating the map with vehicleName and list of its geolocation
				vehicleLocationMap.put(vehicle_name,thisVehicleReports);
			}
		}

		//create multiple threads for each vehicle and invoke callable on each of them (Location object)
		threadPool = Executors.newCachedThreadPool();		
		boolean isThreadPoolShutdown = false;
		do{
			List<Callable<Object>> calls = new ArrayList<>();
			int i=0;
			for (String vehicleName : vehicleLocationMap.keySet())
			{
				System.out.println("here"+(i++));
				calls.add(new Location(vehicleName, vehicleLocationMap.get(vehicleName), jmsTemplate));
			}
			
			try {
				threadPool.invokeAll(calls);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			
			//before here all the locations are read to map and sent to callable(Location) in seperate thread
			
			//if the user entered 'q', threadPool is shutdown. 
			System.out.println("Repeating again iff user hadn't terminated!");
			
		}while(isThreadPoolShutdown==true);
	}

	public void finish() 
	{
		threadPool.shutdownNow();
	}


}
