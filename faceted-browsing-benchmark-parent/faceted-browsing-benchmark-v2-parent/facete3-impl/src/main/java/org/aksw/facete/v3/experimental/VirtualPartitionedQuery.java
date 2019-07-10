package org.aksw.facete.v3.experimental;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.AliasedPath;
import org.aksw.facete.v3.api.AliasedPathImpl;
import org.aksw.facete.v3.api.traversal.TraversalDirNode;
import org.aksw.facete.v3.api.traversal.TraversalMultiNode;
import org.aksw.facete.v3.api.traversal.TraversalNode;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.XExpr;
import org.aksw.jena_sparql_api.data_query.api.ResolverNode;
import org.aksw.jena_sparql_api.data_query.impl.DataQueryImpl;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.syntax.Element;

import com.eccenca.access_control.triple_based.core.ElementTransformTripleRewrite;
import com.eccenca.access_control.triple_based.core.GenericLayer;

//class Contrib {
//	protected BinaryRelation reachingRelation;
//	protected TernaryRelation graphRelation;
//	
//	public Contrib(BinaryRelation reachingRelation, TernaryRelation graphRelation) {
//		super();
//		this.reachingRelation = reachingRelation;
//		this.graphRelation = graphRelation;
//	}
//
//	public BinaryRelation getReachingRelation() {
//		return reachingRelation;
//	}
//
//	public TernaryRelation getGraphRelation() {
//		return graphRelation;
//	}
//}



interface Factory<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>> {

	D newDirNode(N node, boolean isFwd);
	M newMultiNode(D dirNode, Resource property);
	N newNode(M multiNode, String alias);
}


class PathFactoryNode<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>>
	extends PathNode<N, D, M>
{
	protected Factory<N, D, M> factory;

	@Override
	public D create(boolean isFwd) {
		D result = factory.newDirNode((N)this, isFwd);
		return result;
	}	
}

class PathFactoryDirNode<N extends TraversalNode<N,D,M>, D extends TraversalDirNode<N, M>, M extends TraversalMultiNode<N>>
	extends PathDirNode<N, M>
{
	protected Factory<N, D, M> factory;

	public PathFactoryDirNode(N parent, boolean isFwd, Factory<N, D, M> factory) {
		super(parent, isFwd);
		this.factory = factory;
	}

	@Override
	protected M viaImpl(Resource property) {
		M result = factory.newMultiNode((D)this, property);
		return result;
	}
}




//
//interface PathResolver<P extends PathResolver<P, S, T>, S, T> {
//	P parent();
//	P step(S step);
//	T value();
//}
//
//interface StepResolver<S, C> {
//	C resolveContrib(S step);
//}
//
//
//class ParentLikn
//
//class PathResolverSimple<P extends PathResolverSimple<P>>
//	implements PathResolver<P, P_Path0, BinaryRelation>
//{
//	
//	@Override
//	public P parent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public P step(P_Path0 step) {
//		BinaryRelationImpl.create(p)
//	}
//
//	@Override
//	public BinaryRelation value() {
//		
//		
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}




/**
 * Notes on path resolutions:
 * 	- Aliased paths do not appear to make sense here:
 *    - One might think that aliases could be used to resolve properties in templates of
 *      partitioned queries such as ?s rdfs:label ?v1, ?v2.
 *      (E.g. consider a base table with multiple columns of alternative labels).
 *      If this is the way it is mapped, then we simply accept it here.
 *      There is no need to resolve rdfs:label to e.g. only ?v1 here - if this is desired, the place to
 *      where to "fix" this is in the partitioned query.
 *    - There is also does not appear to be a good reason / user case
 *      for aliases to affect the naming of variables:
 *      The output of the resolution in a virtual RDF graph according to the partitioned queries.
 *      In a virtual RDF graph, the naming of the variables are meaningless anyway,
 *      as the rewriting system on top just cares about subject, predicate and object positions
 *      but not how they are named.
 * 
 * 
 * @author raven
 *
 */
