package org.aksw.facete.v3.bgp.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class BgpMultiNodeImpl
	extends ResourceBase
	implements BgpMultiNode
{
	public BgpMultiNodeImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	public static <T> Optional<T> toOptional(Iterable<T> i) {
		Iterator<T> it = i.iterator();

		T first = it.hasNext() ? it.next() : null;
		
		if(it.hasNext()) {
			throw new RuntimeException("More than 1 item found");
		}
		
		Optional<T> result = Optional.ofNullable(first);
		return result;
	}
	
	public static <T> T chainAdd(Collection<? super T> c, T item) {
		c.add(item);
		return item;
	}
	
	@Override
	public BgpNode one() {
		Set<BgpNode> set = new SetFromPropertyValues<>(this, Vocab.one, BgpNode.class);

		BgpNode result = toOptional(set).orElse(chainAdd(set, getModel().createResource()
				.addProperty(RDF.type, Vocab.BgpNode)
				.as(BgpNode.class)));
		
		return result;
	}

	@Override
	public boolean contains(BgpNode bgpNode) {
		Set<BgpNode> set = new SetFromPropertyValues<>(this, Vocab.one, BgpNode.class);

		boolean result = set.contains(bgpNode);

		return result;
	}

	@Override
	public BgpNode parent() {
		BgpNode result =
			Optional.ofNullable(
				ResourceUtils.getReversePropertyValue(this, Vocab.fwd, BgpNode.class))
			.orElseGet(() -> ResourceUtils.getReversePropertyValue(this, Vocab.bwd, BgpNode.class));
		return result;
	}

	
	@Override
	public Property reachingProperty() {
		Property result = ResourceUtils.getPropertyValue(this, Vocab.property, Property.class);
		return result;
	}

	@Override
	public boolean isReverse() {
//		boolean isReverse = false;
//		Resource entry = ResourceUtils.tryGetReversePropertyValue(parent, Vocab.fwd)
//			.orElseGet(() -> ResourceUtils.getReversePropertyValue(parent, Vocab.bwd));

		System.out.println("THIS: " + this.getId().getLabelString());
		RDFDataMgr.write(System.out, this.getModel(), RDFFormat.NTRIPLES_UTF8);
		
		this.getModel().getGraph().find().forEachRemaining(x -> System.out.println("[" + x.hashCode()+ "] " + x));
		
		boolean result =
				Optional.ofNullable(
					ResourceUtils.getReversePropertyValue(this, Vocab.fwd, BgpNode.class))
					.map(x -> false)
				
				.orElseGet(() -> ResourceUtils.tryGetReversePropertyValue(this, Vocab.bwd, BgpNode.class)
						.map(x -> true)
						.orElseThrow(() -> new IllegalStateException()));
		return result;

	}
}
