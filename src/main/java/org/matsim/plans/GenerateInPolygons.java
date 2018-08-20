package org.matsim.plans;

import com.vividsolutions.jts.geom.*;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.utils.geometry.CoordUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


import java.util.*;

import static javax.print.attribute.standard.MediaSizeName.A;

public class GenerateInPolygons {
    private Map<Person, Integer> personsToAdd = new HashMap<Person, Integer>();
    int numberOfCreatedAgents = 0;
    Map<Id<Person>, Integer> personsToClone = new HashMap<Id<Person>, Integer>();
    int numberOfTries = 0;



    public void cloneAgentsFromPolygons(
            Scenario scenario, int numberOfAgentsToCreate, MultiPolygon multiPolygon) {
        // while (numberOfAgentsToCreate > numberOfCreatedAgents){
        collectPersonsToClone(scenario, multiPolygon);
        clonePersons(scenario, numberOfAgentsToCreate);
        randomizeAndAddPersons(scenario);

    }

    private void randomizeAndAddPersons(Scenario scenario) {
        for (Person person : personsToAdd.keySet()){
            Random random = new Random();
            Person randomizedPerson = scenario.getPopulation().getFactory().createPerson(
                    Id.createPersonId(person.getId() + "_" + random.nextInt(100000000)));
            Plan newPlan = getClonedPlan(scenario.getPopulation().getFactory(), person);
            randomizedPerson.addPlan(newPlan);
            scenario.getPopulation().addPerson(randomizedPerson);
        }
        System.out.println("Added " + personsToAdd.keySet().size() + " new persons");
        personsToAdd.clear();
    }


    public void cloneAgentsFromPolygons(
            Scenario scenario, int numberOfAgentsToCreate, MultiPolygon multiPolygon, Coord targetCoord ) {
        // while (numberOfAgentsToCreate > numberOfCreatedAgents){
        collectPersonsToClone(scenario, multiPolygon);
        clonePersons(scenario, numberOfAgentsToCreate);
        modifyPlans(targetCoord);
        randomizeAndAddPersons(scenario);
        System.out.println("Added " + personsToAdd.size() + " new persons");
    }

    private void modifyPlans(Coord targetCoord) {
        for (Person person : personsToAdd.keySet()){
            modifyPlan(person.getSelectedPlan(), targetCoord, personsToAdd.get(person));
        }
    }

    private void clonePersons(Scenario scenario, int numberOfAgentsToCreate) {
        PopulationFactory populationFactory = scenario.getPopulation().getFactory();
        for (int i = 0; i < numberOfAgentsToCreate; i++){
            Random random = new Random();
            Id<Person> personId = personsToClone.keySet().stream().skip(random.nextInt(personsToClone.keySet().size())).findFirst().get();
            Person personToClone = scenario.getPopulation().getPersons().get(personId);
            Double s = Math.random();
            Person personCloned = populationFactory.createPerson(Id.createPersonId(i +
                    s.toString() + numberOfAgentsToCreate + "_" + Math.round(Math.random() * Integer.MAX_VALUE)
            ));
            Plan newPlan = getClonedPlan(
                    populationFactory, scenario.getPopulation().getPersons().get(personToClone.getId()));
            personCloned.addPlan(newPlan);
            personCloned.getAttributes().putAttribute(personCloned.getId().toString(), "subpopulation");
            personsToAdd.put(personCloned, personsToClone.get(personToClone.getId()));
        }
    }

