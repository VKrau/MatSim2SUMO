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
        
def adding_agents(dictOfAgents):
    for id_, route in dictOfAgents.iteritems():
        traci.route.add(str(id_), route)
        traci.vehicle.add(str(id_), str(id_), typeID="pass")

def run_simulation(network_file, simdata):
    print("--------------LOADING DATA--------------")
    with open(simdata, "rb") as f:
        print("Loading %s" % simdata)
        data = pickle.load(f)
    step = min(data)
    options = get_options()
    max_time = max(data)
    if options.nogui:
        sumoBinary = checkBinary("sumo")
    else:
        sumoBinary = checkBinary("sumo-gui")
    sumoCmd = [sumoBinary, "--net-file", "%s" % network_file, 
               "--route-files", "routes.flo.xml",
               "--step-length",str(1.0*milliseconds_in_step/1000)]
               #"--full-output","full_xmllog.xml", "--netstate-dump","output_dump.xml",
               #"--device.rerouting.adaptation-steps", "1"]
    print("Ok")
    print("--------------СHECKING THE NETWORK'S COMPLIANCE WITH ROUTES--------------")
    checking_network(data, network_file)
    print("--------------SIMULATION START--------------")
    traci.start(sumoCmd)
    while step < max_time:
        if step in data:
            adding_agents(data[step])
        traci.simulationStep()
        step += 1
    traci.close;
    
run_simulation("mySUMOnetwork1.net.xml", "morning.simdata")

    