package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.components.FacetedBrowserView;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;
import org.aksw.facete3.app.vaadin.qualifier.DetailView;
import org.aksw.facete3.app.vaadin.qualifier.DisplayLabelConfig;
import org.aksw.facete3.app.vaadin.qualifier.FullView;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.mapper.BestLiteralConfig;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * This is a generic context configuration which declares all DataProviders
 * used by the {@link FacetedBrowserView} Vaadin component.
 *
 * The context also features built-in support for refreshing the data providers
 * by listening to {@link RefreshScopeRefreshedEvent} events
 *
 * The context requires an {@link RDFConnection} to function.
 *
 * @author raven
 *
 */
public class ConfigFacetedBrowserView {


    @Bean
    @Autowired
    public Facete3Wrapper facetedQueryConf(RDFConnection baseDataConnection) {
        return new Facete3Wrapper(baseDataConnection);
    }

    @Bean
    @Autowired
    public ItemProvider itemProvider(
            SparqlQueryConnection baseDataConnection,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            Config config) {
        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                baseDataConnection,
                config.getAlternativeLabel(),
                prefixMapping);

        return new ItemProvider(facetedQueryConf, labelService);
    }

    @Bean
    @Autowired
    public FacetCountProvider facetCountProvider(
            SparqlQueryConnection baseDataConnection,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            Config config) {

        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                baseDataConnection,
                RDFS.label,
                prefixMapping);

        return new FacetCountProvider(facetedQueryConf, labelService);
    }

    @Bean
    @Autowired
    public FacetValueCountProvider facetValueCountProvider(
            SparqlQueryConnection baseDataConnection,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            Config config) {

        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                baseDataConnection,
                RDFS.label,
                prefixMapping);

        return new FacetValueCountProvider(facetedQueryConf, labelService);
    }


    @Bean
    @Autowired
    public FacetedBrowserView factedBrowserView(
            RDFConnection baseDataConnection,
            SearchProvider searchProvider,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            FacetCountProvider facetCountProvider,
            FacetValueCountProvider facetValueCountProvider,
            ItemProvider itemProvider,
            Config config,
            @FullView ViewManager viewManagerFull,
            @DetailView ViewManager viewManagerDetail,
            @DisplayLabelConfig BestLiteralConfig bestLabelConfig
    ) {
        return new FacetedBrowserView(
                baseDataConnection,
                searchProvider,
                prefixMapping,
                facetedQueryConf,
                facetCountProvider,
                facetValueCountProvider,
                itemProvider,
                config,
                viewManagerFull,
                viewManagerDetail,
                bestLabelConfig);
    }

    @Bean
    @Autowired
    public RefreshHandler refreshHandler () {
        return new RefreshHandler();
    }

    public static class RefreshHandler
        implements ApplicationListener<RefreshScopeRefreshedEvent>
    {
        @Autowired protected ItemProvider itemProvider;
        @Autowired protected FacetCountProvider facetCountProvider;
        @Autowired protected FacetCountProvider facetValueCountProvider;

        @Autowired protected FacetedBrowserView facetedBrowserView;

        @Override
        public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
            itemProvider.refreshAll();
            facetCountProvider.refreshAll();
            facetValueCountProvider.refreshAll();

            facetedBrowserView.onRefresh();
        }
    }



}


//@Bean
//@Autowired
//public ApplicationListener<ApplicationEvent> genericListener () {
//  return new ApplicationListener<ApplicationEvent>() {
//      @Override
//      public void onApplicationEvent(ApplicationEvent event) {
//          System.out.println("SAW EVENT: " + event);
//      }
//  };
//}

//@EventListener
//public void handleRefreshScopeRefreshedEvent(RefreshScopeRefreshedEvent ev) {
//System.out.println("THIS REFRESH WORKED");
//}
