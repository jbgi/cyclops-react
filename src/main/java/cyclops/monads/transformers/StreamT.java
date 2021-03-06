package cyclops.monads.transformers;


import com.aol.cyclops2.types.FoldableTraversable;
import com.aol.cyclops2.types.To;
import com.aol.cyclops2.types.Traversable;
import com.aol.cyclops2.types.anyM.transformers.FoldableTransformerSeq;
import com.aol.cyclops2.types.stream.CyclopsCollectable;
import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.control.Maybe;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Monad Transformer for Java Streams and related types such as ReactiveSeq
 * 
 * StreamT allows the deeply wrapped Stream to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested  Streams
 */
public class StreamT<W extends WitnessType<W>,T> implements To<StreamT<W,T>>,
                                                          FoldableTransformerSeq<W,T> {

    final AnyM<W,Stream<T>> run;

    
    
    private StreamT(final AnyM<W,? extends Stream<T>> run) {
        this.run = AnyM.narrow(run);
    }
    
   

    public <R> AnyM<W, R> visit(Function<? super ReactiveSeq<T>, ? extends R> rsFn,
                                Function<? super Stream<T>, ? extends R> sFn) {

        return this.transformerStream()
                   .map(t -> {
                       if (t instanceof ReactiveSeq)
                           return rsFn.apply((ReactiveSeq<T>) t);
                        else
                           return sFn.apply((Stream<T>)t);

                   });
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyM<W,Stream<T>> unwrap() {
        return run;
    }
    public <R> R unwrapTo(Function<? super AnyM<W,Stream<T>>,? extends R> fn) {
        return unwrap().to(fn);
    }

    /**
     * Peek at the current value of the List
     * <pre>
     * {@code 
     *    ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of List
     * @return ListT with peek call
     */
    @Override
    public StreamT<W,T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }

    /**
     * Filter the wrapped List
     * <pre>
     * {@code 
     *    ListT.of(AnyM.fromStream(Arrays.asList(10,11))
     *             .filter(t->t!=10);
     *             
     *     //ListT<AnyM<Stream<List[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped List
     * @return ListT that applies the provided filter
     */
    @Override
    public StreamT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(seq -> seq.filter(test)));
    }

    /**
     * Map the wrapped List
     * 
     * <pre>
     * {@code 
     *  ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //ListT<AnyM<Stream<List[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped List
     * @return ListT that applies the map function to the wrapped List
     */
    @Override
    public <B> StreamT<W,B> map(final Function<? super T, ? extends B> f) {
        return of(run.map(o -> o.map(f)));
    }

    @Override
    public <B> StreamT<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f) {
        return new StreamT<W,B>(
                               run.map(o -> o.flatMap(f.andThen(ReactiveSeq::fromIterable))));

    }

    /**
     * Flat Map the wrapped List
      * <pre>
     * {@code 
     *  ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .flatMap(t->List.empty();
     *  
     *  
     *  //ListT<AnyM<Stream<List.empty>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return ListT that applies the flatMap function to the wrapped List
     */
    public <B> StreamT<W,B> flatMapT(final Function<? super T, StreamT<W,B>> f) {

        return of(run.map(list -> list.flatMap(a -> f.apply(a).run.stream())
                                      .flatMap(a -> a)));
    }

    

   

    /**
     * Construct an ListT from an AnyM that contains a monad type that contains type other than List
     * The values in the underlying monad will be mapped to List<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType<W>,A> StreamT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(ReactiveSeq::of));
    }
    
    /**
     * Construct an ListT from an AnyM that wraps a monad containing  Lists
     * 
     * @param monads AnyM that contains a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType<W>,A> StreamT<W,A> of(final AnyM<W,? extends Stream<A>> monads) {
        return new StreamT<>(
                              monads);
    }
    public static <W extends WitnessType<W>,A> StreamT<W,A> ofList(final AnyM<W,? extends List<A>> monads) {
        return new StreamT<>(
                              monads.map(ReactiveSeq::fromIterable));
    }
    public static <A> StreamT<Witness.stream,A> fromStream(final Stream<? extends Stream<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> StreamT<Witness.reactiveSeq,A> fromReactiveSeq(final ReactiveSeq<? extends Stream<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> StreamT<Witness.optional,A> fromOptional(final Optional<? extends Stream<A>> nested) {
        return of(AnyM.fromOptional(nested));
    }
    public static <A> StreamT<Witness.maybe,A> fromMaybe(final Maybe<? extends Stream<A>> nested) {
        return of(AnyM.fromMaybe(nested));
    }
    public static <A> StreamT<Witness.list,A> fromList(final List<? extends Stream<A>> nested) {
        return of(AnyM.fromList(nested));
    }
    public static <A> StreamT<Witness.set,A> fromSet(final Set<? extends Stream<A>> nested) {
        return of(AnyM.fromSet(nested));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("ListT[%s]",  run.unwrap().toString());

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Pure#unit(java.lang.Object)
     */
    public <T> StreamT<W,T> unit(final T unit) {
        return of(run.unit(ReactiveSeq.of(unit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMap(e -> e);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#collectable()
     
    @Override
    public Collectable<T> collectable() {
       return this;
    } */
    @Override
    public <R> StreamT<W,R> unitIterator(final Iterator<R> it) {  
        return of(run.unitIterator(it)
                     .map(i -> ReactiveSeq.of(i)));
    }

    @Override
    public <R> StreamT<W,R> empty() {
        return of(run.empty());
    }

    @Override
    public AnyM<W,? extends FoldableTraversable<T>> nestedFoldables() {
        return run.map(ReactiveSeq::fromStream);

    }

    @Override
    public AnyM<W,? extends CyclopsCollectable<T>> nestedCollectables() {
        return run.map(ReactiveSeq::fromStream);

    }

    @Override
    public <T> StreamT<W,T> unitAnyM(final AnyM<W,Traversable<T>> traversable) {

        return of((AnyM) traversable.map(t -> ReactiveSeq.fromIterable(t)));
    }

    @Override
    public AnyM<W,? extends FoldableTraversable<T>> transformerStream() {

        return run.map(ReactiveSeq::fromStream);
    }

    public static <W extends WitnessType<W>,T> StreamT<W,T> emptyList(W witness) { 
        return of(witness.<W>adapter().unit(ReactiveSeq.empty()));
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public StreamT<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.combine(predicate, op);
    }
    @Override
    public StreamT<W,T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (StreamT<W,T>)FoldableTransformerSeq.super.combine(op,predicate);
    }
    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycle(int)
     */
    @Override
    public StreamT<W,T> cycle(final long times) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycle(cyclops2.function.Monoid, int)
     */
    @Override
    public StreamT<W,T> cycle(final Monoid<T> m, final long times) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> cycleWhile(final Predicate<? super T> predicate) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> StreamT<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (StreamT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.ListT#zip(java.util.reactiveStream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> StreamT<W,R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (StreamT<W,R>) FoldableTransformerSeq.super.zipS(other, zipper);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#zipStream(java.util.reactiveStream.Stream)
     */
    @Override
    public <U> StreamT<W,Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (StreamT) FoldableTransformerSeq.super.zipS(other);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.ListT#zip(java.lang.Iterable)
     */
    @Override
    public <U> StreamT<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (StreamT) FoldableTransformerSeq.super.zip(other);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    public <S, U> StreamT<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (StreamT) FoldableTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    public <T2, T3, T4> StreamT<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                              final Iterable<? extends T4> fourth) {

        return (StreamT) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#zipWithIndex()
     */
    @Override
    public StreamT<W,Tuple2<T, Long>> zipWithIndex() {

        return (StreamT<W,Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sliding(int)
     */
    @Override
    public StreamT<W,PVectorX<T>> sliding(final int windowSize) {

        return (StreamT<W,PVectorX<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sliding(int, int)
     */
    @Override
    public StreamT<W,PVectorX<T>> sliding(final int windowSize, final int increment) {

        return (StreamT<W,PVectorX<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamT<W,C> grouped(final int size, final Supplier<C> supplier) {

        return (StreamT<W,C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (StreamT<W,ListX<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public StreamT<W,ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (StreamT<W,ListX<T>>) FoldableTransformerSeq.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (StreamT<W,ListX<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamT<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (StreamT<W,C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamT<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (StreamT<W,C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#grouped(int)
     */
    @Override
    public StreamT<W,ListX<T>> grouped(final int groupSize) {

        return (StreamT<W,ListX<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#grouped(java.util.function.Function, java.util.reactiveStream.Collector)
     */
    @Override
    public <K, A, D> StreamT<W,Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {

        return (StreamT) FoldableTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#grouped(java.util.function.Function)
     */
    @Override
    public <K> StreamT<W,Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return (StreamT) FoldableTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#distinct()
     */
    @Override
    public StreamT<W,T> distinct() {

        return (StreamT<W,T>) FoldableTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    public StreamT<W,T> scanLeft(final Monoid<T> monoid) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> StreamT<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (StreamT<W,U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanRight(cyclops2.function.Monoid)
     */
    @Override
    public StreamT<W,T> scanRight(final Monoid<T> monoid) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> StreamT<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (StreamT<W,U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sorted()
     */
    @Override
    public StreamT<W,T> sorted() {

        return (StreamT<W,T>) FoldableTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sorted(java.util.Comparator)
     */
    @Override
    public StreamT<W,T> sorted(final Comparator<? super T> c) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> takeWhile(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> dropWhile(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> takeUntil(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> dropUntil(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#dropRight(int)
     */
    @Override
    public StreamT<W,T> dropRight(final int num) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#takeRight(int)
     */
    @Override
    public StreamT<W,T> takeRight(final int num) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#skip(long)
     */
    @Override
    public StreamT<W,T> skip(final long num) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#skipWhile(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> skipWhile(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#skipUntil(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> skipUntil(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#limit(long)
     */
    @Override
    public StreamT<W,T> limit(final long num) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#limitWhile(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> limitWhile(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#limitUntil(java.util.function.Predicate)
     */
    @Override
    public StreamT<W,T> limitUntil(final Predicate<? super T> p) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#intersperse(java.lang.Object)
     */
    @Override
    public StreamT<W,T> intersperse(final T value) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#reverse()
     */
    @Override
    public StreamT<W,T> reverse() {

        return (StreamT<W,T>) FoldableTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#shuffle()
     */
    @Override
    public StreamT<W,T> shuffle() {

        return (StreamT<W,T>) FoldableTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#skipLast(int)
     */
    @Override
    public StreamT<W,T> skipLast(final int num) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#limitLast(int)
     */
    @Override
    public StreamT<W,T> limitLast(final int num) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#onEmpty(java.lang.Object)
     */
    @Override
    public StreamT<W,T> onEmpty(final T value) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public StreamT<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> StreamT<W,T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#shuffle(java.util.Random)
     */
    @Override
    public StreamT<W,T> shuffle(final Random random) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#slice(long, long)
     */
    @Override
    public StreamT<W,T> slice(final long from, final long to) {

        return (StreamT<W,T>) FoldableTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> StreamT<W,T> sorted(final Function<? super T, ? extends U> function) {
        return (StreamT) FoldableTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof StreamT) {
            return run.equals(((StreamT) o).run);
        }
        return false;
    }



    public <T2, R1, R2, R3, R> StreamT<W,R> forEach4M(Function<? super T, ? extends StreamT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends StreamT<W,R2>> value2,
                                                      Fn3<? super T, ? super R1, ? super R2, ? extends StreamT<W,R3>> value3,
                                                      Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> StreamT<W,R> forEach4M(Function<? super T, ? extends StreamT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends StreamT<W,R2>> value2,
                                                      Fn3<? super T, ? super R1, ? super R2, ? extends StreamT<W,R3>> value3,
                                                      Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                      Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> StreamT<W,R> forEach3M(Function<? super T, ? extends StreamT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends StreamT<W,R2>> value2,
                                                  Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> StreamT<W,R> forEach3M(Function<? super T, ? extends StreamT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends StreamT<W,R2>> value2,
                                                  Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                  Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> StreamT<W,R> forEach2M(Function<? super T, ? extends StreamT<W,R1>> value1,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> StreamT<W,R> forEach2M(Function<? super T, ? extends StreamT<W,R1>> value1,
                                          BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .filter(in2->filterFunction.apply(in,in2))
                .map(in2->yieldingFunction.apply(in,in2)));
    }
}