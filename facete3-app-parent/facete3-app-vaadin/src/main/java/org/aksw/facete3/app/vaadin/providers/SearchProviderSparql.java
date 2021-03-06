package org.aksw.facete3.app.vaadin.providers;

import java.util.Arrays;
import java.util.function.Function;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromRootedQuery;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromRootedQueryImpl;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1Impl;
import org.aksw.jena_sparql_api.mapper.RootedQuery;
import org.aksw.jena_sparql_api.mapper.RootedQueryFromPartitionedQuery1;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.syntax.Element;

public class SearchProviderSparql
    implements SearchProvider
{
    protected Function<String, ? extends UnaryRelation> searchStringToConcept;

    public SearchProviderSparql(Function<String, ? extends UnaryRelation> searchStringToConcept) {
        super();
        this.searchStringToConcept = searchStringToConcept;
    }

    @Override
    public RDFNodeSpec search(String searchString) {
        UnaryRelation concept = searchStringToConcept.apply(searchString);

        Element elt = new RelationImpl(
                ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
                Arrays.asList(Vars.s, Vars.p, Vars.o))
                .joinOn(Vars.s)
                .with(concept, concept.getVar())
                .getElement();

        Query query = new Query();
        query.setQueryConstructType();
        query.setQueryPattern(elt);

        PartitionedQuery1 pq = new PartitionedQuery1Impl(query, concept.getVar());
        RootedQuery rq = new RootedQueryFromPartitionedQuery1(pq);
        RDFNodeSpecFromRootedQuery result = new RDFNodeSpecFromRootedQueryImpl(rq);
        return result;
    }

    @Override
    public String toString() {
        return "sparql";
    }
}
