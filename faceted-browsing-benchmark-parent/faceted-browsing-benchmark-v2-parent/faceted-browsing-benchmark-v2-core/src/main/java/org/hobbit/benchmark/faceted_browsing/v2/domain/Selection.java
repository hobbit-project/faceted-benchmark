package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;

public interface Selection
	extends Resource
{
	void setAlias(Var alias);
	Var getAlias();
}