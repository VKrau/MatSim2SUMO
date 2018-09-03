# -*- coding: utf-8 -*-
"""
Created on Thu Aug 23 13:43:55 2018

@author: VK
"""
from __future__ import absolute_import
from __future__ import print_function

import csv
import random
import pickle
import argparse

#This generator creates a set of start times without repeating
def start_time_generator (n, time, biasA=0, biasB=1000):
    setStartTime = set()
    while len(setStartTime) < n:
        setStartTime.add(random.randint(time+biasA, time+biasB)) 
    return list(setStartTime)

def routes_reader(file_obj, StartTime, EndTime, agent_multiplier=0, biasA=0, biasB=1000):
    data = {}
    reader = csv.reader(file_obj, delimiter=';')
    b=0
    for line in reader:
        agent_id = line[0]
        time = int(line[1])
        route = [s.strip() for s in line[2][1:-1].split(',')]
        if time>=StartTime and time<=EndTime:
            generated_agent_time_list = [time]
            #generated_agent_time_list.extend([(random.randint(int(time), int(time)+1200)) for i in range(agent_multiplier-1)])
            generated_agent_time_list.extend(start_time_generator(agent_multiplier-1, time, biasA, biasB))
            for num, key in enumerate(generated_agent_time_list):
                while True:
                    if key not in data:
                        data.setdefault(key, {"%s_%s" % (agent_id, num): route})
                        break
                    else:
                        if data[key].get("%s_%s" % (agent_id, num)):
                            key = start_time_generator(1, key, biasA, biasB)[0]
                        else:
                            data[key]["%s_%s" % (agent_id, num)] = route
                            break
                   
            b += len(generated_agent_time_list)
    print("Total number of generated agents: %s" % b)
    k = 0
    for i in data.itervalues():
        k +=len(i)
    print("Total number of recorded agents: %s" % k)
    return data
    
def routes2simdata(out_filename, data):
    with open('%s.simdata' % out_filename, 'wb') as f:
        pickle.dump(data, f, protocol=2)
    print("Well Done! File: %s" % (out_filename+".simdata")) 

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--periods", required=True, 
                        help="Setting the necessary periods (format: ['name1',time1,time2];['name2',time1,time2])\
                        for their packaging in the simdata format.\
                        For example: ['morning',7,9];['evening',17,19]", dest="periods")
    parser.add_argument("--multiplier", required=False, default=1, type=int,
                        help="Agent multiplier", dest="multiplier")
    parser.add_argument("--biasA", required=False, default=0, type=int, 
                        help="Offset of the lower limit of the start time (sec.)", dest="biasA")
    parser.add_argument("--biasB", required=False, default=1000, type=int, 
                        help="Offset of the upper limit of the start time (sec.)", dest="biasB")
    parse = parser.parse_args().periods.split(";")
    print("Ok, let's do it!")
    print("You set %s period(s):" % len(parse))
    required_time_intervals = []
    for j,i in enumerate(parse):
        print(j,": ", i, sep="")
        required_time_intervals.append(i[1:-1].split(','))
    increase_in_agents = parser.parse_args().multiplier
    biasA = parser.parse_args().biasA
    biasB = parser.parse_args().biasB
    print("Multiplier: %s" % increase_in_agents)
    print("Offset of the lower limit of the start time: %s seconds" % biasA)
    print("Offset of the upper limit of the start time: %s seconds" % biasB)
    for i in required_time_intervals:
        with open("data.routes.csv") as f_obj:
            dict_data = routes_reader(f_obj, int(i[1])*3600, int(i[2])*3600, increase_in_agents, biasA, biasB)
        routes2simdata(i[0], dict_data)