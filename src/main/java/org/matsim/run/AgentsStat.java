package org.matsim.run;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.vehicles.Vehicle;

import java.util.*;

public class AgentsStat {
    private Scenario scenario;
    private Table<String, Integer, ArrayList<String>> ReportTable = HashBasedTable.create();
    private Map<String, Integer> currentTrips = new HashMap<>();
    public HashSet<String> getSetOfMonitoringLinks() {
        return setOfMonitoringLinks;
    }

    //private HashMap<String, HashSet<Id>> BookOfEvents = new HashMap<String, HashSet<Id>>();
    private HashSet<String> setOfMonitoringLinks;

    //Class constructor
    AgentsStat(Scenario scenario, HashSet<String> setOfMonitoringLinks) {
        this.scenario = scenario;
        this.setOfMonitoringLinks = setOfMonitoringLinks;

        //Full remove agent from BookOfEvents
        /**private void removeAgentFromBook(Id id){
         for (String key:BookOfEvents.keySet()) {
         BookOfEvents.get(key).remove(id);
         }
         }*/
    }

    public Table<String, Integer, ArrayList<String>> getReportTable() {
        return ReportTable;
    }

    public Map<String, Integer> getCurrentTrips() {
        return currentTrips;
    }

    public void addTrip(Id id, Integer startTime) {
        currentTrips.put(String.valueOf(id), startTime);
    }

    public void removeFromTrip(Id id) {
        if (currentTrips.containsKey(String.valueOf(id))) {
            currentTrips.remove(String.valueOf(id));
        }
    }

    public void clearBookAndReportTable(){
        ReportTable.clear();
        currentTrips.clear();
        System.out.println("---------CLEANUP---------");
        System.out.println(ReportTable);
        System.out.println(currentTrips);
    }

    public void recordToReportTable(Id<Vehicle> VehicleID, Double Time, Id<Link> currentLink) {
        String strVehicleID = String.valueOf(VehicleID);
        ArrayList<String> strArray = new ArrayList<>();
        strArray.add(String.valueOf(currentLink));


        ReportTable.put(strVehicleID, (int) Math.round(Time), strArray);
    }

    public void recordToReportTable(Id<Vehicle> VehicleID, Id<Link> currentLink) {
        String strVehicleID = String.valueOf(VehicleID);
        ReportTable.get(strVehicleID, getCurrentTrips().get(strVehicleID)).add(String.valueOf(currentLink));
    }
}
