import re
import csv
import sys

def csv_writer(data):
    with open("..\Links.csv", "wb") as w_obj:
        writer = csv.writer(w_obj)
        for i in data:
            writer.writerow([i])

def all_links_of_network(file):
    SetSum = set()
    f = open(file, 'rb')
    for line in f:
        Sum = re.findall(r'\<edge id="([^":]+)', line)
        if Sum:
            SetSum.add(Sum[0])
    f.close()
    return SetSum

if __name__ == "__main__":
    if len(sys.argv) > 1:
        Links = all_links_of_network(sys.argv[1])
        csv_writer(Links)
    else:
        print("You need to pass the name of the file with an argument!")