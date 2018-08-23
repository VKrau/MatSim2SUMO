import re
import csv
import sys

def csv2QGis(junctions, edges, speeds):
    with open("Output4QGis.csv", "wb") as w_obj:
        writer = csv.writer(w_obj)
        writer.writerow(['from','to','id','linestring','speed'])
        for k,v in edges.iteritems():
            writer.writerow([v[0],v[1],k,'LINESTRING(%s %s, %s %s)' % 
                            (junctions[v[0]][0], junctions[v[0]][1],
                            junctions[v[1]][0], junctions[v[1]][1]),
                             speeds[k]])

def SUMONet_parser(file):
    dictJunction = dict()
    dictEdge = dict()
    dictSpeed = dict()
    f = open(file, 'rb')
    for line in f:
        Junction = re.findall(r'<junction id="([^":]+)', line)
        Edge = re.findall(r'<edge id="([^":]+)', line) 
        Speed = re.findall(r'<lane id="([^":]+)_0', line)
        if Junction:
            print(Junction[0])
            dictJunction.setdefault(Junction[0],re.findall(r'x="([^":]+)" y="([^":]+)"', line)[0])
        if Edge:
            print(Edge[0])
            dictEdge.setdefault(Edge[0],re.findall(r'from="([^":]+)" to="([^":]+)"', line)[0])
        if Speed:
            dictSpeed.setdefault(Speed[0], re.findall(r'speed="([^":]+)"',line)[0])
    f.close()
    return dictJunction, dictEdge, dictSpeed

if __name__ == "__main__":
    if len(sys.argv) > 1:
        network_filename = sys.argv[1]
        Junctions, Edges, Speeds = SUMONet_parser(network_filename)
        csv2QGis(Junctions, Edges, Speeds)
    else:
        print("You need to pass the name of the net file with an argument!")