package cyclops;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import org.reactivestreams.Publisher;

import cyclops.monads.AnyM;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.ListX;
import com.aol.cyclops2.types.Value;
import cyclops.monads.Witness;

import lombok.experimental.UtilityClass;

/**
 * Utility class for working with JDK Optionals
 * 
 * @author johnmcclean
 *
 */
@UtilityClass
public class Optionals {

    /**
     * Perform a For Comprehension over a Optional, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Optionals.
     *
     *  <pre>
     * {@code
     *
     *   import static com.aol.cyclops2.reactor.Optionals.forEach4;
     *
    forEach4(Optional.just(1),
    a-> Optional.just(a+1),
    (a,b) -> Optional.<Integer>just(a+b),
    a                  (a,b,c) -> Optional.<Integer>just(a+b+c),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Optional
     * @param value2 Nested Optional
     * @param value3 Nested Optional
     * @param value4 Nested Optional
     * @param yieldingFunction Generates a result per combination
     * @return Optional with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Optional<R> forEach4(Optional<? extends T1> value1,
                                                                   Function<? super T1, ? extends Optional<R1>> value2,
                                                                   BiFunction<? super T1, ? super R1, ? extends Optional<R2>> value3,
                                                                   Fn3<? super T1, ? super R1, ? super R2, ? extends Optional<R3>> value4,
                                                                   Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Optional<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Optional<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Optional<R3> c = value4.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     *
     * Perform a For Comprehension over a Optional, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Optionals.
     *
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Optionals.forEach4;
     *
     *  forEach4(Optional.just(1),
    a-> Optional.just(a+1),
    (a,b) -> Optional.<Integer>just(a+b),
    (a,b,c) -> Optional.<Integer>just(a+b+c),
    (a,b,c,d) -> a+b+c+d <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Optional
     * @param value2 Nested Optional
     * @param value3 Nested Optional
     * @param value4 Nested Optional
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Optional with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Optional<R> forEach4(Optional<? extends T1> value1,
                                                                   Function<? super T1, ? extends Optional<R1>> value2,
                                                                   BiFunction<? super T1, ? super R1, ? extends Optional<R2>> value3,
                                                                   Fn3<? super T1, ? super R1, ? super R2, ? extends Optional<R3>> value4,
                                                                   Fn4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                   Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Optional<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Optional<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Optional<R3> c = value4.apply(in,ina,inb);
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     * Perform a For Comprehension over a Optional, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Optionals.
     *
     *  <pre>
     * {@code
     *
     *   import static com.aol.cyclops2.reactor.Optionals.forEach3;
     *
    forEach3(Optional.just(1),
    a-> Optional.just(a+1),
    (a,b) -> Optional.<Integer>just(a+b),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Optional
     * @param value2 Nested Optional
     * @param value3 Nested Optional
     * @param yieldingFunction Generates a result per combination
     * @return Optional with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Optional<R> forEach3(Optional<? extends T1> value1,
                                                           Function<? super T1, ? extends Optional<R1>> value2,
                                                           BiFunction<? super T1, ? super R1, ? extends Optional<R2>> value3,
                                                           Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Optional<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Optional<R2> b = value3.apply(in,ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
                });


        });

    }

    /**
     *
     * Perform a For Comprehension over a Optional, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Optionals.
     *
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Optionals.forEach3;
     *
     *  forEach3(Optional.just(1),
    a-> Optional.just(a+1),
    (a,b) -> Optional.<Integer>just(a+b),
    (a,b,c) -> a+b+c <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Optional
     * @param value2 Nested Optional
     * @param value3 Nested Optional
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Optional with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Optional<R> forEach3(Optional<? extends T1> value1,
                                                           Function<? super T1, ? extends Optional<R1>> value2,
                                                           BiFunction<? super T1, ? super R1, ? extends Optional<R2>> value3,
                                                           Fn3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                           Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Optional<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Optional<R2> b = value3.apply(in,ina);
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, in2));
                });



        });

    }

    /**
     * Perform a For Comprehension over a Optional, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Optionals.
     *
     *  <pre>
     * {@code
     *
     *   import static com.aol.cyclops2.reactor.Optionals.forEach;
     *
    forEach(Optional.just(1),
    a-> Optional.just(a+1),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Optional
     * @param value2 Nested Optional
     * @param yieldingFunction Generates a result per combination
     * @return Optional with a combined value generated by the yielding function
     */
    public static <T, R1, R> Optional<R> forEach2(Optional<? extends T> value1, Function<? super T, Optional<R1>> value2,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Optional<R1> a = value2.apply(in);
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
            });



    }

    /**
     *
     * Perform a For Comprehension over a Optional, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Optionals.
     *
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Optionals.forEach;
     *
     *  forEach(Optional.just(1),
    a-> Optional.just(a+1),
    (a,b) -> Optional.<Integer>just(a+b),
    (a,b,c) -> a+b+c <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Optional
     * @param value2 Nested Optional
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Optional with a combined value generated by the yielding function
     */
    public static <T, R1, R> Optional<R> forEach2(Optional<? extends T> value1, Function<? super T, ? extends Optional<R1>> value2,
                                                 BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Optional<R1> a = value2.apply(in);
            return a.filter(in2->filterFunction.apply(in,in2))
                        .map(in2 -> yieldingFunction.apply(in,  in2));
            });




    }


    public static Optional<Double> optional(OptionalDouble d){
        return d.isPresent() ? Optional.of(d.getAsDouble()) : Optional.empty();
    }
    public static Optional<Long> optional(OptionalLong l){
        return l.isPresent() ? Optional.of(l.getAsLong()) : Optional.empty();
    }
    public static Optional<Integer> optional(OptionalInt l){
        return l.isPresent() ? Optional.of(l.getAsInt()) : Optional.empty();
    }
    /**
     * Sequence operation, take a Collection of Optionals and turn it into a Optional with a Collection
     * By constrast with {@link Optionals#sequencePresent(CollectionX)}, if any Optionals are empty the result
     * is an empty Optional
     * 
     * <pre>
     * {@code
     * 
     *  Optional<Integer> just = Optional.of(10);
        Optional<Integer> none = Optional.empty();
     *  
     *  Optional<ListX<Integer>> opts = Optionals.sequence(ListX.of(just, none, Optional.of(1)));
        //Optional.empty();
     * 
     * }
     * </pre>
     * 
     * 
     * @param maybes Maybes to Sequence
     * @return  Maybe with a List of values
     */
    public static <T> Optional<ListX<T>> sequence(final CollectionX<Optional<T>> opts) {
        return sequence(opts.stream()).map(s -> s.toListX());

    }
    /**
     * Sequence operation, take a Collection of Optionals and turn it into a Optional with a Collection
     * Only successes are retained. By constrast with {@link Optionals#sequence(CollectionX)} Optional#empty types are 
     * tolerated and ignored.
     * 
     * <pre>
     * {@code 
     *  Optional<Integer> just = Optional.of(10);
        Optional<Integer> none = Optional.empty();
     * 
     * Optional<ListX<Integer>> maybes = Optionals.sequencePresent(ListX.of(just, none, Optional.of(1)));
       //Optional.of(ListX.of(10, 1));
     * }
     * </pre>
     * 
     * @param opts Optionals to Sequence
     * @return Optional with a List of values
     */
    public static <T> Optional<ListX<T>> sequencePresent(final CollectionX<Optional<T>> opts) {
       return sequence(opts.stream().filter(Optional::isPresent)).map(s->s.toListX());
    }
    /**
     * Sequence operation, take a Collection of Optionals and turn it into a Optional with a Collection
     * By constrast with {@link Optional#sequencePresent(CollectionX)} if any Optional types are empty 
     * the return type will be an empty Optional
     * 
     * <pre>
     * {@code
     * 
     *  Optional<Integer> just = Optional.of(10);
        Optional<Integer> none = Optional.empty();
     *  
     *  Optional<ListX<Integer>> maybes = Optionals.sequence(ListX.of(just, none, Optional.of(1)));
        //Optional.empty();
     * 
     * }
     * </pre>
     * 
     * 
     * @param opts Maybes to Sequence
     * @return  Optional with a List of values
     */
    public static <T> Optional<ReactiveSeq<T>> sequence(final Stream<Optional<T>> opts) {
        return AnyM.sequence(opts.map(AnyM::fromOptional), Witness.optional.INSTANCE)
                   .map(ReactiveSeq::fromStream)
                   .to(Witness::optional);

    }
    /**
     * Accummulating operation using the supplied Reducer (@see cyclops2.Reducers). A typical use case is to accumulate into a Persistent Collection type.
     * Accumulates the present results, ignores empty Optionals.
     * 
     * <pre>
     * {@code 
     *  Optional<Integer> just = Optional.of(10);
        Optional<Integer> none = Optional.empty();
        
     * Optional<PSetX<Integer>> opts = Optional.accumulateJust(ListX.of(just, none, Optional.of(1)), Reducers.toPSetX());
       //Optional.of(PSetX.of(10, 1)));
     * 
     * }
     * </pre>
     * 
     * @param optionals Optionals to accumulate
     * @param reducer Reducer to accumulate values with
     * @return Optional with reduced value
     */
    public static <T, R> Optional<R> accumulatePresent(final CollectionX<Optional<T>> optionals, final Reducer<R> reducer) {
        return sequencePresent(optionals).map(s -> s.mapReduce(reducer));
    }
    /**
     * Accumulate the results only from those Optionals which have a value present, using the supplied mapping function to
     * convert the data from each Optional before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     * 
     * <pre>
     * {@code 
     *  Optional<Integer> just = Optional.of(10);
        Optional<Integer> none = Optional.empty();
        
     *  Optional<String> opts = Optional.accumulateJust(ListX.of(just, none, Optional.of(1)), i -> "" + i,
                                                     Monoids.stringConcat);
        //Optional.of("101")
     * 
     * }
     * </pre>
     * 
     * @param optionals Optionals to accumulate
     * @param mapper Mapping function to be applied to the result of each Optional
     * @param reducer Monoid to combine values from each Optional
     * @return Optional with reduced value
     */
    public static <T, R> Optional<R> accumulatePresent(final CollectionX<Optional<T>> optionals, final Function<? super T, R> mapper,
            final Monoid<R> reducer) {
        return sequencePresent(optionals).map(s -> s.map(mapper)
                                                 .reduce(reducer));
    }
    /**
     * Accumulate the results only from those Optionals which have a value present, using the 
     * supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     * 
     * <pre>
     * {@code 
     *  Optional<Integer> just = Optional.of(10);
        Optional<Integer> none = Optional.empty();
        
     *  Optional<String> opts = Optional.accumulateJust(Monoids.stringConcat,ListX.of(just, none, Optional.of(1)), 
                                                     );
        //Optional.of("101")
     * 
     * }
     * </pre>
     * 
     * @param optionals Optionals to accumulate
     * @param mapper Mapping function to be applied to the result of each Optional
     * @param reducer Monoid to combine values from each Optional
     * @return Optional with reduced value
     */
    public static <T> Optional<T> accumulatePresent(final Monoid<T> reducer, final CollectionX<Optional<T>> optionals) {
        return sequencePresent(optionals).map(s -> s
                                                 .reduce(reducer));
    }

    /**
     * Combine an Optional with the provided value using the supplied BiFunction
     * 
     * <pre>
     * {@code 
     *  Optionals.combine(Optional.of(10),Maybe.just(20), this::add)
     *  //Optional[30]
     *  
     *  private int add(int a, int b) {
            return a + b;
        }
     *  
     * }
     * </pre>
     * @param f Optional to combine with a value
     * @param v Value to combine
     * @param fn Combining function
     * @return Optional combined with supplied value
     */
    public static <T1, T2, R> Optional<R> combine(final Optional<? extends T1> f, final Value<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Maybe.fromOptional(f)
                           .combine(v, fn)
                           .toOptional());
    }
    /**
     * Combine an Optional with the provided Optional using the supplied BiFunction
     * 
     * <pre>
     * {@code 
     *  Optionals.combine(Optional.of(10),Optional.of(20), this::add)
     *  //Optional[30]
     *  
     *  private int add(int a, int b) {
            return a + b;
        }
     *  
     * }
     * </pre>
     * 
     * @param f Optional to combine with a value
     * @param v Optional to combine
     * @param fn Combining function
     * @return Optional combined with supplied value, or empty Optional if no value present
     */
    public static <T1, T2, R> Optional<R> combine(final Optional<? extends T1> f, final Optional<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return combine(f,Maybe.fromOptional(v),fn);
    }

    /**
     * Combine an Optional with the provided Iterable (selecting one element if present) using the supplied BiFunction
     * <pre>
     * {@code 
     *  Optionals.zip(Optional.of(10),Arrays.asList(20), this::add)
     *  //Optional[30]
     *  
     *  private int add(int a, int b) {
            return a + b;
        }
     *  
     * }
     * </pre>
     * @param f Optional to combine with first element in Iterable (if present)
     * @param v Iterable to combine
     * @param fn Combining function
     * @return Optional combined with supplied Iterable, or empty Optional if no value present
     */
    public static <T1, T2, R> Optional<R> zip(final Optional<? extends T1> f, final Iterable<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Maybe.fromOptional(f)
                           .zip(v, fn)
                           .toOptional());
    }

    /**
     * Combine an Optional with the provided Publisher (selecting one element if present) using the supplied BiFunction
     * <pre>
     * {@code 
     *  Optionals.zip(Flux.just(10),Optional.of(10), this::add)
     *  //Optional[30]
     *  
     *  private int add(int a, int b) {
            return a + b;
        }
     *  
     * }
     * </pre> 
     * 
     * @param p Publisher to combine
     * @param f  Optional to combine with
     * @param fn Combining function
     * @return Optional combined with supplied Publisher, or empty Optional if no value present
     */
    public static <T1, T2, R> Optional<R> zip(final Publisher<? extends T2> p, final Optional<? extends T1> f,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Maybe.fromOptional(f)
                           .zipP(p, fn)
                           .toOptional());
    }
    /**
     * Narrow covariant type parameter
     * 
     * @param broad Optional with covariant type parameter
     * @return Narrowed Optional
     */
    public static <T> Optional<T> narrow(final Optional<? extends T> optional) {
        return (Optional<T>) optional;
    }

}
