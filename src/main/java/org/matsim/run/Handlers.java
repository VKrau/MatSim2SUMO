package org.matsim.run;

import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;


public class Handlers implements LinkEnterEventHandler {

    private AgentsStat agentsStat;

    //Class constructor
    Handlers (AgentsStat agentsStat){
        this.agentsStat = agentsStat;
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (agentsStat.getSetOfMonitoringLinks().contains(String.valueOf(event.getLinkId()))) {
            if (!agentsStat.getCurrentTrips().containsKey(String.valueOf(event.getVehicleId()))) {
                agentsStat.recordToReportTable(event.getVehicleId(), event.getTime(), event.getLinkId());
                agentsStat.addTrip(event.getVehicleId(), (int) Math.round(event.getTime()));
            } else {
                agentsStat.recordToReportTable(event.getVehicleId(), event.getLinkId());
            }
        } else {
            agentsStat.removeFromTrip(event.getVehicleId());
        }
    }
}
