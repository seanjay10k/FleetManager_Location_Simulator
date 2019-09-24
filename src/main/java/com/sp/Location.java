package com.sp;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;

public class Location implements Callable<Object> 
{
	private List<String> geoLocations;
	private String vehicleName;
	private JmsTemplate jmsTemplate;

	public Location(String vehicleName, List<String> geoLocations, JmsTemplate jmsTemplate) 
	{
		this.geoLocations = geoLocations;
		this.vehicleName = vehicleName;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	public Object call() throws InterruptedException  
	{
		for (String nextReport: this.geoLocations){
			int delim=nextReport.indexOf(" ");
			// get the lat and long
			String latitude = nextReport.substring(0, delim);
			String[] lat = latitude.split("\"");
			latitude = lat[1];
			System.out.print("lat"+" : "+latitude+" & ");
			String longitude = nextReport.substring(delim+1);
			String[] lon = longitude.split("\"");
			longitude = lon[1];
			System.out.println("long"+" : "+longitude);
			//create hashmap
			HashMap<String,String> locationMessage = new HashMap<String,String>();
			locationMessage.put("vehicle", vehicleName);
			locationMessage.put("lat", latitude);
			locationMessage.put("long", longitude);
			locationMessage.put("time", new java.util.Date().toString());
			Boolean tryAgain=true;
			do{
				try {
					jmsTemplate.convertAndSend("locationQueue",locationMessage);
					tryAgain=false;
				}catch(Exception e) {
					System.out.println("Queue unavailable. waiting and trying again");
					Thread.sleep(4000);
				}
			}while(tryAgain);
			Thread.sleep((long)(Math.random() * 4000));//random wait for vehicle location to be sent to queue

		}
		System.out.println(vehicleName+" has reached destination");
		return null;
	}


}
