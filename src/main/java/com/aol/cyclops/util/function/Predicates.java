package com.aol.cyclops.util.function;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.hamcrest.Matcher;
import org.jooq.lambda.tuple.Tuple1;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Matchable.MTuple1;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MTuple4;
import com.aol.cyclops.control.Matchable.MTuple5;
import com.aol.cyclops.control.Matchable.MatchableTuple1;
import com.aol.cyclops.control.Matchable.MatchableTuple2;
import com.aol.cyclops.control.Matchable.MatchableTuple3;
import com.aol.cyclops.control.Matchable.MatchableTuple4;
import com.aol.cyclops.control.Matchable.MatchableTuple5;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.internal.matcher2.ADTPredicateBuilder;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * 
 * Some useful Predicates
 * 
 * @author johnmcclean
 *
 */
public class Predicates {

	public static <T> Predicate<T> p(Predicate<T> p){
		return p;
	}

	/**
	 * wildcard predicate
	 * 
	 */
	public static final Predicate __ = test ->true;
	
	/**
	 * @see Predicates#__
	 * @return Wildcard predicate, capitlised to disambiguate from Hamcrest.any()
	 * 
	 */
	public static final <Y> Predicate<Y> any(){  return __; };
	
	/**
	 * Match against any object that is an instance of supplied type
	 * 
	 * @param c Class type to match against
	 * @return Predicate that mathes against type
	 */
	public static final <Y> Predicate<Y> any(Class<Y> c){  return a -> a.getClass().isAssignableFrom(c); };
	
	
	/**
	 * Recursively decompose and match against case classes of specified type.
	 * 
	 * <pre>
	 * {@code
	 *  return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).with(__,new Const(0)))
				.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
				.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
				
				
				.apply(e).orElse(new Const(-1));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param type Classs type to decompose
	 * @return Predicate builder that can decompose classes of specified type
	 */
	public	static<T> ADTPredicateBuilder<T> type(Class<T> type){
			return new ADTPredicateBuilder<>(type);
	}

	
    @NoArgsConstructor
    public static class PredicateBuilder1<T1> implements Predicate<Matchable.MatchableTuple1<T1>>{
        Predicate predicate;
        public PredicateBuilder1(MTuple1<Predicate<? super T1>> when) {   
            predicate = new ADTPredicateBuilder<>(Matchable.MatchableTuple2.class).isGuard(when.v1());
          
        }

        @Override
        public boolean test(MatchableTuple1<T1> t) {
          return predicate.test(t);
        }
    }
    public static <T1,T2,T3> PredicateBuilder1<T1> decons(MTuple1<Predicate<? super T1>> when){
        
        return new PredicateBuilder1<T1>(when);
    }	
    @NoArgsConstructor
    public static class PredicateBuilder2<T1,T2> implements Predicate<Matchable.MatchableTuple2<?,T1,T2>>{
        Predicate predicate;
        public PredicateBuilder2(MTuple2<Predicate<? super T1>,Predicate<? super T2>> when) {   
            predicate = new ADTPredicateBuilder<>(Matchable.MatchableTuple2.class).isGuard(when.v1(),when.v2());
          
        }

        @Override
        public boolean test(MatchableTuple2<?,T1,T2> t) {
          return predicate.test(t);
        }
    }
    public static <T1,T2,T3> PredicateBuilder2<T1,T2> decons(MTuple2<Predicate<? super T1>,Predicate<? super T2>> when){
        
        return new PredicateBuilder2<T1,T2>(when);
    }
    @NoArgsConstructor
    public static class PredicateBuilder3<T1,T2,T3> implements Predicate<Matchable.MatchableTuple3<?,T1,T2,T3>>{
        Predicate predicate;
        public PredicateBuilder3(MTuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> when) {   
            predicate = new ADTPredicateBuilder<>(Matchable.MatchableTuple3.class).isGuard(when.v1(),when.v2(),when.v3());
         
        }

        @Override
        public boolean test(MatchableTuple3<?,T1,T2,T3> t) {
          return predicate.test(t);
        }
    }
    public static <T1,T2,T3> PredicateBuilder3<T1,T2,T3> decons(MTuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> when){
        
        return new PredicateBuilder3<T1,T2,T3>(when);
    }
    @NoArgsConstructor
    public static class PredicateBuilder4<T1,T2,T3,T4> implements Predicate<Matchable.MatchableTuple4<?,T1,T2,T3,T4>>{
        Predicate predicate;
        public PredicateBuilder4(MTuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> when) {   
            predicate = new ADTPredicateBuilder<>(Matchable.MatchableTuple4.class).isGuard(when.v1(),when.v2(),when.v3(),when.v4());
          
        }

