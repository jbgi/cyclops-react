package com.aol.cyclops.comprehensions.converters;

import java.util.Optional;
import java.util.OptionalDouble;

import lombok.val;

public class OptionalLongToOptionalConverter implements
		MonadicConverter<Optional> {

	@Override
	public boolean accept(Object o) {
		return (o instanceof OptionalDouble);
	}

	@Override
	public Optional convertToMonadicForm(Object f) {
		val optional = (OptionalDouble)f;
		if(optional.isPresent())
			return Optional.of(optional.getAsDouble());
		return Optional.empty();
	}

}
