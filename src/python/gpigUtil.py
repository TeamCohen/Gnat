dbpedia="<http://dbpedia.org/resource/"
def undbp(eid):
    """
    Convert a dbpdia entity id (uri) to a plaintext label.
    """
    return eid[len(dbpedia):-1].replace("_"," ")