        @Override
        public boolean test(MatchableTuple4<?,T1,T2,T3,T4> t) {
          return predicate.test(t);
        }
    }
    public static <T1,T2,T3,T4> PredicateBuilder4<T1,T2,T3,T4> decons(MTuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> when){
        
        return new PredicateBuilder4<T1,T2,T3,T4>(when);
    }
    @NoArgsConstructor
    public static class PredicateBuilder5<T1,T2,T3,T4,T5> implements Predicate<Matchable.MatchableTuple5<?,T1,T2,T3,T4,T5>>{
        Predicate predicate;
        public PredicateBuilder5(MTuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> when) {   
            predicate = new ADTPredicateBuilder<>(Matchable.MatchableTuple5.class).isGuard(when.v1(),when.v2(),when.v3(),when.v4());
          
        }

        @Override
        public boolean test(MatchableTuple5<?,T1,T2,T3,T4,T5> t) {
          return predicate.test(t);
        }
    }
    public static <T1,T2,T3,T4,T5> PredicateBuilder5<T1,T2,T3,T4,T5> decons(MTuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> when){
        
        return new PredicateBuilder5<T1,T2,T3,T4,T5>(when);
    }
	/**
	 * Recursively compose an Object without specifying a type
	 * 
	 * <pre>
	 * {@code 
	 * return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).with(__,new Const(0)))
				.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
				.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
				
				
				.apply(e).orElse(new Const(-1));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param values To match against
	 * @return Predicate builder that can decompose Case class and match against specified values
	 */
	@SafeVarargs
	public	static<V> Predicate<V> has(V... values){
		return new ADTPredicateBuilder<Object>(Object.class).hasGuard(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> hasWhere(Predicate<V>... values){
		return (Predicate<V>) new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(Object.class).hasWhere(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> hasMatch(Matcher<V>... values){
		return (Predicate<V>)new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(Object.class).hasMatch(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> is(V... values){
		return new ADTPredicateBuilder<Object>(Object.class).<V>isGuard(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> isWhere(Predicate<V>... values){
		return (Predicate<V>)new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(Object.class).isWhere(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> isMatch(Matcher<V>... values){
		return (Predicate<V>)new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(Object.class).isMatch(values);
	}
	
	public	static<V> Predicate<V> eq(V value){
	//	return new ADTPredicateBuilder<Object>(Object.class).eq(value);
	    return test->Objects.equals(test, value);
	}

	public static <T1> Predicate<? super T1> not(Predicate<? super T1> p){
		return p.negate();
	}
	
	
	@SafeVarargs
	public static <T1> Predicate<? super T1> in(T1... values){
		return test ->Arrays.asList(values).contains(test);
	}
	public static <T1 extends Comparable<T1>> Predicate<? super T1> greaterThan(T1 v){
		return test -> test.compareTo(v)>0;
	}
	public static <T1 extends Comparable<T1>> Predicate<? super T1> greaterThanOrEquals(T1 v){
		return test -> test.compareTo(v)>=0;
	}
	public static <T1 extends Comparable<T1>> Predicate<? super T1> lessThan(T1 v){
		return test -> test.compareTo(v)<0;
	}
	public static <T1 extends Comparable<T1>> Predicate<? super T1> lessThanOrEquals(T1 v){
		return test -> test.compareTo(v)<=0;
	}
	public static <T1 extends Comparable<T1>> Predicate<? super T1> equals(T1 v){
		return test -> test.compareTo(v)==0;
	}
	public static <T1> Predicate<? super T1> nullValue(){
		return test -> test==null;
	}
	public static <T1> Predicate<? super T1> instanceOf(Class<T1> clazz){
		return test -> clazz.isAssignableFrom(test.getClass());
	}
	@SafeVarargs
	public static <T1> Predicate<? super T1> allOf(Predicate<? super T1>...preds){
		return test -> ReactiveSeq.of(preds).map(t->t.test(test)).allMatch(r->r);
	}
	@SafeVarargs
	public static <T1> Predicate<? super T1> anyOf(Predicate<? super T1>...preds){
		return test -> ReactiveSeq.of(preds).map(t->t.test(test)).anyMatch(r->r);
	}
	@SafeVarargs
	public static <T1> Predicate<? super T1> noneOf(Predicate<? super T1>...preds){
		return test -> ReactiveSeq.of(preds).map(t->t.test(test)).noneMatch(r->r);
	}
	@SafeVarargs
	public static <T1> Predicate<? super T1> xOf(int x,Predicate<? super T1>...preds){
		return test -> ReactiveSeq.of(preds).map(t->t.test(test)).xMatch(x,r->r);
	}
	
	
	
}
