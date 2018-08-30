package org.matsim.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.vehicles.Vehicle;


public class Handlers implements LinkEnterEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler{

    private AgentsStat agentsStat;

    //Class constructor
    Handlers (AgentsStat agentsStat){
        this.agentsStat = agentsStat;
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        agentsStat.addLinkToRoute(event.getLinkId(), event.getVehicleId(), event.getTime());
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        agentsStat.removeFromTrip(event.getPersonId());
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        // Unfortunately necessary since vehicle departures are not uniformly registered
        Id<Vehicle> vehId = Id.create(event.getPersonId(), Vehicle.class);
        agentsStat.addLinkToRoute(event.getLinkId(), vehId, event.getTime());
    }
}