    private Plan getClonedPlan(PopulationFactory populationFactory, Person person) {
        Plan newPlan = populationFactory.createPlan();
        Iterator elementIterator = person.getSelectedPlan().getPlanElements().iterator();
        int numberOfHandledElements = 0;
        while (elementIterator.hasNext()){
            numberOfHandledElements++;
            PlanElement planElement = (PlanElement) elementIterator.next();
            if (planElement instanceof Activity){
                String actType = ((Activity) planElement).getType();
                Coord coord = ((Activity) planElement).getCoord();
                double endTime  = ((Activity) planElement).getEndTime();
                Activity activity1 = populationFactory.createActivityFromCoord(actType, coord);
                activity1.setEndTime(endTime);
                newPlan.addActivity(activity1);
                if (numberOfHandledElements < person.getSelectedPlan().getPlanElements().size()){
                    newPlan.addLeg(populationFactory.createLeg("car"));
                }
            }
        }
        return newPlan;
    }

    private void collectPersonsToClone(Scenario scenario, MultiPolygon multiPolygon) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Iterator iterator = scenario.getPopulation().getPersons().values().iterator();

        while (iterator.hasNext()){
            numberOfTries++;
            Person person = (Person) iterator.next();
            for (int i = 0; i < 5; i=i+2) {
                if (i < person.getSelectedPlan().getPlanElements().size()) {
                    Activity activity = (Activity) person.getSelectedPlan().getPlanElements().get(i);
                    if (activityWithinPolygon(multiPolygon, geometryFactory, person, activity) &
                            !personsToClone.keySet().contains(person.getId())) {
                        personsToClone.put(person.getId(), i);
                    }
                }
            }
        }
    }


    private boolean activityWithinPolygon(MultiPolygon multiPolygon, GeometryFactory geometryFactory, Person person, Activity activity) {
        Point point = geometryFactory.createPoint(
                new Coordinate(activity.getCoord().getX(), activity.getCoord().getY()));
        try {
            point = (Point) JTS.transform(point, CRS.findMathTransform(CRS.decode("EPSG:32635"), CRS.decode("EPSG:4326")));
        } catch (TransformException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        Point inverse_point = geometryFactory.createPoint(new Coordinate(point.getY(), point.getX()));
        if (multiPolygon.contains(inverse_point)){
            return true;
        } else return false;
    }

    private static void modifyPlan(Plan plan, Coord targetCoord, Integer indexOfPlanElementToModify) {

        // Modifies the first and the last activities of an agent with the new random coordinates inside
        // a circle around given point

        if (indexOfPlanElementToModify == 0) {
            modifyFirstAndLastActivities(plan, targetCoord);
        } else modifySingleActivity(plan, targetCoord, indexOfPlanElementToModify);
    }

    private static void modifySingleActivity(Plan plan, Coord targetCoord, Integer indexOfPlanElementToModify) {
        Activity activityToModify = (Activity) plan.getPlanElements().get(indexOfPlanElementToModify);
        Coord coord = getModifiedCoord(targetCoord);
        activityToModify.setCoord(coord);
        plan.getPlanElements().remove(indexOfPlanElementToModify);
        plan.getPlanElements().add(indexOfPlanElementToModify, activityToModify);
    }

    private static void modifyFirstAndLastActivities(Plan plan, Coord targetCoord) {
        Activity firstActivity = (Activity) plan.getPlanElements().get(0);
        Activity lastActivity = (Activity) plan.getPlanElements().get(plan.getPlanElements().size() - 1);
        Coord coord = getModifiedCoord(targetCoord);
        firstActivity.setCoord(coord);
        lastActivity.setCoord(firstActivity.getCoord());
        plan.getPlanElements().remove(0);
        plan.getPlanElements().add(0, firstActivity);
        plan.getPlanElements().remove(plan.getPlanElements().size() - 1);
        plan.getPlanElements().add(lastActivity);
    }

    private static Coord getModifiedCoord(Coord targetCoord) {
        double phi = Math.random() * 2 * Math.PI;
        double radius = 20 * 60 * (5 / 3.6) * Math.sqrt(Math.random());
        double newX = targetCoord.getX() + radius * Math.cos(phi);
        double newY = targetCoord.getY() + radius * Math.sin(phi);
        return CoordUtils.createCoord(newX, newY);
    }


}
