# -*- coding: utf-8 -*-
"""
Created on Thu Aug 16 12:12:12 2018

@author: VK
"""
from __future__ import absolute_import
from __future__ import print_function

import pandas as pd
import pickle
import random
import sys

def picklepacker(out_filename, df, agent_multiplier=0):
    data = {}
    for i in range(len(df)):
        key = int(df.iloc[i][1])
        route = [s.strip() for s in df.iloc[i][2][1:-1].split(',')]
        agent_id = df.iloc[i][0]
        generated_agent_time_list = [key]
        generated_agent_time_list.extend([(random.randint(int(key), int(key)+600)) for i in range(agent_multiplier-1)])
        for num, key_ in enumerate(generated_agent_time_list):
            if key_ not in data:
                data.setdefault(key_, {"%s_%s" % (agent_id, num): route})
            else:
                data[key_]["%s_%s" % (agent_id, num)] = route
    with open('%s.simdata' % out_filename, 'wb') as f:
        pickle.dump(data, f, protocol=2)

if __name__ == "__main__":
    increase_in_agents = 20
    if len(sys.argv) > 1:
        required_time_intervals = []
        for i in sys.argv[1:]:
            required_time_intervals.append(i[1:-1].split(','))
    else:
        required_time_intervals = [["morning",7,9],["evening",17,19]]
    print(required_time_intervals)
    df = pd.read_csv('data.routes.csv', header=None, sep=';')

    for i in required_time_intervals:
        df_result = df.loc[(df[1] >= int(i[1])*3600) & (df[1] <= int(i[2])*3600)]
        picklepacker(i[0], df_result, increase_in_agents)