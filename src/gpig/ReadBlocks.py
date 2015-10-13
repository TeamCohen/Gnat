from guineapig import *

# @author lbing
# @author mingyanl
# @author krivard
class ReadBlocks(Reader):
    """ Returns blocks of lines in a file. """
    def __init__(self,src,isEndBlock=lambda line:line=="\n"):
        Reader.__init__(self,src)
        self.isEndBlock = isEndBlock
    def rowGenerator(self):
        buf = []
        for line in sys.stdin:
            if self.isEndBlock(line):
                yield buf
                buf = []
            else:
                buf.append(line)
        if buf:
            yield buf
    def __str__(self):
        return 'ReadBlocks("%s")' % self.src + self.showExtras()
