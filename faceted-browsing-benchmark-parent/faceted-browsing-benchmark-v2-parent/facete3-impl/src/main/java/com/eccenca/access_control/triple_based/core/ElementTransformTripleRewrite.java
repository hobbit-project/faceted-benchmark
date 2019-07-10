package com.eccenca.access_control.triple_based.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathRewriter;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.ValueSetOld;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformApplyElementTransform;


public class ElementTransformTripleRewrite
	extends ElementTransformTripleBasedRewrite
{
	//protected Generator<Var> vargen = VarGeneratorImpl2.create("i");

	@Override
	public Element applyTriplePathTransform(TriplePath tp) {
		Path basePath = tp.getPath();
		Path effectivePath = pathRewriter.apply(basePath);

		Path foldPath = PathUtils.foldNulls(effectivePath);
		
		//System.out.println(effectivePath + " -> " + finalPath);
		
		Path finalPath = PathUtils.isNull(foldPath)
				? PathFactory.pathLink(NodeUtils.nullUriNode)
				: foldPath;
		
		Element result = ElementUtils.createElementPath(tp.getSubject(), finalPath, tp.getObject());

		// Inject a FILTER(FALSE)
		if(PathUtils.isNull(foldPath)) {
			result = ElementUtils.groupIfNeeded(result, new ElementFilter(NodeValue.FALSE));
		}

		return result;
	}

    public Element applyTripleTransform(Triple t) {
        Element result = ElementTransformTripleRewrite.applyTransform(t, genericLayer.getRelation().toTernaryRelation(), null, varGen);
        return result;
    }

    protected GenericLayer genericLayer;
    protected Generator<Var> varGen;


    protected transient	PathRewriter pathRewriter;

    public ElementTransformTripleRewrite(GenericLayer genericLayer) {
        this(genericLayer, VarGeneratorImpl2.create("inj"));
    }

    public ElementTransformTripleRewrite(GenericLayer genericLayer, Generator<Var> varGen) {
        super();
        this.genericLayer = genericLayer;    	
    	this.varGen = varGen;
    }

    /**
     *
     * Returns null if no transformation needed to be applied
     *
     * @param triple
     * @param filter
     * @param varGen
     * @return
     */
    public static Element applyTransform(Triple triple, TernaryRelation filter, ValueSetOld<Binding> valueSet, Generator<Var> varGen) {

    	// If the relation is a mere basic graph pattern, we can just substitute its variables with
    	// the rdf terms / variables of the triple    	
    	// However, if the relation is a query of the form 'SELECT (... AS ?x)' we cannot substitute ?x for
    	// a constant. In this case, we have to map ?x to a fresh variable and add a filter
    	
    	//List<Node> Arrays.asList(triple.getSubject(), triple.getPredicate(), triple.getObject());
    	Node[] tripleNodes = TripleUtils.toArray(triple);
    	Set<Var> tripleVars = NodeUtils.getVarsMentioned(Arrays.asList(tripleNodes));
    	
    	Set<Var> filterVars = filter.getVarsMentioned();
    	Set<Var> blacklist = new HashSet<Var>(filterVars);
    	blacklist.addAll(tripleVars);
    	
    	
    	
    	Generator<Var> vg = VarGeneratorBlacklist.create(varGen, blacklist);
    	//List<Var> filterProj = filter.getVars();
    	
    	ExprList filters = new ExprList();
    	Var[] newTripleVars = new Var[3];
    	for(int i = 0; i < tripleNodes.length; ++i) {
    		Node tn = tripleNodes[i];
			Var tgt;
			if(tn.isVariable()) {
    			tgt = (Var)tn;
    		} else {
    			tgt = vg.next();
    			filters.add(new E_Equals(new ExprVar(tgt), NodeValue.makeNode(tn)));
    		}
			newTripleVars[i] = tgt;
			//relationVarMap.put(src, tgt);
    	}
    	
    	Element result = RelationUtils.renameNodes(
    			filter,
    			Arrays.asList(newTripleVars));
    	
    	if(!filters.isEmpty()) {
    		result = ElementUtils.groupIfNeeded(result,
    				new ElementFilter(ExprUtils.andifyBalanced(filters)));
    	}
    	
    	//Relation transformed = filter.applyNodeTransform(new NodeTransformSubst(map));

    	//ExprList el = new ExprList();
    	//el.add(new E_Equals(new ExprVar(filter.getS()), ));
    	
    		
    	
    	
//    	Element e = filter.getElement();
//
//    	Set<Var> vas = Sets.newHashSet(filter.getS(), filter.getP(), filter.getO());
//    	Set<Var> vbs = ElementUtils.getMentionedVars(e);
//    	Map<Var, Var> tmp = VarUtils.createDistinctVarMap(vas, vbs, true, varGen);
//    	map.putAll(tmp);
//    	
//    	map.put(filter.getS(), triple.getSubject());
//    	map.put(filter.getP(), triple.getPredicate());
//    	map.put(filter.getO(), triple.getObject());
//
//    	NodeTransform nodeTransform = new NodeTransformSubst(map);
//    	Element result = ElementUtils.applyNodeTransform(e, nodeTransform);

    	return result;
    }
    
    
    public static Query transform(Query query, GenericLayer conceptLayer, boolean cloneOnChange) {
        Element oldQueryPattern = query.getQueryPattern();
        Element newQueryPattern = transform(oldQueryPattern, conceptLayer);

        Query result;
        if(oldQueryPattern == newQueryPattern) {
            result = query;
        } else {
            result = cloneOnChange ? query.cloneQuery() : query;
            result.setQueryPattern(newQueryPattern);
        }

        return result;
    }

    public static Element transform(Element element, GenericLayer conceptLayer) { //ValueSet<Node> valueSet) {    	
    	ElementTransformTripleRewrite elementTransform = new ElementTransformTripleRewrite(conceptLayer);
        Element result = ElementTransformer.transform(element, elementTransform, new ExprTransformApplyElementTransform(elementTransform));
        
        ElementTransform t2 = new ElementTransformCleanGroupsOfOne();
        result = ElementTransformer.transform(result, t2, new ExprTransformApplyElementTransform(t2));
        return result;
    }
}