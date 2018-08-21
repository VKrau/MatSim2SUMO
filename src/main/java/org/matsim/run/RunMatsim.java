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

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.roadpricing.ControlerDefaultsWithRoadPricingModule;
import org.matsim.roadpricing.RoadPricingConfigGroup;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

abstract public class RunMatsim {
	/**CUSTOM VARIABLES*/
	private static String fileConfig = "scenarios/zsd/config_spb_zsd_after_new.xml";
	private static boolean downsizePopulation = true;
	/**----------------*/

	private static Scenario scenario;
	private static Config config;
	private static double populationSample = 0.4;
	public static String getOutputDirectory() { return config.controler().getOutputDirectory();}


	public static void main(String[] args) {

		if ( args.length==0 || args[0]=="" ) {
			config = ConfigUtils.loadConfig(fileConfig) ;
			config.controler().setLastIteration(0);
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		} else {
			config = ConfigUtils.loadConfig(args[0]) ;
		}

		config.controler().setOutputDirectory(config.controler().getOutputDirectory() + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString().split(":")[0]);

		RoadPricingConfigGroup configRoad = ConfigUtils.addOrGetModule(config, RoadPricingConfigGroup.GROUP_NAME, RoadPricingConfigGroup.class);
		configRoad.setTollLinksFile("tolls.xml");

		scenario = ScenarioUtils.loadScenario(config);

		config.qsim().setStorageCapFactor(populationSample * 1.15);
		config.qsim().setFlowCapFactor(populationSample * 1.15);

		//Create an instance of the Controler
		Controler controler = new Controler(scenario);

		//downsample population
		if (downsizePopulation) {
			List<Id<Person>> personIdList = new LinkedList<Id<Person>>();
			for (Person person : scenario.getPopulation().getPersons().values()) {
				personIdList.add(person.getId());
			}
			drawPopulationSample(populationSample, scenario.getPopulation(), personIdList);
		}

		HashSet<String> setOfMonitoringLinks = LinksReader.read("Links.csv");
		AgentsStat agentsStat = new AgentsStat(scenario, setOfMonitoringLinks);

		//Create an instance of the HandlersCollection
		final Handlers my_handlers = new Handlers(agentsStat);
        //Add collection of the events handlers

		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.addEventHandlerBinding().toInstance(my_handlers);
			}
		});


		controler.setModules(new ControlerDefaultsWithRoadPricingModule());
		controler.addControlerListener(new IterationEndsListener() {
			@Override
			public void notifyIterationEnds(IterationEndsEvent iterationEndsEvent) {
			    System.out.println(agentsStat.getReportTable());
				Integer iter = controler.getIterationNumber();
				//String output_file = String.format("%s/ITERS/it.%d/%d.routes.csv", RunMatsim.getOutputDirectory(), iter, iter);
				String output_file = String.format("SUMO/data.routes.csv");
				//Write data to csv. True = Append to file, false = Overwrite
				WriteToCSV.run(agentsStat.getReportTable(), output_file, false);
				agentsStat.clearBookAndReportTable();
			}
		});
		config.counts().setWriteCountsInterval(1);
		//Start simulation
		controler.run();
	}

	private static void drawPopulationSample(double populationSample, Population population, List<Id<Person>> personIdList2) {
		List<Id<Person>> randomDraw = pickNRandom(personIdList2, personIdList2.size() * (1 - populationSample));
		Iterator randomDrawIterator = randomDraw.iterator();
		Integer i = 0;
		while (randomDrawIterator.hasNext()) {
			Id<Person> toRemoveId = (Id<Person>) randomDrawIterator.next();
			log.println(i+": Removing the person " + toRemoveId);
			population.removePerson(toRemoveId);
			i++;
		}
	}

	public static List<Id<Person>> pickNRandom (List < Id < Person >> lst,double n){
		List<Id<Person>> copy = new LinkedList<Id<Person>>(lst);
		Collections.shuffle(copy);
		return copy.subList(0, (int) n);
	}
}