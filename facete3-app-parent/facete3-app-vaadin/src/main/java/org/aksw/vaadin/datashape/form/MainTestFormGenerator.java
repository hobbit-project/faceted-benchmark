package org.aksw.vaadin.datashape.form;

import org.aksw.jena_sparql_api.collection.ObservableGraph;
import org.aksw.jena_sparql_api.collection.ObservableGraphImpl;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DCAT;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHNodeShape;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MainTestFormGenerator {

    static { JenaSystem.init(); }

    public static void main(String[] args) {
        SHFactory.ensureInited();

        ObservableGraph shapeGraph = ObservableGraphImpl.decorate(RDFDataMgr.loadGraph("dcat-ap.shapes.ttl"));
        shapeGraph.addPropertyChangeListener(ev -> System.out.println("Event: " + ev));
        Model shapeModel = ModelFactory.createModelForGraph(shapeGraph);
        SHNodeShape ns = shapeModel.createResource(DCAT.Dataset.getURI()).as(SHNodeShape.class);
        // SHNodeShape nodeShape = shapeModel.createResource()

        NodeSchema schema = new NodeSchemaFromNodeShape(ns);
        // schema.createPropertySchema(RDFS.Nodes.label, false);
        PropertySchema ppp = schema.createPropertySchema(DCAT.distribution.asNode(), true);
        System.out.println("Target schema for distribution: " + ppp.getTargetSchema());

        for (PropertySchema ps : ppp.getTargetSchema().getPredicateSchemas()) {
            System.out.println(ps.getPredicate() + " " + ps.isForward());
        }



        Multimap<NodeSchema, Node> roots = HashMultimap.create();
        roots.put(schema, NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04"));

        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");

//        RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);


        RDFConnection conn = RDFConnectionFactory.connect(ds);

        for (int i = 0; i < 1; ++i) {

            NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
            Graph graph = GraphFactory.createDefaultGraph();
            dataFetcher.sync(graph, roots, conn);

            System.out.println("Fetching complete:");
            RDFDataMgr.write(System.out, ModelFactory.createModelForGraph(graph), RDFFormat.TURTLE_PRETTY);
        }

//        for (SHPropertyShape ps : ns.getPropertyShapes()) {
//            System.out.println(ps.getPath() + " " + ps.getMinCount() + " " + ps.getMaxCount() + " " + ps.getOrder() + " " + ps.getClassOrDatatype());
//        }

        // shapeGraph.add(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type));


        //NodeSchemaDataFetcher.
    }
}
