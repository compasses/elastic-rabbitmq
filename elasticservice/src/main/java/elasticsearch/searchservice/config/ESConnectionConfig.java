package elasticsearch.searchservice.config;

/**
 * Created by I311352 on 6/9/2017.
 */
public class ESConnectionConfig {
    private String esDatabase;
    private String esHost;
    private Integer esPort;

    public ESConnectionConfig() {
    }

    public String getEsDatabase() {
        return this.esDatabase;
    }

    public void setEsDatabase(String esDatabase) {
        this.esDatabase = esDatabase;
    }

    public String getEsHost() {
        return this.esHost;
    }

    public void setEsHost(String esHost) {
        this.esHost = esHost;
    }

    public Integer getEsPort() {
        return this.esPort;
    }

    public void setEsPort(Integer esPort) {
        this.esPort = esPort;
    }
}