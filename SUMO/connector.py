# -*- coding: utf-8 -*-
"""
Created on Thu Aug 16 10:45:32 2018

@author: VK
"""

from __future__ import absolute_import
from __future__ import print_function

import os, sys
import optparse
import pickle
import LinksParser

try:
    sys.path.append(os.path.join(os.path.dirname(
        __file__), '..', '..', '..', '..', "tools"))
    sys.path.append(os.path.join(os.environ.get("SUMO_HOME", os.path.join(
        os.path.dirname(__file__), "..", "..", "..")), "tools"))
    from sumolib import checkBinary  # noqa
except ImportError:
    sys.exit(
        "please declare environment variable 'SUMO_HOME' as the root directory of your sumo installation (it should contain folders 'bin', 'tools' and 'docs')")

import traci

def get_options():
    optParser = optparse.OptionParser()
    optParser.add_option("--nogui", action="store_true",
                         default=False, help="run the commandline version of sumo")
    options, args = optParser.parse_args()
    return options

milliseconds_in_step = 1000
simulation_in_seconds_duration = 3600

def checking_network(data, net_file):
    set_of_routes_links = set()
    set_of_network_links = set()
    for v1 in data.itervalues():
        for v2 in v1.itervalues():
            for i in v2:
                set_of_routes_links.add(i)
    set_of_network_links = LinksParser.all_links_of_network(net_file)
    errors = set_of_routes_links - set_of_network_links
    if errors:
        sys.exit("Error! There are no necessary links in the network: \n%s" % list(errors))
    else:
        print("Ok!")
        
def adding_agents(dictOfAgents, step):
    for id_, route in dictOfAgents.iteritems():
        traci.route.add(str(id_+str(step)), route)
        try:
            traci.vehicle.add(str(id_), str(id_+str(step)), typeID="pass")
        except:
            print("Agent %s has not yet been destroyed" % (id_))    

def run_simulation(network_file, simdata):
    print("--------------LOADING DATA--------------")
    with open(simdata, "rb") as f:
        print("Loading %s" % simdata)
        data = pickle.load(f)
    print("%s items uploaded" % len(data))
    step = min(data)
    options = get_options()
    max_time = max(data)
    print("Will be executed %s steps" % (max_time-step))
    if options.nogui:
        sumoBinary = checkBinary("sumo")
    else:
        sumoBinary = checkBinary("sumo-gui")
    sumoCmd = [sumoBinary, "--net-file", "%s" % network_file, 
               "--route-files", "routes.flo.xml",
               "--step-length",str(1.0*milliseconds_in_step/1000),
               "--additional-files", "additional.xml"]
               #"--full-output","full_xmllog.xml", "--netstate-dump","output_dump.xml"]
               #"--device.rerouting.adaptation-steps", "1"]
    print("Ok")
    print("--------------CHECKING THE NETWORK'S COMPLIANCE WITH ROUTES--------------")
    checking_network(data, network_file)
    print("--------------SIMULATION START--------------")
    try:
        traci.start(sumoCmd)
    except:
        sys.exit("Error! Something is wrong with SUMO")
    while step < max_time:
        if step in data:
            adding_agents(data[step], step)
        traci.simulationStep()
        step += 1
    traci.close();
    
if __name__ == "__main__":
    if len(sys.argv) > 2:
        run_simulation(sys.argv[1], sys.argv[2])
    else:
        try:
            run_simulation("mySUMOnetwork.net.xml", "morning.simdata")
        except:
            print("You need to pass the name of the file with an argument!")    