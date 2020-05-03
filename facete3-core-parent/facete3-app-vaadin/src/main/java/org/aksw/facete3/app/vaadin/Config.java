package org.aksw.facete3.app.vaadin;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("facete3")
public class Config {

    private String sparqlEnpoint;
    private final Nli nli = new Nli();

    public String getSparqlEnpoint() {
        return sparqlEnpoint;
    }

    public void setSparqlEnpoint(String sparqlEnpoint) {
        this.sparqlEnpoint = sparqlEnpoint;
    }

    public static class Nli {
        private String endpoint;
        private Double resultLimit;

        public String getEnpoint() {
            return endpoint;
        }

        public Double getResultLimit() {
            return resultLimit;
        }

        public void setResultLimit(Double resultLimit) {
            this.resultLimit = resultLimit;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    public Nli getNli() {
        return nli;
    }
}