public class VirtualPartitionedQuery {
//
//	public static void rewrite(Collection<PartitionedQuery1> views, Iterable<Entry<P_Path0, String>> aliasedPath) {
//		// Rewrite a path over a collection of partitioned query views
//		
//		
//		
//		//return null;
//	}
//	
//	public void step(Collection<PartitionedQuery1> views, P_Path0 step, String alias) {
//		for(PartitionedQuery1 pq : views) {
//		
//		}
//	}
//
//	
//	// Note: The code below may not work with literals in the template due to
//	// jena not allowing literals to act as resources
//	// but actually its a pointless limitation for our purposes
//	public Resolver createResolver(PartitionedQuery1 pq, Iterable<? extends P_Path0> path) {
//		Node rootNode = pq.getPartitionVar();
//		
//		Query query = pq.getQuery();
//		Template template = query.getConstructTemplate();
//		GraphVar graphVar = new GraphVarImpl(GraphFactory.createDefaultGraph());
//		GraphUtil.add(graphVar, template.getTriples());
//		Model model = ModelFactory.createModelForGraph(graphVar);
//		
//		Resource root = model.getRDFNode(rootNode).asResource();
//		System.out.println(root.listProperties().toList());
//
//		Collection<RDFNode> starts = Collections.singleton(root); 
//		for(P_Path0 step : path) {
////			Property p = ResourceUtils.getProperty(step);
//			List<RDFNode> targets =
//				starts.stream().flatMap(s ->
//					ResourceUtils.listPropertyValues(s.asResource(), step).toList().stream())
//				.collect(Collectors.toList());
//			starts = targets;
//		}
//		
//		
//		//Element basePattern = query.getQueryPattern();
//
//		Set<Node> result = starts.stream().map(RDFNode::asNode).collect(Collectors.toSet());
//		return result;
//	}
//	
////	public static Set<Var> resolve(PartitionedQuery1 pq, Collection<Var> startVars, P_Path0 step) {
////		
////	}
//	
//	
//	public static Set<Var> resolve() {
//		//Relation baseRelation = RelationImpl.create(basePattern, PatternVars.vars(basePattern));
//
//		//FacetedQueryGenerator.createRelationForPath(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, P childPath, boolean includeAbsent) {
//
//		
//		List<TernaryRelation> trs;
//		for(RDFNode target : targets) {
//			// Generate the triple pattern (target, p, o)
//			Var var = (Var)target.asNode();
//			System.out.println(var);
//		
//			BinaryRelation br =
//				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//				.joinOn(var).with(new Concept(basePattern, var))
//				.toBinaryRelation();
//			
//		}		
//	}
//	
//	
	
	
//	public static Resolver createResolver(PartitionedQuery1 pq) {
//		RDFNode node = toRdfModel(pq);
//		Resolver result = new ResolverTemplate(pq, Collections.singleton(node));
//		return result;
//	}

	
	
//	public void step(SimplePath basePath, PartitionedQuery1 pq, P_Path0 step, boolean isFwd, String alias) {
//		System.out.println(root.listProperties().toList());
//		
//		Property p = ResourceUtils.getProperty(step);
//		List<RDFNode> targets = ResourceUtils.listPropertyValues(root, step).toList();
//		
//		Element basePattern = query.getQueryPattern();
//		//Relation baseRelation = RelationImpl.create(basePattern, PatternVars.vars(basePattern));
//
//		//FacetedQueryGenerator.createRelationForPath(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, P childPath, boolean includeAbsent) {
//
//		
//		List<TernaryRelation> trs;
//		for(RDFNode target : targets) {
//			// Generate the triple pattern (target, p, o)
//			Var var = (Var)target.asNode();
//			System.out.println(var);
//		
//			BinaryRelation br =
//				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//				.joinOn(var).with(new Concept(basePattern, var))
//				.toBinaryRelation();
//			
//		}
//		
////		// Resolve the path to a 
////		PathAccessorRdf<SimplePath> pathAccessor = new PathAccessorSimplePath();
////		PathToRelationMapper<SimplePath> mapper = new PathToRelationMapper<>(pathAccessor, "w");
////
////		basePath.
////		mapper.getOverallRelation(path);
//		
////		BinaryRelation br =
////				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
////				.joinOn(var).with(new Concept(basePattern, var))
////				.toBinaryRelation();
//		
//		
//		
//		System.out.println(ResourceUtils.listPropertyValues(root, step).toList());
//	}

	public static TernaryRelation unionTernary(Collection<? extends TernaryRelation> items) {
		Relation tmp = union(items, Arrays.asList(Vars.s, Vars.p, Vars.o));
		TernaryRelation result = tmp.toTernaryRelation();
		return result;
	}

	
	public static Relation union(Collection<? extends Relation> items, List<Var> proj) {
		List<Element> elements = items.stream()
				.map(e -> RelationUtils.rename(e, proj))
				.map(Relation::getElement)
				.collect(Collectors.toList());
		
		Element e = ElementUtils.unionIfNeeded(elements);

		Relation result = new RelationImpl(e, proj);
		return result;
	}
	
	
//	public static Query rewrite(Resolver resolver, boolean isFwd, Query query) {
//		Collection<TernaryRelation> views = resolver.getContrib(true);
//
//		TernaryRelation tr = unionTernary(views);
////		System.out.println(tr);
//		
//		GenericLayer layer = GenericLayer.create(tr);
//		
//		Query raw = ElementTransformTripleRewrite.transform(query, layer, true);
//		Query result = DataQueryImpl.rewrite(raw, DataQueryImpl.createDefaultRewriter()::rewrite);
//
//		if(false) {
//			System.out.println("Views:");
//			for(TernaryRelation view : views) {
//				System.out.println(view);
//			}
//		}
//
//		return result;
//	}
//	
	
