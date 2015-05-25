# MapReduce
MapReduce sample programs written to understand basic aspects of MapReduce programming paradigm

## 1) In-Mapper Combiner Pattern

**The following text is taken as is from the open source book "Data-Intensive Text Processing with MapReduce" that can be found  [here](http://lintool.github.io/MapReduceAlgorithms/ "Data-Intensive Text Processing
with MapReduce").**

In Hadoop, intermediate results are written to local disk before being
sent over the network. Since network and disk latencies are relatively expensive compared to other operations, 
reductions in the amount of intermediate data translate into increases in algorithmic efficiency. In MapReduce, local
aggregation of intermediate results is one of the keys to efficient algorithms. Through use of the in-mapper combiner pattern 
and by taking advantage of the ability to preserve
state across multiple inputs, it is often possible to substantially reduce both the
number and size of key-value pairs that need to be shuffled from the mappers
to the reducers.

There are two main advantages to using this
design pattern:
- First, it provides control over when local aggregation occurs and how it
exactly takes place. In contrast, the semantics of the combiner is underspecified
in MapReduce. For example, Hadoop makes no guarantees on how many times
the combiner is applied, or that it is even applied at all. The combiner is
provided as a semantics-preserving optimization to the execution framework,
which has the option of using it, perhaps multiple times, or not at all (or even
in the reduce phase). In some cases (although not in this particular example),
such indeterminism is unacceptable, which is exactly why programmers often
choose to perform their own local aggregation in the mappers.
- Second, in-mapper combining will typically be more efficient than using
actual combiners. One reason for this is the additional overhead associated
with actually materializing the key-value pairs. Combiners reduce the amount
of intermediate data that is shuffled across the network, but don’t actually
reduce the number of key-value pairs that are emitted by the mappers in the
first place. With the default Combiner, intermediate key-value pairs
are still generated on a per-document basis, only to be “compacted” by the
combiners. This process involves unnecessary object creation and destruction
(garbage collection takes time), and furthermore, object serialization and deserialization (when intermediate key-value pairs fill the in-memory buffer holding
map outputs and need to be temporarily spilled to disk). In contrast, with inmapper combining, the mappers will generate only those key-value pairs that
need to be shuffled across the network to the reducers.

Moreover, there are dissadvantages to this pattern. Further details can be found in the cited book.
