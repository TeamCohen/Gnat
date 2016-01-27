#!/usr/bin/python
import subprocess
import logging
import sys

def callProcess(o,args,**kw):
   """Call a process, tracing the actual call."""
   if kw: logging.info('subprocess call options: '+ str(kw))
   logging.info('calling: ' + ' '.join([(a,"\"%s\"" % a)[a.find(" ")>=0] for a in args]))
   if o['dryRun']: return
   stat = subprocess.call(args,**kw)
   if stat:
      logging.info(('call failed (status %d): ' % stat) + ' '.join(args))
      sys.exit(stat) #propagate failure

def getArg(i,defaultVal=None):
    """Get the i-th command line argument."""
    def safeDefault():
        if defaultVal: 
            return defaultVal
        else:
            logging.warn("expected at least %d command-line arguments - use 'help' for help" % (i+1))
            sys.exit(-1)
    try:
        result = sys.argv[i+1]
        return result if not result.startswith("--") else safeDefault()
    except IndexError:      
        return safeDefault()
