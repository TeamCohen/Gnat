def undbp(eid):
    """
    Convert a dbpdia entity id (uri) to a plaintext label.
    """
    return eid[eid.rindex("/")+1:-1].replace("_"," ")

