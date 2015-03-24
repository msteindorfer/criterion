module Dominators

import IO;
import ValueIO;
import Relation;
import util::Math;
import util::Benchmark;
import analysis::graphs::Graph;
extend lang::php::analysis::cfg::CFG;

loc dir = |compressed+project://cfg-benchmark/data/|;

//  Cooper, Keith D.; Harvey, Timothy J; and Kennedy, Ken (2001). "A Simple, Fast Dominance Algorithm"

void sample() {
  gr = readBinaryValueFile(#map[NamePath,Graph[CFGNode]], dir + "wordpress-cfgs-as-graphs.bin.gz");
  sampled = (n:gr[n] | n <- gr, arbInt(10) == 0);
  writeBinaryValueFile(dir + "wordpress-cfgs-as-graphs-sampled.bin.gz", sampled); 
}

@Memo
map[list[NamePart], Graph[CFGNode]] getSample()
  = readBinaryValueFile(#map[NamePath,Graph[CFGNode]]
      , dir + "wordpress-cfgs-as-graphs-sampled.bin.gz");
  
  
set[void]       intersect({{},*set[&T] _})           = {};
set[void]       intersect({})                        = {};
default set[&T] intersect({set[&T] h,*set[&T] tail}) = (h|it & e| e <- tail);

map[CFGNode, set[CFGNode]] dominators(Graph[CFGNode] gr) {
  set[CFGNode] nodes = carrier(gr);
  map[CFGNode, set[CFGNode]] preds = toMap(gr<1,0>);
   
  if ({n0,*_} := top(gr), n0 is methodEntry || n0 is functionEntry || n0 is scriptEntry) {
    map[CFGNode, set[CFGNode]] dom 
      = (n0:{n0}) + (n:nodes | n <- nodes - {n0});
  
    int i = 0;
    
    solve (dom) {
      for (n <- nodes) 
        dom[n] = {n} + intersect({dom[p] | p <- preds[n]?{}});
    }
    
    return dom;
  }
  else { 
    println("did not ok; more than one entry node in <top(gr)>???");
    return ();
  }
}  

@javaClass{dom.Dominators}
java map[CFGNode, set[CFGNode]] jDominators(Graph[CFGNode] gr);

bool compareResults() {
  \java = readBinaryValueFile(#set[value], dir[scheme="project"] + "dominators-java.bin");
  \rascal = readBinaryValueFile(#set[value], dir[scheme="project"] + "dominators-rascal.bin");
  return \java == \rascal;
}

Graph[CFGNode] getSingleSample()
  = readBinaryValueFile(#Graph[CFGNode], dir + "single.bin.gz");


void main() {
  s = getSample();
  before = cpuTime();
  r = {dominators(s[p]) | p <- s}; 
  println("Duration <(cpuTime() - before) / 1000000000> seconds");
  writeBinaryValueFile(dir[scheme="project"] + "dominators-rascal.bin", r);
}