	public static Query rewrite(Collection<TernaryRelation> views, Query query) {
//		Resolver resolver = createResolver(view, viewVar);
//		Query result = rewrite(resolver, true, query);
		TernaryRelation tr = unionTernary(views);
//		System.out.println(tr);
		
		GenericLayer layer = GenericLayer.create(tr);
		
		Query raw = ElementTransformTripleRewrite.transform(query, layer, true);
		System.out.println("Raw rewritten query:\n" + raw);
		
		Query result = DataQueryImpl.rewrite(raw, DataQueryImpl.createDefaultRewriter()::rewrite);

		return result;
	}
	
	
	/**
	 * 
	 * @return The updated partitioned query with the variable set to the target of the path
	 * 
	 * TODO Maybe we want to return a PartitionedQuery2 - with source and target var
	 */
	public static PartitionedQuery1 extendQueryWithPath(PartitionedQuery1 base, AliasedPath path) {
		Var targetVar = Var.alloc("todo-fresh-var");
		
		ResolverNode node = ResolverNodeImpl.from(base, null);
		ResolverNode target = node.walk(path);

		Collection<BinaryRelation> rawBrs = target.getPaths();

		// Set the target variable of the paths to the desired alias
//		Collection<BinaryRelation> brs = rawBrs.stream()
//				.map(br -> RelationUtils.rename(br, Arrays.asList(br.getSourceVar(), targetVar)).toBinaryRelation())
//				.collect(Collectors.toList());
		
		for(BinaryRelation br : rawBrs) {
			System.out.println("Relation: " + br);
		}
		
		return null;
	}

	public static void main(String[] args) {

		
		
		
		Query view = QueryFactory.create("CONSTRUCT { ?p <http://facetCount> ?c } { { SELECT ?p (COUNT(?o) AS ?c) { ?s ?p ?o } GROUP BY ?p } }");		
		PartitionedQuery1 pq = PartitionedQuery1.from(view, Vars.p);
		Resolver resolver = Resolver.from(pq);
		
		if(false) {
		
		Query example1 = rewrite(
				resolver
					.getContrib(true),
				QueryFactory.create("SELECT ?x ?y ?z { ?x ?y ?z }"));
		System.out.println("Example 1\n" + example1);

		Query example2 = rewrite(
				resolver
					.getContrib(true),
				QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
		System.out.println("Example 2\n" + example2);

		Query example3 = rewrite(
				resolver
					.resolve(new P_Link(NodeFactory.createURI("http://facetCount")))	
					.getContrib(true),
				QueryFactory.create("SELECT ?x ?y ?z { ?x ?y ?z }"));
		System.out.println("Example 3\n" + example3);

		Query example4a = rewrite(
				resolver
					.resolve(new P_Link(NodeFactory.createURI("http://facetCount")))	
					.getContrib(true),
				QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
		System.out.println("Example 4a\n" + example4a);
		Query example4b = rewrite(
				resolver
					.resolve(new P_Link(NodeFactory.createURI("http://facetCount")), "someAlias")	
					.getContrib(true),
				QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
		System.out.println("Example 4b\n" + example4b);
		}

		// TODO We may need to tag alias as whether it corresponds to a fixed var name
		// or a relative path id
//		System.out.println(
//				resolver
//					.resolve(new P_Link(NodeFactory.createURI("http://facetCount")), "p")	
//					.resolve(new P_Link(NodeFactory.createURI("http://label")), "labelAlias")	
//					.getPaths());

		AliasedPath path = PathBuilderNode.start()
			.fwd("http://facetCount").viaAlias("a")
			.fwd("http://label").one()//viaAlias("b")
			.aliasedPath();

		if(false) {
		path = PathBuilderNode.start()
				.fwd("http://facetCount").one()
				.fwd("http://label").one()
				.aliasedPath();

		}
		System.out.println("built path: " + path);
		
		
		// High level API:
//		System.out.println("Paths: " + (ResolverNode.from(resolver)
//			.fwd("http://facetCount").viaAlias("a")
//			.fwd("http://label").viaAlias("b")
//			.getPaths());
		
		System.out.println(pq);
		extendQueryWithPath(pq, path);
		
//
//		System.out.println(resolver
//			.resolve(new P_Link(NodeFactory.createURI("http://facetCount")))	
//			.getPaths());

	}
	
	static class GeneralizedStep {
		boolean isFwd;
		XExpr expr;
	}
	
	
	public static void extend(Element element, Collection<AliasedPathImpl> paths) {
		
	}
	
	public static void extend(Element element, BinaryRelation relation) {
		
		
	}
	//processor.step(pq, new P_Link(NodeFactory.createURI("http://facetCount")), true, "a");
	
	
	//VirtualPartitionedQuery processor = new VirtualPartitionedQuery();
	


//	Query query = QueryFactory.create("CONSTRUCT { ?city <http://hasMayor> ?mayor . ?mayor <http://hasParty> ?party } { ?city <http://hasMayor> ?mayor . ?mayor <http://hasParty> ?party }");
//	PartitionedQuery1 pq = new PartitionedQuery1(query, Var.alloc("city"));
//	Resolver resolver = createResolver(pq);
//	resolver = resolver.resolve(new P_Link(NodeFactory.createURI("http://hasMayor")));


}
