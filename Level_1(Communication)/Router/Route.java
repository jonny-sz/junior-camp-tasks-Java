public class Route {
    private Network network;
    private IPv4Address gateway;
    private String interfaceName;
    private int metric;

    public Route(Network network, IPv4Address gateway, String interfaceName, int metric) {
        this.network = network;
        this.gateway = gateway;
        this.interfaceName = interfaceName;
        this.metric = metric;
    }

    public IPv4Address getGateway() {
        return this.gateway;
    }

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public int getMetric() {
        return this.metric;
    }

    public Network getNetwork() {
        return this.network;
    }

    public String toString() {
        if ( this.gateway == null) {
            return String.format("net: %s, interface: %s, metric: %s",
                    this.network, this.interfaceName, this.metric);
        }
        return String.format("net: %s, gateway: %s, interface: %s, metric: %s",
                this.network, this.gateway, this.interfaceName, this.metric);
    }
}
