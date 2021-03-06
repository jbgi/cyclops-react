package cyclops.collections.immutable;


import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPOrderedSetX;
import com.aol.cyclops2.data.collections.extensions.persistent.PersistentCollectionX;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import cyclops.collections.ListX;
import com.aol.cyclops2.types.OnEmptySwitch;
import com.aol.cyclops2.types.To;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface POrderedSetX<T> extends To<POrderedSetX<T>>,POrderedSet<T>, PersistentCollectionX<T>, OnEmptySwitch<T, POrderedSet<T>> {
    /**
     * Narrow a covariant POrderedSetX
     * 
     * <pre>
     * {@code 
     *  POrderedSetX<? extends Fruit> set = POrderedSetX.of(apple,bannana);
     *  POrderedSetX<Fruit> fruitSet = POrderedSetX.narrowK(set);
     * }
     * </pre>
     * 
     * @param setX to narrowK generic type
     * @return POrderedSetX with narrowed type
     */
    public static <T> POrderedSetX<T> narrow(final POrderedSetX<? extends T> setX) {
        return (POrderedSetX<T>) setX;
    }
    /**
     * Create a POrderedSetX that contains the Integers between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range POrderedSetX
     */
    public static POrderedSetX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .toPOrderedSetX();
    }

    /**
     * Create a POrderedSetX that contains the Longs between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range POrderedSetX
     */
    public static POrderedSetX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .toPOrderedSetX();
    }

    /**
     * Unfold a function into a POrderedSetX
     * 
     * <pre>
     * {@code 
     *  POrderedSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return POrderedSetX generated by unfolder function
     */
    static <U, T> POrderedSetX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .toPOrderedSetX();
    }

    /**
     * Generate a POrderedSetX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate POrderedSetX elements
     * @return POrderedSetX generated from the provided Supplier
     */
    public static <T> POrderedSetX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .toPOrderedSetX();
    }

    /**
     * Create a POrderedSetX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return POrderedSetX generated by iterative application
     */
    public static <T> POrderedSetX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .toPOrderedSetX();

    }

    public static <T> POrderedSetX<T> of(final T... values) {
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.from(Arrays.asList(values)));
    }

    public static <T> POrderedSetX<T> empty() {
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.empty());
    }

    public static <T> POrderedSetX<T> singleton(final T value) {
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.singleton(value));
    }

    /**
     * Reduce a Stream to a POrderedSetX, 
     * 
     * 
     * <pre>
     * {@code 
     *    POrderedSetX<Integer> set = POrderedSetX.fromStream(Stream.of(1,2,3));
     * 
     *  //set = [1,2,3]
     * }</pre>
     * 
     * 
     * @param stream to convert 
     * @return
     */
    public static <T> POrderedSetX<T> fromStream(final Stream<T> stream) {
        return Reducers.<T> toPOrderedSetX()
                       .mapReduce(stream);
    }

    public static <T> POrderedSetX<T> fromCollection(final Collection<T> stream) {
        if (stream instanceof POrderedSetX)
            return (POrderedSetX) stream;
        if (stream instanceof POrderedSet)
            return new LazyPOrderedSetX<>(
                                          (POrderedSet) stream);
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.from(stream));
    }

    /**
     * Construct a POrderedSetX from an Publisher
     * 
     * @param publisher
     *            to construct POrderedSetX from
     * @return POrderedSetX
     */
    public static <T> POrderedSetX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher)
                          .toPOrderedSetX();
    }

    public static <T> POrderedSetX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof POrderedSetX)
            return (POrderedSetX) iterable;
        if (iterable instanceof POrderedSet)
            return new LazyPOrderedSetX<>(
                                          (POrderedSet) iterable);


        return new LazyPOrderedSetX<>(null,
                ReactiveSeq.fromIterable(iterable),
                Reducers.toPOrderedSet());
    }

    public static <T> POrderedSetX<T> toPOrderedSet(final Stream<T> stream) {
        return Reducers.<T> toPOrderedSetX()
                       .mapReduce(stream);
    }


    @Override
    default POrderedSetX<T> materialize() {
        return (POrderedSetX<T>)PersistentCollectionX.super.materialize();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> POrderedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (POrderedSetX)PersistentCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> POrderedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (POrderedSetX)PersistentCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> POrderedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (POrderedSetX)PersistentCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> POrderedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (POrderedSetX)PersistentCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> POrderedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (POrderedSetX)PersistentCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> POrderedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (POrderedSetX)PersistentCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     POrderedSetX.of(1,2,3)
     *                 .map(i->i*2)
     *                 .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *     //POrderedSetX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed POrderedSetX
     */
    default <R> POrderedSetX<R> coflatMap(Function<? super POrderedSetX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);

    }

    @Override
    default POrderedSetX<T> take(final long num) {

        return limit(num);
    }
    @Override
    default POrderedSetX<T> drop(final long num) {

        return skip(num);
    }
    @Override
    default POrderedSetX<T> toPOrderedSetX() {
        return this;
    }

    @Override
    default <R> POrderedSetX<R> unit(final Collection<R> col) {
        return fromCollection(col);
    }

    @Override
    default <R> POrderedSetX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> POrderedSetX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    @Override
    default <R> POrderedSetX<R> emptyUnit() {
        return empty();
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    default POrderedSet<T> toPOrderedSet() {
        return this;
    }

    /**
    * Combine two adjacent elements in a POrderedSetX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code 
    *  POrderedSetX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),Semigroups.intSum)
                 .toListX()
                 
    *  //ListX(3,4) 
    * }</pre>
    * 
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced POrderedSetX
    */
    @Override
    default POrderedSetX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (POrderedSetX<T>) PersistentCollectionX.super.combine(predicate, op);
    }
    @Override
    default POrderedSetX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (POrderedSetX<T>)PersistentCollectionX.super.combine(op,predicate);
    }


    @Override
    default <X> POrderedSetX<X> from(final Collection<X> col) {
        return fromCollection(col);
    }

    @Override
    default <T> Reducer<POrderedSet<T>> monoid() {
        return Reducers.toPOrderedSet();
    }

    /* (non-Javadoc)
     * @see org.pcollections.PSet#plus(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> plus(T e);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#plusAll(java.util.Collection)
     */
    @Override
    public POrderedSetX<T> plusAll(Collection<? extends T> list);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#minus(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> minus(Object e);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#minusAll(java.util.Collection)
     */
    @Override
    public POrderedSetX<T> minusAll(Collection<?> list);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#reverse()
     */
    @Override
    default POrderedSetX<T> reverse() {
        return (POrderedSetX<T>) PersistentCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> filter(final Predicate<? super T> pred) {
        return (POrderedSetX<T>) PersistentCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> POrderedSetX<R> map(final Function<? super T, ? extends R> mapper) {
        return (POrderedSetX<R>) PersistentCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> POrderedSetX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (POrderedSetX<R>) PersistentCollectionX.super.flatMap(mapper);
    }

    @Override
    default POrderedSetX<T> takeRight(final int num) {
        return (POrderedSetX<T>) PersistentCollectionX.super.takeRight(num);
    }

    @Override
    default POrderedSetX<T> dropRight(final int num) {
        return (POrderedSetX<T>) PersistentCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limit(long)
     */
    @Override
    default POrderedSetX<T> limit(final long num) {
        return (POrderedSetX<T>) PersistentCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skip(long)
     */
    @Override
    default POrderedSetX<T> skip(final long num) {
        return (POrderedSetX<T>) PersistentCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> takeWhile(final Predicate<? super T> p) {
        return (POrderedSetX<T>) PersistentCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> dropWhile(final Predicate<? super T> p) {
        return (POrderedSetX<T>) PersistentCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> takeUntil(final Predicate<? super T> p) {
        return (POrderedSetX<T>) PersistentCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> dropUntil(final Predicate<? super T> p) {
        return (POrderedSetX<T>) PersistentCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> POrderedSetX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (POrderedSetX<R>) PersistentCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
     */
    @Override
    default POrderedSetX<T> slice(final long from, final long to) {
        return (POrderedSetX<T>) PersistentCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> POrderedSetX<T> sorted(final Function<? super T, ? extends U> function) {
        return (POrderedSetX<T>) PersistentCollectionX.super.sorted(function);
    }

    @Override
    default POrderedSetX<ListX<T>> grouped(final int groupSize) {
        return (POrderedSetX<ListX<T>>) PersistentCollectionX.super.grouped(groupSize);
    }

    @Override
    default <K, A, D> POrderedSetX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return (POrderedSetX) PersistentCollectionX.super.grouped(classifier, downstream);
    }

    @Override
    default <K> POrderedSetX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (POrderedSetX) PersistentCollectionX.super.grouped(classifier);
    }

    @Override
    default <U> POrderedSetX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (POrderedSetX) PersistentCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> POrderedSetX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (POrderedSetX<R>) PersistentCollectionX.super.zip(other, zipper);
    }


    @Override
    default <U, R> POrderedSetX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (POrderedSetX<R>) PersistentCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#permutations()
     */
    @Override
    default POrderedSetX<ReactiveSeq<T>> permutations() {

        return (POrderedSetX<ReactiveSeq<T>>) PersistentCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#combinations(int)
     */
    @Override
    default POrderedSetX<ReactiveSeq<T>> combinations(final int size) {

        return (POrderedSetX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#combinations()
     */
    @Override
    default POrderedSetX<ReactiveSeq<T>> combinations() {

        return (POrderedSetX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations();
    }

    @Override
    default POrderedSetX<PVectorX<T>> sliding(final int windowSize) {
        return (POrderedSetX<PVectorX<T>>) PersistentCollectionX.super.sliding(windowSize);
    }

    @Override
    default POrderedSetX<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return (POrderedSetX<PVectorX<T>>) PersistentCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default POrderedSetX<T> scanLeft(final Monoid<T> monoid) {
        return (POrderedSetX<T>) PersistentCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> POrderedSetX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (POrderedSetX<U>) PersistentCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default POrderedSetX<T> scanRight(final Monoid<T> monoid) {
        return (POrderedSetX<T>) PersistentCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> POrderedSetX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (POrderedSetX<U>) PersistentCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default POrderedSetX<T> plusInOrder(final T e) {

        return (POrderedSetX<T>) PersistentCollectionX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
    * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycle(int)
    */
    @Override
    default PStackX<T> cycle(final long times) {

        return this.stream()
                   .cycle(times)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default PStackX<T> cycle(final Monoid<T> m, final long times) {

        return this.stream()
                   .cycle(m, times)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default PStackX<T> cycleWhile(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleWhile(predicate)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default PStackX<T> cycleUntil(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleUntil(predicate)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> POrderedSetX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (POrderedSetX) PersistentCollectionX.super.zipS(other);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> POrderedSetX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (POrderedSetX) PersistentCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <T2, T3, T4> POrderedSetX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (POrderedSetX) PersistentCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
     */
    @Override
    default POrderedSetX<Tuple2<T, Long>> zipWithIndex() {

        return (POrderedSetX<Tuple2<T, Long>>) PersistentCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#distinct()
     */
    @Override
    default POrderedSetX<T> distinct() {

        return (POrderedSetX<T>) PersistentCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#sorted()
     */
    @Override
    default POrderedSetX<T> sorted() {

        return (POrderedSetX<T>) PersistentCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default POrderedSetX<T> sorted(final Comparator<? super T> c) {

        return (POrderedSetX<T>) PersistentCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> skipWhile(final Predicate<? super T> p) {

        return (POrderedSetX<T>) PersistentCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> skipUntil(final Predicate<? super T> p) {

        return (POrderedSetX<T>) PersistentCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> limitWhile(final Predicate<? super T> p) {

        return (POrderedSetX<T>) PersistentCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> limitUntil(final Predicate<? super T> p) {

        return (POrderedSetX<T>) PersistentCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default POrderedSetX<T> intersperse(final T value) {

        return (POrderedSetX<T>) PersistentCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#shuffle()
     */
    @Override
    default POrderedSetX<T> shuffle() {

        return (POrderedSetX<T>) PersistentCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
     */
    @Override
    default POrderedSetX<T> skipLast(final int num) {

        return (POrderedSetX<T>) PersistentCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
     */
    @Override
    default POrderedSetX<T> limitLast(final int num) {

        return (POrderedSetX<T>) PersistentCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default POrderedSetX<T> onEmptySwitch(final Supplier<? extends POrderedSet<T>> supplier) {
        if (this.isEmpty())
            return POrderedSetX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default POrderedSetX<T> onEmpty(final T value) {

        return (POrderedSetX<T>) PersistentCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default POrderedSetX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (POrderedSetX<T>) PersistentCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> POrderedSetX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (POrderedSetX<T>) PersistentCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
     */
    @Override
    default POrderedSetX<T> shuffle(final Random random) {

        return (POrderedSetX<T>) PersistentCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> POrderedSetX<U> ofType(final Class<? extends U> type) {

        return (POrderedSetX<U>) PersistentCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default POrderedSetX<T> filterNot(final Predicate<? super T> fn) {

        return (POrderedSetX<T>) PersistentCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#notNull()
     */
    @Override
    default POrderedSetX<T> notNull() {

        return (POrderedSetX<T>) PersistentCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.reactiveStream.Stream)
     */
    @Override
    default POrderedSetX<T> removeAllS(final Stream<? extends T> stream) {

        return (POrderedSetX<T>) PersistentCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default POrderedSetX<T> removeAllI(final Iterable<? extends T> it) {

        return (POrderedSetX<T>) PersistentCollectionX.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default POrderedSetX<T> removeAll(final T... values) {

        return (POrderedSetX<T>) PersistentCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default POrderedSetX<T> retainAllI(final Iterable<? extends T> it) {

        return (POrderedSetX<T>) PersistentCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#retainAllI(java.util.reactiveStream.Stream)
     */
    @Override
    default POrderedSetX<T> retainAllS(final Stream<? extends T> seq) {

        return (POrderedSetX<T>) PersistentCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default POrderedSetX<T> retainAll(final T... values) {

        return (POrderedSetX<T>) PersistentCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> POrderedSetX<U> cast(final Class<? extends U> type) {

        return (POrderedSetX<U>) PersistentCollectionX.super.cast(type);
    }

    @Override
    default <C extends Collection<? super T>> POrderedSetX<C> grouped(final int size, final Supplier<C> supplier) {

        return (POrderedSetX<C>) PersistentCollectionX.super.grouped(size, supplier);
    }

    @Override
    default POrderedSetX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (POrderedSetX<ListX<T>>) PersistentCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default POrderedSetX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (POrderedSetX<ListX<T>>) PersistentCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default POrderedSetX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (POrderedSetX<ListX<T>>) PersistentCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> POrderedSetX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (POrderedSetX<C>) PersistentCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> POrderedSetX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (POrderedSetX<C>) PersistentCollectionX.super.groupedUntil(predicate, factory);
    }
    @Override
    default <R> POrderedSetX<R> retry(final Function<? super T, ? extends R> fn) {
        return (POrderedSetX<R>)PersistentCollectionX.super.retry(fn);
    }

    @Override
    default <R> POrderedSetX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (POrderedSetX<R>)PersistentCollectionX.super.retry(fn);
    }

    @Override
    default <R> POrderedSetX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (POrderedSetX<R>)PersistentCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> POrderedSetX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (POrderedSetX<R>)PersistentCollectionX.super.flatMapP(fn);
    }

    @Override
    default POrderedSetX<T> prependS(Stream<? extends T> stream) {
        return (POrderedSetX<T>)PersistentCollectionX.super.prependS(stream);
    }

    @Override
    default POrderedSetX<T> append(T... values) {
        return (POrderedSetX<T>)PersistentCollectionX.super.append(values);
    }

    @Override
    default POrderedSetX<T> append(T value) {
        return (POrderedSetX<T>)PersistentCollectionX.super.append(value);
    }

    @Override
    default POrderedSetX<T> prepend(T value) {
        return (POrderedSetX<T>)PersistentCollectionX.super.prepend(value);
    }

    @Override
    default POrderedSetX<T> prepend(T... values) {
        return (POrderedSetX<T>)PersistentCollectionX.super.prepend(values);
    }

    @Override
    default POrderedSetX<T> insertAt(int pos, T... values) {
        return (POrderedSetX<T>)PersistentCollectionX.super.insertAt(pos,values);
    }

    @Override
    default POrderedSetX<T> deleteBetween(int start, int end) {
        return (POrderedSetX<T>)PersistentCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default POrderedSetX<T> insertAtS(int pos, Stream<T> stream) {
        return (POrderedSetX<T>)PersistentCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default POrderedSetX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (POrderedSetX<T>)PersistentCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> POrderedSetX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (POrderedSetX<T>)PersistentCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default POrderedSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (POrderedSetX<T>)PersistentCollectionX.super.plusLoop(max,value);
    }

    @Override
    default POrderedSetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (POrderedSetX<T>)PersistentCollectionX.super.plusLoop(supplier);
    }


}
