package org.aksw.facete3.app.vaadin.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.beust.jcommander.internal.Lists;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import org.aksw.facete3.app.vaadin.TransformService;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class ResourceComponent extends VerticalLayout {

    private static final long serialVersionUID = -6150238480758268911L;
    private RDFNode node;
    private Grid<Row> grid;
    private Label subject, authors;
    private Anchor titleLink; 
    private TextArea summary;
    // TODO: move this part to config and init it there  !!
    private Property titleProperty = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/title");
    private Property id = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/identifiedBy");
    private Property abs = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/summary");
    private Property creator = ResourceFactory.createProperty("http://purl.org/dc/terms/creator");
	private HashMap<Object,List<Property>> objectToProperty = new HashMap<>();

    public void setNode(RDFNode node) {
        this.node = node;
        refesh();
    }

    public class Row {

        private String predicate;
        private RDFNode object;

        public String getPredicate() {
            return predicate;
        }

        public RDFNode getObject() {
            return object;
        }

        public Row(String labelPredicate, RDFNode object) {
            this.predicate = labelPredicate;
            this.object = object;
        }
    }

    public ResourceComponent(TransformService transformService) {
    	//this.transformService = transformService;
    	// ..
        grid = new Grid<>(Row.class);
        grid.setItems(getRows());
        add(new Label("Paper Data"));
        //add(new ComponentRenderer<>(paper -> {
        subject = new Label();
        authors = new Label();
        titleLink = new Anchor();
        summary = new TextArea();
        summary.setWidthFull();
        summary.setVisible(false);
        add(titleLink);
        add(authors);
        add(summary);
        add(subject);
        objectToProperty.put(titleLink,Lists.newArrayList(titleProperty,id));
    	objectToProperty.put(summary,Lists.newArrayList(abs));
    	objectToProperty.put(authors,Lists.newArrayList(creator));
        grid.getColumns()
                .forEach(grid::removeColumn);
        grid.addColumn(new ComponentRenderer<>(row -> {
        	Anchor anchor = new Anchor(); 
        	anchor.setText(transformService.handleResource(row.getPredicate()));
        	anchor.setHref(row.getPredicate());
        	return anchor;
        })).setHeader("Predicate").setWidth("200px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(row -> {
        	Anchor anchor = new Anchor();
        	anchor.setText(transformService.handleObject(row.getObject()));
        	if (row.getObject().isResource()) {
        			anchor.setText(transformService.handleObject(row.getObject()));
        			anchor.setHref(row.getObject().toString());
        	} 
        	else {
        		anchor.removeHref();
        		anchor.getElement().getStyle().set("color", "hsla(214, 40%, 16%, 0.94)");
        	}
        	return anchor;
        })).setHeader("Object").setFlexGrow(1);
        
        add(grid);
        refesh();
    }

    private List<Row> getRows() {
        LinkedList<Row> rows = new LinkedList<Row>();
        if (node != null && node.isResource()) {
            Resource resource = node.asResource();
            List<Property> predicates = resource.listProperties()
                    .mapWith(Statement::getPredicate)
                    .toList()
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
            for (Property predicate : predicates) {
                List<RDFNode> objects = ResourceUtils.listPropertyValues(resource, predicate)
                        .toList();
                String labelPredicate = predicate.toString();
                for (RDFNode object : objects) {
                	object.toString();
                    rows.add(new Row(labelPredicate, object));
                    labelPredicate = "";
                }
            }
        }
        return rows;
       
    }
   
    // TODO: clear if there are no resources
    private String getViewText(Property property) {
    	Resource resource = node.asResource();
    	String text = "";
    			if (resource.hasProperty(property)) {
    				StmtIterator it =  resource.listProperties(property);
    				while (it.hasNext()) {
    					String nextText = it.next().getString();
    					text = nextText
    						.concat(" ");
    				}
    			}
    	return text;
    }

    public void refesh() {
        if (node != null) {
        	for (Entry<Object, List<Property>> entry : objectToProperty.entrySet()) {
        		Object key = entry.getKey();
        		List<Property> properties = entry.getValue();
        		for (Property property : properties) {
        			String viewText = getViewText(property);
        			if (key.equals(titleLink)) {
        				if (property.equals(titleProperty)) {
						titleLink.setText(viewText);
        				}
        				else { titleLink.setHref(viewText); }
        			}
        			else if (key.equals(summary)) {
        				summary.setValue(viewText);
        				summary.setVisible(true);
        			}
        			else { authors.setText(viewText); }
        		}
        	}
        	subject.setText("Subject: " + node.toString());
        } 
        grid.setItems(getRows());
    }
}
