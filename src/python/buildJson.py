#!/usr/bin/python
import sys
import json

def doMain(fieldlist,constantValues):
    template = {}
    for x in constantValues:
        (k,d,v) = x.partition("=")
        template[k] = v
    genId='id' in template
    fields = fieldlist.split(",")
    N = len(fields)
    i=0
    print "["
    delim=False
    for line in sys.stdin:
        i+=1
        vlist = line.strip().split("\t")
        for vi in range(len(vlist)):
            if vlist[vi].startswith("@python"):
                vlist[vi] = eval(vlist[vi][7:])
            if len(vlist[vi]) == 0:
                vlist[vi] = None
        if len(vlist) != N:
            assert "Expected %d fields on line %d (saw %d)" % (N,i,len(vlist))
        x = dict(template)
        x.update(zip(fields,vlist))
        for k in x.keys():
            if x[k] == None: del x[k]
        if genId: x['id'] = "%s%d" % (template['id'],i)
        if delim: print ","
        else: delim=True
        print json.dumps(x),
    print "\n]"

if __name__=="__main__":
    if len(sys.argv) < 3:
        print "Usage: python %s field1,field2,field3 fieldX=value fieldY=value ..."
        exit(1)
    doMain(sys.argv[1],sys.argv[2:])
