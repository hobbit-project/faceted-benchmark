package org.aksw.facete.v3.experimental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.AliasedPathImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.impl.QueryFragment;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

/**
 * Resolution on a template yields 2 resolvers (if the step leads to a target):
 * - Another one on the template level
 * - One on the data level
 * 
 * These two resolvers are wrapped as a single ResolverUnion
 * 
 * @author raven
 *
 */
public class ResolverTemplate
	implements Resolver
{
	/**
	 * Simple mode ensures that only at most a single path is returned.
	 * 
	 * (1) resolution on the template level takes precedence over the data level - no unions of both
	 * (2) resolution on the template level to multiple targets raises an exception
	 * 
	 */
	protected boolean singlePathMode = true;
	
	protected BinaryRelation reachingRelation;
	
	protected PartitionedQuery1 query;
	///protected Set<? extends RDFNode> starts;
	protected RDFNode start;

	public Collection<BinaryRelation> getPaths() {
		Var v = (Var)start.asNode();
		Collection<BinaryRelation> result = Collections.singleton(reachingRelation == null
				? new BinaryRelationImpl(new ElementGroup(), v, v)
				: reachingRelation);

//		Collection<BinaryRelation> result = starts.stream()
//				.map(x -> (Var)x.asNode())
//				.map(v -> new BinaryRelationImpl(new ElementGroup(), v, v))
//				.collect(Collectors.toList());

		return result;
	}
	
	
	protected ResolverTemplate(PartitionedQuery1 query, RDFNode start, BinaryRelation reachingRelation) {
		this.query = query;
		//this.starts = starts;
		this.start = start;
		this.reachingRelation = reachingRelation;
	}
	
	
	
	@Override
	public Resolver resolve(P_Path0 step, String alias) {
		
		List<Resolver> tmp = new ArrayList<>();
		tmp.addAll(resolveTemplate(step, alias));
		
		if(tmp.isEmpty() || !singlePathMode) {
			tmp.addAll(resolveData(step, alias));
		}
		
		ResolverUnion result = new ResolverUnion(tmp);
		
		return result;
	}
	

	protected Collection<Resolver> resolveTemplate(P_Path0 step, String alias) {
		Var startVar = (Var)start.asNode();
		//Collection<RDFNode> starts = Collections.singleton(root); 
//			Property p = ResourceUtils.getProperty(step);
		Set<RDFNode> targets = ResourceUtils.listPropertyValues(start.asResource(), step).toSet();
		
		if(singlePathMode && targets.size() > 1) {
			throw new RuntimeException("Single path mode enabled; but " + start + " resolved to these multiple targets " + targets + " in pattern " + query);
		}
		
			//starts.stream()
//			Collections.singleton(start).stream().flatMap(s ->
//				ResourceUtils.listPropertyValues(s.asResource(), step).toList().stream())
//			.collect(Collectors.toSet());
		
		Collection<Resolver> result = new ArrayList<>();


		// If an alias is given, create a copy of the partitioned query
		// with all variables - save for the one being joined on - renamed
		PartitionedQuery1 newPq = query;
		Set<RDFNode> newTargets = targets;
		if(alias != null && !targets.isEmpty()) {
			Map<Var, Var> renameMap = new HashMap<>();
			NodeTransformCollectNodes collector = new NodeTransformCollectNodes();
			QueryUtils.applyNodeTransform(query.getQuery(), collector);
			
			Collection<Var> vars = collector.getNodes().stream()
					.filter(n -> n.isVariable())
					.map(n -> (Var)n)
					.collect(Collectors.toSet());

			for(Var var : vars) {
				if(!var.equals(startVar)) {
					String varName = var.getName();
					Var newName = Var.alloc(alias + "_" + varName);
					renameMap.put(var, newName);
				}
			}
			
//			for(RDFNode target : targets) {
//				Var var = (Var)target.asNode();
//				String varName = var.getName();
//				Var newName = Var.alloc(alias + "_" + varName);
//				renameMap.put(var, newName);				
//			}
			Query newQuery = QueryUtils.applyNodeTransform(query.getQuery(), new NodeTransformSubst(renameMap));

			Var oldRoot = query.getPartitionVar();
			Var newRoot = renameMap.getOrDefault(oldRoot, oldRoot);
			newPq = new PartitionedQuery1(newQuery, newRoot);
			
			newTargets = new LinkedHashSet<>();
			for(RDFNode oldT : targets) {
				Model m = oldT.getModel();
				Node o = oldT.asNode();
				Var newT = renameMap.get(o);
				
				RDFNode newN = newT == null ? oldT : m.asRDFNode(newT);
				newTargets.add(newN);
				
				BinaryRelation relation = new BinaryRelationImpl(newQuery.getQueryPattern(), startVar, newT);

				if(reachingRelation != null) {
					
					Element grouped = ElementUtils.groupIfNeeded(Iterables.concat(reachingRelation.getElements(), relation.getElements()));
					relation = new BinaryRelationImpl(grouped, reachingRelation.getSourceVar(), relation.getTargetVar());
					
				}
				
				result.add(new ResolverTemplate(newPq, newN, relation));
			}
		} else {
			for(RDFNode oldT : targets) {
				result.add(new ResolverTemplate(newPq, oldT, null));
			}
		}
		
		return result;
		
		//return new ResolverTemplate(newPq, newTargets);
		
		//Element basePattern = query.getQueryPattern();

//		Set<Node> result = starts.stream().map(RDFNode::asNode).collect(Collectors.toSet());
	
	}
	
	protected Collection<Resolver> resolveData(P_Path0 step, String alias) {
		Collection<Resolver> result = new ArrayList<>();
		//for(RDFNode start : starts) {
			PartitionedQuery1 tmp = new PartitionedQuery1(query.getQuery(), (Var)start.asNode());
			Resolver item = new ResolverData(tmp, AliasedPathImpl.empty().subPath(Maps.immutableEntry(step, alias)), reachingRelation);
			result.add(item);
		//}
		
		return result;
		//return new ResolverData(query, Arrays.asList(Maps.immutableEntry(step, alias)));
	}

	@Override
	public Collection<TernaryRelation> getContrib(boolean isFwd) {
		Collection<TernaryRelation> result = new ArrayList<>();
		
		Element basePattern = query.getQuery().getQueryPattern();
		Collection<Var> baseVars = PatternVars.vars(basePattern);
		
		// Find all outgoing predicates according to the template
		//for(RDFNode rdfNode : starts) {
			
			Var var = (Var)start.asNode();
			UnaryRelation templateConcept = new Concept(basePattern, var);
			
			List<Statement> stmts = ResourceUtils.listProperties(start, isFwd).toList();
			for(Statement stmt : stmts) {
				Node s = stmt.getSubject().asNode();
				Node p = stmt.getPredicate().asNode();
				Node o = stmt.getObject().asNode();
				
				// Create a pattern ?s ?p ?o { <placeholder> BIND(?p = :const }
				// Then prepend the original pattern
//				TernaryRelation tr = new TernaryRelationImpl(
//						ElementUtils.groupIfNeeded(
//								ElementUtils.createElementTriple(Vars.s, p, Vars.o),
//								new ElementBind(Vars.p, NodeValue.makeNode(p))),
//						Vars.s, Vars.p, Vars.o);
				
				// The predicate of the triple view can be defined by the template or the pattern
				// In the first case, the predicate is a constant, otherwise its a variable
				
				TernaryRelation tr;
				if(p.isVariable()) {
					tr = new TernaryRelationImpl(basePattern,
						(Var)s, (Var)p, (Var)o);
				} else {
					// Allocate a fresh variable for 'p'
					Var freshP = VarGeneratorBlacklist.create(baseVars).next();
					
					NodeValue nvp = NodeValue.makeNode(p);
					tr = new TernaryRelationImpl(
							ElementUtils.groupIfNeeded(
									basePattern,
							new ElementBind(freshP, nvp)),
						(Var)s, freshP, (Var)o);					
				}
//				
//				TernaryRelation combined = tr
//						.prependOn((Var)s).with(templateConcept)
//						.toTernaryRelation();
						
				result.add(tr);
			}
			
			
			//rdfNode.asResource().listProperties().toList();
			
			
			//TernaryRelation tr = createRelation(isFwd, Vars.s, Vars.p, Vars.o);
			
			//Node p = NodeFactory.createURI("http://test");
			TernaryRelation tmp = new TernaryRelationImpl(
					//ElementUtils.groupIfNeeded(
							ElementUtils.createElement(QueryFragment.createTriple(!isFwd, Vars.s, Vars.p, Vars.o)),
							//new ElementBind(Vars.p, NodeValue.makeNode(p))),
					Vars.s, Vars.p, Vars.o);

			
			// I think the result of using joinOn is wrong as the lhs triple pattern gets remove
			// due to being a subject concept - however, the subject concept removal
			// must be suppressed if its variables are projected referred to
			TernaryRelation todebug =
					tmp
					.joinOn(tmp.getS()).with(templateConcept)
					.toTernaryRelation();

			
			TernaryRelation tr =
				tmp
				.prependOn(tmp.getS()).with(templateConcept)
				.toTernaryRelation();
			
			result.add(tr);

			// Create the data level contribution

			

			
//			TernaryRelation tr =
//					BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//					.joinOn(var).with(new Concept(basePattern, var))
//					.toTernaryRelation();

//			TernaryRelationImpl 
//			RDFNode from = ResourceUtils.getSource(stmt, isFwd).asNode();
//			RDFNode to = ResourceUtils.getSource(stmt, isFwd).asNode();

		
		//}
		
		return result;
	}
	
}