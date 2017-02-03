package com.aol.cyclops2.internal.stream.spliterators.push.grouping.groupedWhile;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class GroupedWhileTckPublisherTest extends PublisherVerification<Long>{

	public GroupedWhileTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).groupedWhile(i->false).map(l->l.get(0)).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribeAll to failed Stream
		
	}
	

}
