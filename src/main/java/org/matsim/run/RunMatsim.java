/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import java.util.*;

abstract public class RunMatsim {
	/**CUSTOM VARIABLES*/
	//private static String network_name = "network_spb_zsd_newcapasity_after_5.xml";
	private static String inputEventFile = "output_events.xml.gz";
	/**----------------*/

	private static Scenario scenario;


	public static void main(String[] args) {
		org.matsim.core.config.Config config = ConfigUtils.createConfig();

		if (args.length == 0 || args[0] == "") {
			//path to events file
			//config.network().setInputFile(String.format("scenarios/zsd/%s.", network_name));
			inputEventFile = String.format("scenarios/zsd/%s", inputEventFile);
		} else {
			//config.network().setInputFile(args[0]);
			inputEventFile = args[0];
		}

		HashSet<String> setOfMonitoringLinks = LinksReader.read("Links.csv");
		AgentsStat agentsStat = new AgentsStat(setOfMonitoringLinks);

		//Create an instance of the HandlersCollection
		final Handlers my_handlers = new Handlers(agentsStat);
		//Add collection of the events handlers

		//create an event object
		EventsManager eventsManager = EventsUtils.createEventsManager();

		eventsManager.addHandler(my_handlers);
		//create the reader and read the file
		MatsimEventsReader reader = new MatsimEventsReader(eventsManager);
		reader.readFile(inputEventFile);
		System.out.println(agentsStat.getReportTable());
		String output_file = String.format("SUMO/data.routes.csv");
		//Write data to csv. True = Append to file, false = Overwrite
		WriteToCSV.run(agentsStat.getReportTable(), output_file, false);
		agentsStat.clearBookAndReportTable();
	}
}